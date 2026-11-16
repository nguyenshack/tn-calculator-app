package com.example

import com.example.math.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {
    @Test
    fun testComplexArithmetic() {
        val z1 = ComplexNumber(3.0, 4.0)
        val z2 = ComplexNumber(1.0, -2.0)
        val sum = z1 + z2
        assertEquals(4.0, sum.re, 1e-6)
        assertEquals(2.0, sum.im, 1e-6)
        assertEquals(5.0, z1.modulus(), 1e-6)
    }

    @Test
    fun testMatrixDeterminantAndInverse() {
        val m = Matrix(2, 2, listOf(
            listOf(4.0, 7.0),
            listOf(2.0, 6.0)
        ))
        val det = m.determinant()
        assertEquals(10.0, det, 1e-6)

        val inv = m.inverse()
        val identity = m * inv
        assertEquals(1.0, identity[0, 0], 1e-6)
        assertEquals(0.0, identity[0, 1], 1e-6)
        assertEquals(0.0, identity[1, 0], 1e-6)
        assertEquals(1.0, identity[1, 1], 1e-6)
    }

    @Test
    fun testEquationSolver() {
        // x^2 - 5x + 6 = 0 -> roots 3, 2
        val q = EquationSolver.solveQuadratic(1.0, -5.0, 6.0)
        val roots = q.roots.map { it.re }.sorted()
        assertEquals(2.0, roots[0], 1e-6)
        assertEquals(3.0, roots[1], 1e-6)
    }

    @Test
    fun testSimultaneousLinearSystem() {
        // 2x + y = 5, x - y = 1 -> x = 2, y = 1
        val res = EquationSolver.solveLinearSystem(
            listOf(listOf(2.0, 1.0), listOf(1.0, -1.0)),
            listOf(5.0, 1.0)
        )
        assertTrue(res is LinearSystemResult.Unique)
        val vals = (res as LinearSystemResult.Unique).values
        assertEquals(2.0, vals[0], 1e-6)
        assertEquals(1.0, vals[1], 1e-6)
    }

    @Test
    fun testFactorizerAndGcdLcm() {
        assertEquals("2^4 × 3^2 × 5^2", Factorizer.factorize(3600L))
        assertEquals(12L, Factorizer.gcd(24L, 36L))
        assertEquals(72L, Factorizer.lcm(24L, 36L))
    }

    @Test
    fun testCalculusAndEvaluation() {
        val eval = MathEvaluator()
        // d/dx(x^2) at x=3 is 6
        val deriv = eval.numericalDerivative("x^2", 3.0)
        assertEquals(6.0, deriv, 1e-3)

        // Integral of 2x from 0 to 3 is 9
        val integral = eval.numericalIntegral("2*x", 0.0, 3.0)
        assertEquals(9.0, integral, 1e-3)

        // Sum of x^2 from 1 to 5 is 1+4+9+16+25 = 55
        val sum = eval.numericalSum("x^2", 1, 5)
        assertEquals(55.0, sum, 1e-6)
    }

    @Test
    fun testQrCodeGeneration() {
        val qr = QRCodeEncoder.encode("TN-001-TEST")
        assertTrue(qr.size >= 21)
        assertTrue(qr.modules.isNotEmpty())
    }

    @Test
    fun testNaturalMathParserStability() {
        // Test parsing cases that previously caused crash or infinite loop
        val cases = listOf(
            "^2",
            "x^2",
            "5^2",
            "^",
            "/",
            "sqrt(",
            "(1/2)^2",
            "5^2^3",
            "2*x^2 + 5*x - 3"
        )
        for (c in cases) {
            val segments = com.example.ui.components.NaturalMathParser.parse(c)
            assertNotNull(segments)
        }
    }

    @Test
    fun testPowerEvaluation() {
        val eval = MathEvaluator()
        eval.ans = 4.0
        val resAnsSq = eval.evaluate("^2")
        assertEquals(16.0, resAnsSq, 1e-6)

        val resFiveSq = eval.evaluate("5^2")
        assertEquals(25.0, resFiveSq, 1e-6)
    }

    @Test
    fun testNthRootAndNaturalSymbols() {
        val rootSegs = com.example.ui.components.NaturalMathParser.parse("root(3, 8)")
        assertTrue(rootSegs.any { it is com.example.ui.components.MathSegment.NthRoot })

        val formattedAsin = com.example.ui.components.formatNaturalSymbols("asin(0.5)")
        assertEquals("sin⁻¹(0.5)", formattedAsin)

        val formattedRoot = com.example.ui.components.formatNaturalSymbols("root(16)")
        assertEquals("ⁿ√(16)", formattedRoot)

        val eval = MathEvaluator()
        val cbrt8 = eval.evaluate("root(3, 8)")
        assertEquals(2.0, cbrt8, 1e-6)

        val sinInv = eval.evaluate("sin⁻¹(1)")
        assertEquals(90.0, sinInv, 1e-6)
    }
}
