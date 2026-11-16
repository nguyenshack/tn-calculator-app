package com.example.math

import kotlin.math.abs

/**
 * Lightweight, robust, pure Kotlin QR Code Generator (Model 2, Byte Mode, ECC Low/Medium).
 * Generates exact QR code module matrices directly to compose Canvas with zero extra external dependencies.
 */
data class QRCodeMatrix(val size: Int, val modules: Array<BooleanArray>)

object QRCodeEncoder {

    fun encode(text: String): QRCodeMatrix {
        val dataBytes = text.toByteArray(Charsets.UTF_8)
        val dataLen = dataBytes.size

        // Select minimal QR Version (1..10) capable of holding dataLen bytes
        val (version, totalCodewords, dataCapacity, ecCodewords) = selectVersion(dataLen)
        val size = version * 4 + 17
        val modules = Array(size) { BooleanArray(size) { false } }
        val isFunction = Array(size) { BooleanArray(size) { false } }

        // 1. Finder patterns (top-left, top-right, bottom-left)
        drawFinderPattern(modules, isFunction, 0, 0)
        drawFinderPattern(modules, isFunction, size - 7, 0)
        drawFinderPattern(modules, isFunction, 0, size - 7)

        // 2. Timing patterns
        for (i in 8 until size - 8) {
            val bit = (i % 2 == 0)
            modules[6][i] = bit
            isFunction[6][i] = true
            modules[i][6] = bit
            isFunction[i][6] = true
        }

        // 3. Dark module
        modules[4 * version + 9][8] = true
        isFunction[4 * version + 9][8] = true

        // 4. Alignment patterns for version >= 2
        if (version >= 2) {
            val pos = alignmentPositions(version)
            for (r in pos) {
                for (c in pos) {
                    if (!isFunction[r][c]) {
                        drawAlignmentPattern(modules, isFunction, r, c)
                    }
                }
            }
        }

        // 5. Reserve format information areas
        for (i in 0..8) {
            isFunction[8][i] = true
            isFunction[i][8] = true
            isFunction[8][size - 1 - i] = true
            isFunction[size - 1 - i][8] = true
        }

        // 6. Encode data bits
        val bits = buildBitStream(dataBytes, version, dataCapacity)

        // 7. Compute Reed-Solomon Error Correction
        val codewords = bitsToCodewords(bits, dataCapacity)
        val ec = computeReedSolomon(codewords, ecCodewords)
        val allCodewords = codewords + ec

        // 8. Place data bits onto matrix in zigzag pattern
        val allBits = mutableListOf<Boolean>()
        for (b in allCodewords) {
            for (i in 7 downTo 0) {
                allBits.add(((b ushr i) and 1) == 1)
            }
        }

        var bitIndex = 0
        var upward = true
        var right = size - 1

        while (right > 0) {
            if (right == 6) right-- // skip vertical timing pattern
            val cols = listOf(right, right - 1)
            val rows = if (upward) (size - 1 downTo 0).toList() else (0 until size).toList()

            for (r in rows) {
                for (c in cols) {
                    if (!isFunction[r][c]) {
                        val bit = if (bitIndex < allBits.size) allBits[bitIndex++] else false
                        // Mask 000: (r + c) % 2 == 0
                        val mask = ((r + c) % 2 == 0)
                        modules[r][c] = bit xor mask
                    }
                }
            }
            upward = !upward
            right -= 2
        }

        // 9. Write format information (Mask 0, ECC L -> bits 111011111000100)
        drawFormatBits(modules, 0b111011111000100, size)

        return QRCodeMatrix(size, modules)
    }

    private fun selectVersion(dataLen: Int): IntArray {
        // Table of (version, totalCodewords, dataCapacity, ecCodewords) for ECC Low
        val versions = arrayOf(
            intArrayOf(1, 26, 19, 7),
            intArrayOf(2, 44, 34, 10),
            intArrayOf(3, 70, 55, 15),
            intArrayOf(4, 100, 80, 20),
            intArrayOf(5, 134, 108, 26),
            intArrayOf(6, 172, 136, 36),
            intArrayOf(7, 196, 156, 40),
            intArrayOf(8, 242, 194, 48),
            intArrayOf(9, 292, 232, 60),
            intArrayOf(10, 346, 274, 72)
        )
        for (v in versions) {
            // Byte mode header is 4 bits mode + 8 bits char count = 1.5 bytes
            if (dataLen + 3 <= v[2]) return v
        }
        return versions.last()
    }

    private fun alignmentPositions(version: Int): IntArray {
        return when (version) {
            2 -> intArrayOf(6, 18)
            3 -> intArrayOf(6, 22)
            4 -> intArrayOf(6, 26)
            5 -> intArrayOf(6, 30)
            6 -> intArrayOf(6, 34)
            7 -> intArrayOf(6, 22, 38)
            8 -> intArrayOf(6, 24, 42)
            9 -> intArrayOf(6, 26, 46)
            10 -> intArrayOf(6, 28, 50)
            else -> intArrayOf(6, 18)
        }
    }

