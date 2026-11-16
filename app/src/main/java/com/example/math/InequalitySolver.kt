package com.example.math

import kotlin.math.abs

enum class InequalitySign(val symbol: String) {
    GREATER(">"),
    GREATER_OR_EQUAL("≥"),
    LESS("<"),
    LESS_OR_EQUAL("≤")
}

data class InequalityResult(
    val solutionText: String,
    val intervals: List<Pair<Double, Double>>, // Closed/open intervals (use -1e9 for -inf, 1e9 for +inf)
    val realRoots: List<Double>
)

object InequalitySolver {

    fun solveDegree2(a: Double, b: Double, c: Double, sign: InequalitySign): InequalityResult {
        require(abs(a) > 1e-12) { "Hệ số 'a' phải khác 0" }
        val delta = b * b - 4 * a * c
        val isStrict = sign == InequalitySign.GREATER || sign == InequalitySign.LESS
        val wantPositive = sign == InequalitySign.GREATER || sign == InequalitySign.GREATER_OR_EQUAL

        // Effective sign of expression: sign(a) * sign(want)
        val deltaZero = abs(delta) < 1e-11

        if (delta < 0 && !deltaZero) {
            val exprSignPositive = a > 0
            val allSatisfied = (exprSignPositive && wantPositive) || (!exprSignPositive && !wantPositive)
            return if (allSatisfied) {
                InequalityResult("Vô số nghiệm (Mọi số thực x ∈ ℝ)", listOf(Pair(-1e9, 1e9)), emptyList())
            } else {
                InequalityResult("Vô nghiệm", emptyList(), emptyList())
            }
        }

        if (deltaZero) {
            val root = -b / (2 * a)
            val rStr = ComplexNumber.cleanDouble(root)
            val exprSignPositive = a > 0
            return when (sign) {
                InequalitySign.GREATER -> if (exprSignPositive) InequalityResult("x ≠ $rStr", listOf(Pair(-1e9, root), Pair(root, 1e9)), listOf(root)) else InequalityResult("Vô nghiệm", emptyList(), listOf(root))
                InequalitySign.GREATER_OR_EQUAL -> if (exprSignPositive) InequalityResult("Mọi số thực x ∈ ℝ", listOf(Pair(-1e9, 1e9)), listOf(root)) else InequalityResult("x = $rStr", listOf(Pair(root, root)), listOf(root))
                InequalitySign.LESS -> if (!exprSignPositive) InequalityResult("x ≠ $rStr", listOf(Pair(-1e9, root), Pair(root, 1e9)), listOf(root)) else InequalityResult("Vô nghiệm", emptyList(), listOf(root))
                InequalitySign.LESS_OR_EQUAL -> if (!exprSignPositive) InequalityResult("Mọi số thực x ∈ ℝ", listOf(Pair(-1e9, 1e9)), listOf(root)) else InequalityResult("x = $rStr", listOf(Pair(root, root)), listOf(root))
            }
        }

        // Two distinct roots
        val poly = EquationSolver.solveQuadratic(a, b, c)
        val r1 = minOf(poly.roots[0].re, poly.roots[1].re)
        val r2 = maxOf(poly.roots[0].re, poly.roots[1].re)
        val s1 = ComplexNumber.cleanDouble(r1)
        val s2 = ComplexNumber.cleanDouble(r2)

        // Sign between roots is opposite of a, outside roots is same as a
        val signBetweenPositive = a < 0
        val insideSatisfies = (signBetweenPositive && wantPositive) || (!signBetweenPositive && !wantPositive)

        return if (insideSatisfies) {
            val text = if (isStrict) "$s1 < x < $s2" else "$s1 ≤ x ≤ $s2"
            InequalityResult(text, listOf(Pair(r1, r2)), listOf(r1, r2))
        } else {
            val text = if (isStrict) "x < $s1 hoặc x > $s2" else "x ≤ $s1 hoặc x ≥ $s2"
            InequalityResult(text, listOf(Pair(-1e9, r1), Pair(r2, 1e9)), listOf(r1, r2))
        }
    }

    fun solveDegree3(a: Double, b: Double, c: Double, d: Double, sign: InequalitySign): InequalityResult {
        val poly = EquationSolver.solveCubic(a, b, c, d)
        val realRoots = poly.roots.filter { abs(it.im) < 1e-7 }.map { it.re }.distinctBy { (it * 1e5).toLong() }.sorted()

        fun eval(x: Double) = a * x * x * x + b * x * x + c * x + d
        return testIntervals(realRoots, ::eval, sign)
    }

    fun solveDegree4(a: Double, b: Double, c: Double, d: Double, e: Double, sign: InequalitySign): InequalityResult {
        val poly = EquationSolver.solveQuartic(a, b, c, d, e)
        val realRoots = poly.roots.filter { abs(it.im) < 1e-7 }.map { it.re }.distinctBy { (it * 1e5).toLong() }.sorted()

        fun eval(x: Double) = a * x * x * x * x + b * x * x * x + c * x * x + d * x + e
        return testIntervals(realRoots, ::eval, sign)
    }

    private fun testIntervals(realRoots: List<Double>, eval: (Double) -> Double, sign: InequalitySign): InequalityResult {
        if (realRoots.isEmpty()) {
            val v = eval(0.0)
            val satisfied = satisfies(v, sign)
            return if (satisfied) InequalityResult("Mọi số thực x ∈ ℝ", listOf(Pair(-1e9, 1e9)), emptyList())
            else InequalityResult("Vô nghiệm", emptyList(), emptyList())
        }

        val testPoints = mutableListOf<Double>()
        testPoints.add(realRoots.first() - 1.0)
        for (i in 0 until realRoots.size - 1) {
            testPoints.add((realRoots[i] + realRoots[i + 1]) / 2.0)
        }
        testPoints.add(realRoots.last() + 1.0)

        val satisfiedIntervals = mutableListOf<Pair<Double, Double>>()
        val isStrict = sign == InequalitySign.GREATER || sign == InequalitySign.LESS

        for (i in 0 until testPoints.size) {
            val mid = testPoints[i]
            if (satisfies(eval(mid), sign)) {
                val start = if (i == 0) -1e9 else realRoots[i - 1]
                val end = if (i == testPoints.size - 1) 1e9 else realRoots[i]
                satisfiedIntervals.add(Pair(start, end))
            }
        }

        // Format textual representation
        val parts = satisfiedIntervals.map { (st, en) ->
            val stStr = ComplexNumber.cleanDouble(st)
            val enStr = ComplexNumber.cleanDouble(en)
            when {
                st <= -1e8 -> if (isStrict) "x < $enStr" else "x ≤ $enStr"
                en >= 1e8 -> if (isStrict) "x > $stStr" else "x ≥ $stStr"
                else -> if (isStrict) "$stStr < x < $enStr" else "$stStr ≤ x ≤ $enStr"
            }
        }

        val text = when {
            parts.isEmpty() -> "Vô nghiệm"
            parts.size == 1 && satisfiedIntervals[0].first <= -1e8 && satisfiedIntervals[0].second >= 1e8 -> "Mọi số thực x ∈ ℝ"
            else -> parts.joinToString(" hoặc ")
        }

        return InequalityResult(text, satisfiedIntervals, realRoots)
    }

    private fun satisfies(value: Double, sign: InequalitySign): Boolean {
        return when (sign) {
            InequalitySign.GREATER -> value > 1e-12
            InequalitySign.GREATER_OR_EQUAL -> value >= -1e-12
            InequalitySign.LESS -> value < -1e-12
            InequalitySign.LESS_OR_EQUAL -> value <= 1e-12
        }
    }
}
