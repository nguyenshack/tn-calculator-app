package com.example.math

import kotlin.math.*

object DistributionEngine {

    fun normalPD(x: Double, mean: Double = 0.0, stdDev: Double = 1.0): Double {
        require(stdDev > 0) { "Độ lệch chuẩn phải > 0" }
        val z = (x - mean) / stdDev
        return exp(-0.5 * z * z) / (stdDev * sqrt(2.0 * PI))
    }

    fun standardNormalCDF(z: Double): Double {
        // Abramowitz and Stegun formula 7.1.26 error function approximation
        val t = 1.0 / (1.0 + 0.2316419 * abs(z))
        val poly = t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))))
        val cdf = 1.0 - (1.0 / sqrt(2.0 * PI)) * exp(-0.5 * z * z) * poly
        return if (z >= 0) cdf else 1.0 - cdf
    }

    fun normalCD(lower: Double, upper: Double, mean: Double = 0.0, stdDev: Double = 1.0): Double {
        require(stdDev > 0) { "Độ lệch chuẩn phải > 0" }
        val zLower = (lower - mean) / stdDev
        val zUpper = (upper - mean) / stdDev
        val cdfLower = if (lower <= -1e10) 0.0 else standardNormalCDF(zLower)
        val cdfUpper = if (upper >= 1e10) 1.0 else standardNormalCDF(zUpper)
        return (cdfUpper - cdfLower).coerceIn(0.0, 1.0)
    }

    // Rational approximation for Inverse Normal (Acklam's algorithm)
    fun inverseNormal(p: Double, mean: Double = 0.0, stdDev: Double = 1.0): Double {
        require(p in 0.0..1.0) { "Xác suất phải trong khoảng [0, 1]" }
        require(stdDev > 0) { "Độ lệch chuẩn phải > 0" }
        if (p == 0.0) return Double.NEGATIVE_INFINITY
        if (p == 1.0) return Double.POSITIVE_INFINITY

        val a1 = -3.969683028665376e+01
        val a2 =  2.209460984245205e+02
        val a3 = -2.759285104469687e+02
        val a4 =  1.383577518672690e+02
        val a5 = -3.066479806614716e+01
        val a6 =  2.506628277459239e+00

        val b1 = -5.447609879822406e+01
        val b2 =  1.615858368580409e+02
        val b3 = -1.556989798598866e+02
        val b4 =  6.680131188771972e+01
        val b5 = -1.328068155288572e+01

        val c1 = -7.784894002430293e-03
        val c2 = -3.223964580411365e-01
        val c3 = -2.400758277161838e+00
        val c4 = -2.549732539343734e+00
        val c5 =  4.374664141464968e+00
        val c6 =  2.938163982698783e+00

        val d1 =  7.784695709041462e-03
        val d2 =  3.224671290700398e-01
        val d3 =  2.445134137142996e+00
        val d4 =  3.754408661907416e+00

        val pLow = 0.02425
        val pHigh = 1.0 - pLow

        val z = when {
            p < pLow -> {
                val q = sqrt(-2.0 * ln(p))
                (((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                        ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0)
            }
            p <= pHigh -> {
                val q = p - 0.5
                val r = q * q
                (((((a1 * r + a2) * r + a3) * r + a4) * r + a5) * r + a6) * q /
                        (((((b1 * r + b2) * r + b3) * r + b4) * r + b5) * r + 1.0)
            }
            else -> {
                val q = sqrt(-2.0 * ln(1.0 - p))
                -(((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                        ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0)
            }
        }
        return mean + z * stdDev
    }

    fun combinations(n: Int, k: Int): Double {
        if (k < 0 || k > n) return 0.0
        if (k == 0 || k == n) return 1.0
        val minK = min(k, n - k)
        var result = 1.0
        for (i in 1..minK) {
            result = result * (n - minK + i) / i
        }
        return result
    }

    fun binomialPD(k: Int, n: Int, p: Double): Double {
        require(n >= 0 && k in 0..n) { "k phải từ 0 đến n, n >= 0" }
        require(p in 0.0..1.0) { "Xác suất p phải trong khoảng [0, 1]" }
        if (p == 0.0) return if (k == 0) 1.0 else 0.0
        if (p == 1.0) return if (k == n) 1.0 else 0.0

        val c = combinations(n, k)
        return c * p.pow(k.toDouble()) * (1.0 - p).pow((n - k).toDouble())
    }

    fun binomialCD(k: Int, n: Int, p: Double): Double {
        require(n >= 0) { "n phải >= 0" }
        if (k < 0) return 0.0
        if (k >= n) return 1.0
        var sum = 0.0
        for (i in 0..k) {
            sum += binomialPD(i, n, p)
        }
        return sum.coerceIn(0.0, 1.0)
    }

    fun poissonPD(k: Int, lambda: Double): Double {
        require(k >= 0) { "k phải >= 0" }
        require(lambda > 0) { "λ (lambda) phải > 0" }

        // Using log-gamma for numerical stability with larger k
        var logProb = -lambda + k * ln(lambda)
        for (i in 1..k) {
            logProb -= ln(i.toDouble())
        }
        return exp(logProb)
    }

    fun poissonCD(k: Int, lambda: Double): Double {
        require(k >= 0) { "k phải >= 0" }
        require(lambda > 0) { "λ (lambda) phải > 0" }
        var sum = 0.0
        for (i in 0..k) {
            sum += poissonPD(i, lambda)
        }
        return sum.coerceIn(0.0, 1.0)
    }
}
