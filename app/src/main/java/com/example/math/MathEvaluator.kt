package com.example.math

import kotlin.math.*
import kotlin.random.Random

enum class AngleUnit(val label: String, val degFactor: Double) {
    DEG("DEG", 1.0),
    RAD("RAD", 180.0 / PI),
    GRA("GRA", 0.9) // 100 grad = 90 deg
}

data class Fraction(val num: Long, val den: Long) {
    init {
        require(den != 0L) { "Mẫu số không thể bằng 0" }
    }

    operator fun plus(other: Fraction): Fraction {
        val g = Factorizer.gcd(den, other.den)
        val l = (den / g) * other.den
        val n = num * (other.den / g) + other.num * (den / g)
        return simplify(n, l)
    }

    operator fun minus(other: Fraction): Fraction {
        val g = Factorizer.gcd(den, other.den)
        val l = (den / g) * other.den
        val n = num * (other.den / g) - other.num * (den / g)
        return simplify(n, l)
    }

    operator fun times(other: Fraction): Fraction {
        return simplify(num * other.num, den * other.den)
    }

    operator fun div(other: Fraction): Fraction {
        require(other.num != 0L) { "Chia cho phân số 0" }
        return simplify(num * other.den, den * other.num)
    }

    fun toDouble(): Double = num.toDouble() / den.toDouble()

    override fun toString(): String {
        return if (den == 1L) "$num" else "$num/$den"
    }

    companion object {
        fun simplify(n: Long, d: Long): Fraction {
            var num = n
            var den = d
            if (den < 0) {
                num = -num
                den = -den
            }
            val g = Factorizer.gcd(abs(num), abs(den))
            return if (g > 1L) Fraction(num / g, den / g) else Fraction(num, den)
        }

        fun fromDouble(v: Double, maxDen: Long = 100000L): Fraction? {
            if (v.isNaN() || v.isInfinite() || abs(v) > 1e11) return null
            val sign = if (v < 0) -1 else 1
            val x = abs(v)
            val integerPart = x.toLong()
            val frac = x - integerPart
            if (frac < 1e-9) return Fraction(sign * integerPart, 1L)
            if (abs(frac - 1.0) < 1e-9) return Fraction(sign * (integerPart + 1), 1L)

            // Continued fractions algorithm
            var h1 = 1L; var h0 = 0L
            var k1 = 0L; var k0 = 1L
            var z = frac

            while (true) {
                val a = z.toLong()
                val h2 = a * h1 + h0
                val k2 = a * k1 + k0
                if (k2 > maxDen) break

                h0 = h1; h1 = h2
                k0 = k1; k1 = k2

                val diff = z - a
                if (abs(diff) < 1e-10) break
                z = 1.0 / diff
                if (z > 1e9) break
            }

            if (k1 == 0L) return null
            val approx = h1.toDouble() / k1.toDouble()
            if (abs(approx - frac) < 1e-5) {
                return simplify(sign * (integerPart * k1 + h1), k1)
            }
            return null
        }

        fun fromRecurring(nonRecurringPart: String, recurringPart: String): Fraction {
            // E.g. 0.1(6) -> nonRecurring = "1", recurring = "6"
            val nonRec = nonRecurringPart.toLongOrNull() ?: 0L
            val rec = recurringPart.toLongOrNull() ?: 0L
            val nLen = nonRecurringPart.length
            val rLen = recurringPart.length

            val d9 = "9".repeat(rLen).toLong()
            val d0 = if (nLen > 0) 10.0.pow(nLen.toDouble()).toLong() else 1L
            val den = d9 * d0
            val num = (nonRec * 10.0.pow(rLen.toDouble()).toLong() + rec) - nonRec
            return simplify(num, den)
        }
    }
}

