package com.example.math

enum class BaseMode(val prefix: String, val radix: Int, val label: String) {
    DEC("DEC", 10, "Thập phân"),
    HEX("HEX", 16, "Thập lục phân"),
    BIN("BIN", 2, "Nhị phân"),
    OCT("OCT", 8, "Bát phân")
}

object BaseNEngine {

    fun parse(input: String, mode: BaseMode): Long {
        val clean = input.trim().replace(" ", "")
        if (clean.isEmpty()) return 0L
        return clean.toLong(mode.radix)
    }

    fun format(value: Long, mode: BaseMode): String {
        return when (mode) {
            BaseMode.DEC -> value.toString(10)
            BaseMode.HEX -> value.toULong().toString(16).uppercase()
            BaseMode.BIN -> {
                val bin = value.toULong().toString(2)
                // Group every 4 bits
                bin.reversed().chunked(4).joinToString(" ").reversed()
            }
            BaseMode.OCT -> value.toULong().toString(8)
        }
    }

    fun and(a: Long, b: Long) = a and b
    fun or(a: Long, b: Long) = a or b
    fun xor(a: Long, b: Long) = a xor b
    fun not(a: Long) = a.inv()
    fun xnor(a: Long, b: Long) = (a xor b).inv()
    fun lsh(a: Long, bits: Int = 1) = a shl bits
    fun rsh(a: Long, bits: Int = 1) = a shr bits
}
