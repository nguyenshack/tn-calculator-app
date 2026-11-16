package com.example.math

import kotlin.math.*

sealed class LinearSystemResult {
    data class Unique(val values: List<Double>) : LinearSystemResult()
    data object Infinite : LinearSystemResult()
    data object NoSolution : LinearSystemResult()
}

data class QuadraticResult(
    val roots: List<ComplexNumber>,
    val delta: Double,
    val vertexX: Double,
    val vertexY: Double,
    val isMin: Boolean
)

data class PolynomialResult(
    val roots: List<ComplexNumber>,
    val degree: Int
)

object EquationSolver {

    fun solveQuadratic(a: Double, b: Double, c: Double): QuadraticResult {
        require(abs(a) > 1e-12) { "Hệ số 'a' phải khác 0 đối với phương trình bậc 2" }
        val delta = b * b - 4 * a * c
        val vertexX = -b / (2 * a)
        val vertexY = c - (b * b) / (4 * a)
        val isMin = a > 0

        val roots = when {
            abs(delta) < 1e-12 -> {
                val r = -b / (2 * a)
                listOf(ComplexNumber(r, 0.0), ComplexNumber(r, 0.0))
            }
            delta > 0 -> {
                val sqrtD = sqrt(delta)
                val x1 = (-b + sqrtD) / (2 * a)
                val x2 = (-b - sqrtD) / (2 * a)
                listOf(ComplexNumber(x1, 0.0), ComplexNumber(x2, 0.0))
            }
            else -> {
                val sqrtAbsD = sqrt(-delta)
                val real = -b / (2 * a)
                val imag = sqrtAbsD / (2 * abs(a))
                listOf(ComplexNumber(real, imag), ComplexNumber(real, -imag))
            }
        }
        return QuadraticResult(roots, delta, vertexX, vertexY, isMin)
    }

    fun solveCubic(a: Double, b: Double, c: Double, d: Double): PolynomialResult {
        require(abs(a) > 1e-12) { "Hệ số 'a' phải khác 0 đối với phương trình bậc 3" }

        // Normalise: x^3 + a1*x^2 + a2*x + a3 = 0
        val a1 = b / a
        val a2 = c / a
        val a3 = d / a

        // Depress to: y^3 + p*y + q = 0 where x = y - a1/3
        val p = a2 - (a1 * a1) / 3.0
        val q = (2.0 * a1.pow(3)) / 27.0 - (a1 * a2) / 3.0 + a3
        val delta = (q * q) / 4.0 + (p * p * p) / 27.0
        val shift = a1 / 3.0

        val roots = mutableListOf<ComplexNumber>()

        when {
            abs(delta) < 1e-11 -> {
                if (abs(q) < 1e-11) {
                    // Triple root
                    val r = -shift
                    roots.add(ComplexNumber(r, 0.0))
                    roots.add(ComplexNumber(r, 0.0))
                    roots.add(ComplexNumber(r, 0.0))
                } else {
                    val u = cbrt(-q / 2.0)
                    val r1 = 2 * u - shift
                    val r2 = -u - shift
                    roots.add(ComplexNumber(r1, 0.0))
                    roots.add(ComplexNumber(r2, 0.0))
                    roots.add(ComplexNumber(r2, 0.0))
                }
            }
            delta > 0 -> {
                // One real root, two complex conjugate roots
                val sqrtDelta = sqrt(delta)
                val u = cbrt(-q / 2.0 + sqrtDelta)
                val v = cbrt(-q / 2.0 - sqrtDelta)

                val y1 = u + v
                val r1 = y1 - shift
                val realPart = -(u + v) / 2.0 - shift
                val imagPart = (u - v) * sqrt(3.0) / 2.0

                roots.add(ComplexNumber(r1, 0.0))
                roots.add(ComplexNumber(realPart, abs(imagPart)))
                roots.add(ComplexNumber(realPart, -abs(imagPart)))
            }
            else -> {
                // Three distinct real roots (casus irreducibilis)
                val r = sqrt(-p.pow(3) / 27.0)
                val phi = acos((-q / (2.0 * r)).coerceIn(-1.0, 1.0))
                val m = 2.0 * sqrt(-p / 3.0)

                val y1 = m * cos(phi / 3.0)
                val y2 = m * cos((phi + 2 * PI) / 3.0)
                val y3 = m * cos((phi + 4 * PI) / 3.0)

                roots.add(ComplexNumber(y1 - shift, 0.0))
                roots.add(ComplexNumber(y2 - shift, 0.0))
                roots.add(ComplexNumber(y3 - shift, 0.0))
            }
        }
        return PolynomialResult(roots.sortedByDescending { it.re }, 3)
    }