class MathEvaluator(
    var angleUnit: AngleUnit = AngleUnit.DEG,
    val variables: MutableMap<String, Double> = mutableMapOf(
        "A" to 0.0, "B" to 0.0, "C" to 0.0, "D" to 0.0, "E" to 0.0, "F" to 0.0,
        "x" to 0.0, "y" to 0.0, "z" to 0.0, "X" to 0.0, "Y" to 0.0, "Z" to 0.0
    ),
    var ans: Double = 0.0,
    var preAns: Double = 0.0
) {
    fun setVariable(name: String, value: Double) {
        val up = name.uppercase()
        variables[up] = value
        variables[name.lowercase()] = value
    }

    fun getVariable(name: String): Double {
        val up = name.uppercase()
        return variables[up] ?: variables[name.lowercase()] ?: 0.0
    }

    fun evaluate(expression: String): Double {
        val cleaned = sanitizeExpression(expression)
        val tokens = tokenize(cleaned)
        val parser = ExpressionParser(tokens, this)
        val result = parser.parseExpression()
        if (result.isNaN() || result.isInfinite()) {
            throw ArithmeticException("Math ERROR: Kết quả vô cực hoặc không xác định")
        }
        preAns = ans
        ans = result
        return result
    }

    fun evaluateFunctionAt(functionExpr: String, xVal: Double): Double {
        val oldX = variables["x"] ?: 0.0
        val oldUpperX = variables["X"] ?: 0.0
        try {
            variables["x"] = xVal
            variables["X"] = xVal
            val cleaned = sanitizeExpression(functionExpr)
            val tokens = tokenize(cleaned)
            val parser = ExpressionParser(tokens, this)
            return parser.parseExpression()
        } finally {
            variables["x"] = oldX
            variables["X"] = oldUpperX
        }
    }

    fun numericalDerivative(expr: String, x0: Double): Double {
        val h = 1e-5 * max(1.0, abs(x0))
        val f_p2 = evaluateFunctionAt(expr, x0 + 2 * h)
        val f_p1 = evaluateFunctionAt(expr, x0 + h)
        val f_m1 = evaluateFunctionAt(expr, x0 - h)
        val f_m2 = evaluateFunctionAt(expr, x0 - 2 * h)
        return (-f_p2 + 8.0 * f_p1 - 8.0 * f_m1 + f_m2) / (12.0 * h)
    }

    fun numericalIntegral(expr: String, a: Double, b: Double, nSteps: Int = 120): Double {
        if (abs(a - b) < 1e-14) return 0.0
        val n = if (nSteps % 2 == 1) nSteps + 1 else nSteps
        val h = (b - a) / n
        var sum = evaluateFunctionAt(expr, a) + evaluateFunctionAt(expr, b)
        for (i in 1 until n) {
            val x = a + i * h
            val weight = if (i % 2 == 1) 4.0 else 2.0
            sum += weight * evaluateFunctionAt(expr, x)
        }
        return sum * (h / 3.0)
    }

    fun numericalSum(expr: String, start: Int, end: Int): Double {
        require(start <= end) { "Giá trị bắt đầu phải ≤ giá trị kết thúc" }
        require(end - start <= 20000) { "Số lượng bước tính tổng quá lớn (tối đa 20000)" }
        var sum = 0.0
        for (i in start..end) {
            sum += evaluateFunctionAt(expr, i.toDouble())
        }
        return sum
    }

    fun numericalProduct(expr: String, start: Int, end: Int): Double {
        require(start <= end) { "Giá trị bắt đầu phải ≤ giá trị kết thúc" }
        require(end - start <= 20000) { "Số lượng bước tính tích quá lớn (tối đa 20000)" }
        var prod = 1.0
        for (i in start..end) {
            prod *= evaluateFunctionAt(expr, i.toDouble())
        }
        return prod
    }

    fun solve(equation: String, initialGuess: Double = 0.0): Double {
        // Equation form: lhs = rhs or expression = 0
        val parts = equation.split("=")
        val fExpr = if (parts.size == 2) "(${parts[0]}) - (${parts[1]})" else equation

        var x = initialGuess
        var fx = evaluateFunctionAt(fExpr, x)
        if (abs(fx) < 1e-12) return x

        // Newton-Raphson with bisection fallback
        var iter = 0
        while (iter < 80) {
            val dfx = numericalDerivative(fExpr, x)
            if (abs(dfx) < 1e-12) {
                // Perturb
                x += 0.5
            } else {
                val step = fx / dfx
                x -= step.coerceIn(-50.0, 50.0)
            }
            fx = evaluateFunctionAt(fExpr, x)
            if (abs(fx) < 1e-10) return x
            iter++
        }
        return x
    }

    private fun sanitizeExpression(expr: String): String {
        var cleaned = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("ⁿ√", "root")
            .replace("Ans", "ans")
            .replace("PreAns", "preans")
            .replace("Ran#", "ran")
            .trim()

        if (cleaned.startsWith("^") || cleaned.startsWith("*") || cleaned.startsWith("/")) {
            cleaned = "ans$cleaned"
        }
        cleaned = cleaned.replace("()", "0")
        val openCount = cleaned.count { it == '(' }
        val closeCount = cleaned.count { it == ')' }
        if (openCount > closeCount) {
            cleaned += ")".repeat(openCount - closeCount)
        }
        return cleaned
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < len && (input[i].isDigit() || input[i] == '.' || input[i] == 'E' || input[i] == 'e')) {
                    if ((input[i] == 'E' || input[i] == 'e') && i + 1 < len && (input[i + 1] == '+' || input[i + 1] == '-')) {
                        sb.append(input[i])
                        i++
                        sb.append(input[i])
                    } else {
                        sb.append(input[i])
                    }
                    i++
                }
                tokens.add(sb.toString())
            } else if (c.isLetter() || c == '_') {
                val sb = StringBuilder()
                while (i < len && (input[i].isLetterOrDigit() || input[i] == '_')) {
                    sb.append(input[i])
                    i++
                }
                tokens.add(sb.toString())
            } else {
                // Symbols
                tokens.add(c.toString())
                i++
            }
        }
        return tokens
    }
}

