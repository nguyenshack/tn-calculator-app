package com.example.math

import kotlin.math.*

data class OneVarStats(
    val n: Double,
    val mean: Double,
    val sumX: Double,
    val sumX2: Double,
    val sampleVariance: Double,
    val sampleStdDev: Double,
    val popVariance: Double,
    val popStdDev: Double,
    val minX: Double,
    val q1: Double,
    val median: Double,
    val q3: Double,
    val maxX: Double
)

data class TwoVarStats(
    val n: Double,
    val meanX: Double,
    val meanY: Double,
    val sumX: Double,
    val sumY: Double,
    val sumX2: Double,
    val sumY2: Double,
    val sumXY: Double,
    val linearA: Double,
    val linearB: Double,
    val correlationR: Double,
    val rSquared: Double,
    val quadA: Double? = null,
    val quadB: Double? = null,
    val quadC: Double? = null,
    val logA: Double? = null,
    val logB: Double? = null,
    val expA: Double? = null,
    val expB: Double? = null
) {
    fun predictLinearY(x: Double) = linearA + linearB * x
    fun predictLinearX(y: Double) = if (abs(linearB) > 1e-12) (y - linearA) / linearB else Double.NaN
}

object StatisticsEngine {
    fun calculateOneVar(data: List<Pair<Double, Double>>): OneVarStats {
        val valid = data.filter { it.second > 0 }
        require(valid.isNotEmpty()) { "Dữ liệu trống hoặc tần số không hợp lệ" }

        val n = valid.sumOf { it.second }
        val sumX = valid.sumOf { it.first * it.second }
        val sumX2 = valid.sumOf { it.first * it.first * it.second }
        val mean = sumX / n

        val ss = valid.sumOf { it.second * (it.first - mean).pow(2) }
        val popVariance = ss / n
        val popStdDev = sqrt(popVariance)

        val sampleVariance = if (n > 1) ss / (n - 1) else 0.0
        val sampleStdDev = sqrt(sampleVariance)

        // Expand sorted elements for quartiles
        val expanded = mutableListOf<Double>()
        for ((x, freq) in valid.sortedBy { it.first }) {
            val count = round(freq).toInt().coerceAtLeast(1)
            repeat(count) { expanded.add(x) }
        }

        val minX = expanded.first()
        val maxX = expanded.last()
        val median = getMedian(expanded)
        val half = expanded.size / 2
        val lowerHalf = expanded.subList(0, half)
        val upperHalf = if (expanded.size % 2 == 0) expanded.subList(half, expanded.size) else expanded.subList(half + 1, expanded.size)

        val q1 = if (lowerHalf.isNotEmpty()) getMedian(lowerHalf) else minX
        val q3 = if (upperHalf.isNotEmpty()) getMedian(upperHalf) else maxX

        return OneVarStats(
            n = n,
            mean = mean,
            sumX = sumX,
            sumX2 = sumX2,
            sampleVariance = sampleVariance,
            sampleStdDev = sampleStdDev,
            popVariance = popVariance,
            popStdDev = popStdDev,
            minX = minX,
            q1 = q1,
            median = median,
            q3 = q3,
            maxX = maxX
        )
    }

    private fun getMedian(list: List<Double>): Double {
        if (list.isEmpty()) return 0.0
        val mid = list.size / 2
        return if (list.size % 2 == 1) {
            list[mid]
        } else {
            (list[mid - 1] + list[mid]) / 2.0
        }
    }

    fun calculateTwoVar(points: List<Pair<Double, Double>>): TwoVarStats {
        require(points.size >= 2) { "Cần ít nhất 2 điểm dữ liệu cho thống kê 2 biến" }
        val n = points.size.toDouble()
        val sumX = points.sumOf { it.first }
        val sumY = points.sumOf { it.second }
        val sumX2 = points.sumOf { it.first * it.first }
        val sumY2 = points.sumOf { it.second * it.second }
        val sumXY = points.sumOf { it.first * it.second }

        val meanX = sumX / n
        val meanY = sumY / n

        val denomLinear = n * sumX2 - sumX * sumX
        val linearB = if (abs(denomLinear) > 1e-12) (n * sumXY - sumX * sumY) / denomLinear else 0.0
        val linearA = meanY - linearB * meanX

        val denomR = sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
        val r = if (abs(denomR) > 1e-12) (n * sumXY - sumX * sumY) / denomR else 0.0
        val r2 = r * r

        // Quadratic fit: y = a + bx + cx^2
        var quadA: Double? = null
        var quadB: Double? = null
        var quadC: Double? = null
        if (points.size >= 3) {
            val sumX3 = points.sumOf { it.first.pow(3) }
            val sumX4 = points.sumOf { it.first.pow(4) }
            val sumX2Y = points.sumOf { it.first * it.first * it.second }

            val m = Matrix(3, 3, listOf(
                listOf(n, sumX, sumX2),
                listOf(sumX, sumX2, sumX3),
                listOf(sumX2, sumX3, sumX4)
            ))
            val v = Matrix(3, 1, listOf(
                listOf(sumY),
                listOf(sumXY),
                listOf(sumX2Y)
            ))
            try {
                val inv = m.inverse()
                val sol = inv * v
                quadA = sol[0, 0]
                quadB = sol[1, 0]
                quadC = sol[2, 0]
            } catch (_: Exception) {}
        }

        // Logarithmic fit: y = a + b * ln(x), x > 0
        var logA: Double? = null
        var logB: Double? = null
        val logValid = points.filter { it.first > 0 }
        if (logValid.size >= 2) {
            val sumLnX = logValid.sumOf { ln(it.first) }
            val sumLnX2 = logValid.sumOf { ln(it.first).pow(2) }
            val sumYLnX = logValid.sumOf { it.second * ln(it.first) }
            val sumYLog = logValid.sumOf { it.second }
            val mLog = logValid.size.toDouble()
            val denomLog = mLog * sumLnX2 - sumLnX * sumLnX
            if (abs(denomLog) > 1e-12) {
                logB = (mLog * sumYLnX - sumYLog * sumLnX) / denomLog
                logA = (sumYLog - logB * sumLnX) / mLog
            }
        }

        // Exponential fit: y = a * e^(bx), y > 0 -> ln(y) = ln(a) + bx
        var expA: Double? = null
        var expB: Double? = null
        val expValid = points.filter { it.second > 0 }
        if (expValid.size >= 2) {
            val mExp = expValid.size.toDouble()
            val sumLnY = expValid.sumOf { ln(it.second) }
            val sumXLnY = expValid.sumOf { it.first * ln(it.second) }
            val sumXExp = expValid.sumOf { it.first }
            val sumX2Exp = expValid.sumOf { it.first * it.first }
            val denomExp = mExp * sumX2Exp - sumXExp * sumXExp
            if (abs(denomExp) > 1e-12) {
                expB = (mExp * sumXLnY - sumXExp * sumLnY) / denomExp
                val lnA = (sumLnY - expB * sumXExp) / mExp
                expA = exp(lnA)
            }
        }

        return TwoVarStats(
            n = n,
            meanX = meanX,
            meanY = meanY,
            sumX = sumX,
            sumY = sumY,
            sumX2 = sumX2,
            sumY2 = sumY2,
            sumXY = sumXY,
            linearA = linearA,
            linearB = linearB,
            correlationR = r,
            rSquared = r2,
            quadA = quadA,
            quadB = quadB,
            quadC = quadC,
            logA = logA,
            logB = logB,
            expA = expA,
            expB = expB
        )
    }
}