    private fun cbrt(x: Double): Double {
        return if (x < 0) -(-x).pow(1.0 / 3.0) else x.pow(1.0 / 3.0)
    }

    fun solveQuartic(a: Double, b: Double, c: Double, d: Double, e: Double): PolynomialResult {
        require(abs(a) > 1e-12) { "Hệ số 'a' phải khác 0 đối với phương trình bậc 4" }

        // Ferrari method or Jenkins-Traub/Durand-Kerner numerical root finding for robust stability
        // Durand-Kerner method converges rapidly in ~20 iterations for quartic polynomials
        val p = doubleArrayOf(e / a, d / a, c / a, b / a, 1.0) // a0 + a1*x + a2*x^2 + a3*x^3 + x^4
        val roots = Array(4) { i ->
            val angle = 2 * PI * i / 4.0 + 0.4
            val rad = 0.8
            ComplexNumber(rad * cos(angle), rad * sin(angle))
        }

        fun evalPoly(z: ComplexNumber): ComplexNumber {
            var res = ComplexNumber(p[4], 0.0)
            for (i in 3 downTo 0) {
                res = res * z + ComplexNumber(p[i], 0.0)
            }
            return res
        }

        repeat(60) {
            for (i in 0 until 4) {
                var denom = ComplexNumber.ONE
                for (j in 0 until 4) {
                    if (i != j) {
                        denom = denom * (roots[i] - roots[j])
                    }
                }
                if (denom.modulus() > 1e-12) {
                    val delta = evalPoly(roots[i]) / denom
                    roots[i] = roots[i] - delta
                }
            }
        }

        // Clean roots
        val cleaned = roots.map { r ->
            val reClean = if (abs(r.re) < 1e-7) 0.0 else r.re
            val imClean = if (abs(r.im) < 1e-7) 0.0 else r.im
            ComplexNumber(reClean, imClean)
        }.sortedByDescending { it.re }

        return PolynomialResult(cleaned, 4)
    }

    fun solveLinearSystem(coeffs: List<List<Double>>, constants: List<Double>): LinearSystemResult {
        val n = constants.size
        require(coeffs.size == n && coeffs.all { it.size == n }) { "Kích thước ma trận hệ số không hợp lệ" }

        val aug = Array(n) { r ->
            DoubleArray(n + 1) { c ->
                if (c < n) coeffs[r][c] else constants[r]
            }
        }

        for (i in 0 until n) {
            var pivot = i
            for (r in i + 1 until n) {
                if (abs(aug[r][i]) > abs(aug[pivot][i])) pivot = r
            }

            if (abs(aug[pivot][i]) < 1e-11) {
                // Check if any non-zero constant remains
                for (r in i until n) {
                    var allZero = true
                    for (c in 0 until n) {
                        if (abs(aug[r][c]) > 1e-11) allZero = false
                    }
                    if (allZero && abs(aug[r][n]) > 1e-11) return LinearSystemResult.NoSolution
                }
                return LinearSystemResult.Infinite
            }

            val temp = aug[i]
            aug[i] = aug[pivot]
            aug[pivot] = temp

            val div = aug[i][i]
            for (c in i until n + 1) {
                aug[i][c] /= div
            }

            for (r in 0 until n) {
                if (r != i) {
                    val factor = aug[r][i]
                    for (c in i until n + 1) {
                        aug[r][c] -= factor * aug[i][c]
                    }
                }
            }
        }

        // Verify solutions
        for (r in 0 until n) {
            var allZero = true
            for (c in 0 until n) {
                if (abs(aug[r][c]) > 1e-11) allZero = false
            }
            if (allZero) {
                return if (abs(aug[r][n]) > 1e-11) LinearSystemResult.NoSolution else LinearSystemResult.Infinite
            }
        }

        val sol = List(n) { r -> aug[r][n] }
        return LinearSystemResult.Unique(sol)
    }
}