class ExpressionParser(private val tokens: List<String>, private val env: MathEvaluator) {
    private var pos = 0

    private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null
    private fun next(): String = tokens[pos++]

    fun parseExpression(): Double {
        var result = parseTerm()
        while (pos < tokens.size) {
            val op = peek()
            if (op == "+" || op == "-") {
                next()
                val right = parseTerm()
                result = if (op == "+") result + right else result - right
            } else {
                break
            }
        }
        return result
    }

    private fun parseTerm(): Double {
        var result = parseFactor()
        while (pos < tokens.size) {
            val op = peek()
            if (op == "*" || op == "/" || op == "%" || op == "R") {
                next()
                val right = parseFactor()
                result = when (op) {
                    "*" -> result * right
                    "/" -> {
                        if (abs(right) < 1e-15) throw ArithmeticException("Phép chia cho 0")
                        result / right
                    }
                    "%" -> result * (right / 100.0)
                    "R" -> { // Remainder / Modulo
                        if (abs(right) < 1e-15) throw ArithmeticException("Phép chia lấy dư cho 0")
                        result % right
                    }
                    else -> result
                }
            } else {
                break
            }
        }
        return result
    }

    private fun parseFactor(): Double {
        val base = parseUnary()
        if (pos < tokens.size && peek() == "^") {
            next()
            val exp = parseFactor() // Right associative
            return base.pow(exp)
        }
        return base
    }

    private fun parseUnary(): Double {
        if (pos < tokens.size) {
            val op = peek()
            if (op == "+") {
                next()
                return parseUnary()
            } else if (op == "-") {
                next()
                return -parseUnary()
            }
        }
        var atom = parseAtom()

        // Postfix operators like factorial '!'
        while (pos < tokens.size && peek() == "!") {
            next()
            val n = round(atom).toLong()
            atom = factorial(n).toDouble()
        }
        return atom
    }

    private fun parseAtom(): Double {
        if (pos >= tokens.size) throw IllegalArgumentException("Biểu thức toán học chưa hoàn chỉnh")
        val token = next()

        // Numbers
        token.toDoubleOrNull()?.let { return it }

        // Constants & Memory
        when (token.lowercase()) {
            "pi" -> return PI
            "e" -> return E
            "ans" -> return env.ans
            "preans" -> return env.preAns
            "ran" -> return Random.nextDouble()
        }

        // Parentheses
        if (token == "(") {
            val res = parseExpression()
            if (peek() == ")") next()
            return res
        }

        // Functions
        val func = token.lowercase()
        if (pos < tokens.size && peek() == "(") {
            next() // consume '('
            val args = mutableListOf<Double>()
            if (peek() != ")") {
                args.add(parseExpression())
                while (pos < tokens.size && peek() == ",") {
                    next() // consume ','
                    args.add(parseExpression())
                }
            }
            if (peek() == ")") next()

            return evaluateFunction(func, args)
        }

        // Check if token is single-variable (A..F, x..z)
        if (token.length == 1 && token[0].isLetter()) {
            return env.getVariable(token)
        }

        throw IllegalArgumentException("Ký hiệu không xác định: $token")
    }