    private fun drawFinderPattern(modules: Array<BooleanArray>, isFunc: Array<BooleanArray>, r0: Int, c0: Int) {
        for (dr in -1..7) {
            for (dc in -1..7) {
                val r = r0 + dr
                val c = c0 + dc
                if (r in modules.indices && c in modules.indices) {
                    isFunc[r][c] = true
                    val inCenter = dr in 0..6 && dc in 0..6
                    if (inCenter) {
                        val isBorder = dr == 0 || dr == 6 || dc == 0 || dc == 6
                        val isInner = dr in 2..4 && dc in 2..4
                        modules[r][c] = isBorder || isInner
                    } else {
                        modules[r][c] = false // separator
                    }
                }
            }
        }
    }

    private fun drawAlignmentPattern(modules: Array<BooleanArray>, isFunc: Array<BooleanArray>, rCenter: Int, cCenter: Int) {
        for (dr in -2..2) {
            for (dc in -2..2) {
                val r = rCenter + dr
                val c = cCenter + dc
                isFunc[r][c] = true
                modules[r][c] = (abs(dr) == 2 || abs(dc) == 2 || (dr == 0 && dc == 0))
            }
        }
    }

    private fun buildBitStream(data: ByteArray, version: Int, capacityBytes: Int): List<Boolean> {
        val bits = mutableListOf<Boolean>()
        fun appendBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                bits.add(((value ushr i) and 1) == 1)
            }
        }

        // Mode: 0100 (Byte mode)
        appendBits(0b0100, 4)
        // Count indicator: 8 bits for versions 1..9, 16 bits for version 10
        val countBits = if (version <= 9) 8 else 16
        appendBits(data.size.coerceAtMost(capacityBytes - 3), countBits)

        val maxDataLen = (capacityBytes - 3).coerceAtLeast(0)
        for (i in 0 until minOf(data.size, maxDataLen)) {
            appendBits(data[i].toInt() and 0xFF, 8)
        }

        // Terminator: up to 4 zero bits
        val totalCapacityBits = capacityBytes * 8
        val terminatorLen = minOf(4, totalCapacityBits - bits.size)
        repeat(terminatorLen) { bits.add(false) }

        // Pad to byte boundary
        while (bits.size % 8 != 0 && bits.size < totalCapacityBits) {
            bits.add(false)
        }

        // Pad bytes (0xEC, 0x11)
        val pad = intArrayOf(0xEC, 0x11)
        var padIdx = 0
        while (bits.size < totalCapacityBits) {
            appendBits(pad[padIdx % 2], 8)
            padIdx++
        }

        return bits
    }

    private fun bitsToCodewords(bits: List<Boolean>, capacity: Int): IntArray {
        val result = IntArray(capacity)
        for (i in 0 until capacity) {
            var v = 0
            for (b in 0 until 8) {
                val bitIdx = i * 8 + b
                if (bitIdx < bits.size && bits[bitIdx]) {
                    v = v or (1 shl (7 - b))
                }
            }
            result[i] = v
        }
        return result
    }

    private fun computeReedSolomon(data: IntArray, ecCount: Int): IntArray {
        // GF(256) with primitive polynomial 0x11D
        val exp = IntArray(512)
        val log = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            exp[i + 255] = x
            log[x] = i
            x = x shl 1
            if ((x and 0x100) != 0) x = x xor 0x11D
        }

        fun gfMul(a: Int, b: Int): Int {
            if (a == 0 || b == 0) return 0
            return exp[log[a] + log[b]]
        }

        // Generator polynomial
        var gen = intArrayOf(1)
        for (i in 0 until ecCount) {
            val root = exp[i]
            val newGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                newGen[j] = newGen[j] xor gfMul(gen[j], root)
                newGen[j + 1] = newGen[j + 1] xor gen[j]
            }
            gen = newGen
        }

        val remainder = IntArray(ecCount)
        for (d in data) {
            val factor = d xor remainder[0]
            for (j in 0 until ecCount - 1) {
                remainder[j] = remainder[j + 1] xor gfMul(gen[ecCount - 1 - j], factor)
            }
            remainder[ecCount - 1] = gfMul(gen[0], factor)
        }
        return remainder
    }

    private fun drawFormatBits(modules: Array<BooleanArray>, format: Int, size: Int) {
        for (i in 0 until 15) {
            val bit = ((format ushr i) and 1) == 1
            // Near top-left
            when {
                i <= 5 -> modules[8][i] = bit
                i == 6 -> modules[8][7] = bit
                i == 7 -> modules[8][8] = bit
                i == 8 -> modules[7][8] = bit
                else -> modules[14 - i][8] = bit
            }
            // Around top-right and bottom-left
            if (i < 8) {
                modules[size - 1 - i][8] = bit
            } else {
                modules[8][size - 15 + i] = bit
            }
        }
    }
}