    private fun evaluateFunction(func: String, args: List<Double>): Double {
        return when (func) {
            "sin" -> sin(toRad(args[0]))
            "cos" -> cos(toRad(args[0]))
            "tan" -> {
                val r = toRad(args[0])
                if (abs(cos(r)) < 1e-14) throw ArithmeticException("tan không xác định tại góc này")
                tan(r)
            }
            "cot" -> {
                val r = toRad(args[0])
                val s = sin(r)
                if (abs(s) < 1e-14) throw ArithmeticException("cot không xác định tại góc này")
                cos(r) / s
            }
            "asin", "arcsin" -> fromRad(asin(args[0]))
            "acos", "arccos" -> fromRad(acos(args[0]))
            "atan", "arctan" -> fromRad(atan(args[0]))
            "sinh" -> sinh(args[0])
            "cosh" -> cosh(args[0])
            "tanh" -> tanh(args[0])
            "sqrt" -> {
                if (args[0] < 0) throw ArithmeticException("Căn bậc hai của số âm")
                sqrt(args[0])
            }
            "cbrt" -> {
                if (args[0] < 0) -(-args[0]).pow(1.0 / 3.0) else args[0].pow(1.0 / 3.0)
            }
            "root" -> { // root(n, x) or root(x)
                if (args.isEmpty()) 0.0
                else if (args.size == 1) sqrt(args[0])
                else {
                    val n = args[0]
                    val x = args[1]
                    if (abs(n) < 1e-15) throw ArithmeticException("Căn bậc 0 không xác định")
                    x.pow(1.0 / n)
                }
            }
            "log" -> {
                if (args.size == 1) {
                    if (args[0] <= 0) throw ArithmeticException("log của số không dương")
                    log10(args[0])
                } else {
                    // log(base, value)
                    if (args[0] <= 0 || args[0] == 1.0 || args[1] <= 0) throw ArithmeticException("log cơ số không hợp lệ")
                    ln(args[1]) / ln(args[0])
                }
            }
            "ln" -> {
                if (args[0] <= 0) throw ArithmeticException("ln của số không dương")
                ln(args[0])
            }
            "abs" -> abs(args[0])
            "round" -> round(args[0])
            "floor" -> floor(args[0])
            "ceil" -> ceil(args[0])
            "ranint" -> {
                val a = args[0].toInt()
                val b = args[1].toInt()
                Random.nextInt(minOf(a, b), maxOf(a, b) + 1).toDouble()
            }
            "npr" -> {
                val n = args[0].toLong()
                val r = args[1].toLong()
                permutation(n, r).toDouble()
            }
            "ncr" -> {
                val n = args[0].toInt()
                val r = args[1].toInt()
                DistributionEngine.combinations(n, r)
            }
            "gcd" -> Factorizer.gcd(args[0].toLong(), args[1].toLong()).toDouble()
            "lcm" -> Factorizer.lcm(args[0].toLong(), args[1].toLong()).toDouble()
            "dms" -> {
                // dms(deg, min, sec) -> decimal degrees
                val d = args.getOrElse(0) { 0.0 }
                val m = args.getOrElse(1) { 0.0 }
                val s = args.getOrElse(2) { 0.0 }
                d + m / 60.0 + s / 3600.0
            }
            else -> throw IllegalArgumentException("Hàm không được hỗ trợ: $func")
        }
    }

    private fun toRad(angle: Double): Double {
        return when (env.angleUnit) {
            AngleUnit.DEG -> Math.toRadians(angle)
            AngleUnit.RAD -> angle
            AngleUnit.GRA -> angle * PI / 200.0
        }
    }

    private fun fromRad(rad: Double): Double {
        return when (env.angleUnit) {
            AngleUnit.DEG -> Math.toDegrees(rad)
            AngleUnit.RAD -> rad
            AngleUnit.GRA -> rad * 200.0 / PI
        }
    }

    private fun factorial(n: Long): Long {
        if (n < 0 || n > 20) throw ArithmeticException("Giai thừa n! ngoài phạm vi hợp lệ (0..20)")
        var res = 1L
        for (i in 2..n) res *= i
        return res
    }

    private fun permutation(n: Long, r: Long): Long {
        if (r < 0 || r > n) return 0L
        var res = 1L
        for (i in (n - r + 1)..n) res *= i
        return res
    }
}
