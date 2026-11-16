package com.example.math

object CalculatorFormulaSanitizer {

    /**
     * Extracts all calculable formulas from AI response text.
     * Replaces variable letters (like x =, y =, a, b, c), LaTeX symbols (\cdot, \frac, \sqrt),
     * and splits ± into separate calculable formulas with concrete numbers.
     */
    fun extractFormulas(text: String): List<String> {
        if (text.isBlank()) return emptyList()

        val results = mutableListOf<String>()

        // 1. Match lines mentioning "Công thức bấm máy" or "Bấm máy"
        val lineRegex = Regex(
            """(?:Công thức bấm máy(?:\s*\d+)?(?:\s*TN-001)?|Bấm máy(?:\s*\d+)?|TN-001)\s*[:=]\s*`?([^\n`]+)`?""",
            RegexOption.IGNORE_CASE
        )

        for (match in lineRegex.findAll(text)) {
            val raw = match.groups[1]?.value?.trim() ?: continue
            val cleanedList = sanitizeAndExpand(raw)
            results.addAll(cleanedList)
        }

        // 2. If no explicit "Công thức bấm máy" prefix was found, search for display math formulas
        if (results.isEmpty()) {
            val mathLineRegex = Regex("""\$\$([^\$]+)\$\$""")
            for (match in mathLineRegex.findAll(text)) {
                val candidate = match.groups[1]?.value?.trim() ?: continue
                if (candidate.contains("+") || candidate.contains("-") || candidate.contains("*") || candidate.contains("/")) {
                    val cleanedList = sanitizeAndExpand(candidate)
                    results.addAll(cleanedList)
                    if (results.size >= 2) break
                }
            }
        }

        return results.distinct().filter { it.isNotBlank() && hasDigits(it) }
    }

    /**
     * Sanitizes a formula string so that it consists exclusively of numbers,
     * parentheses, and operators that MathEvaluator can calculate.
     */
    fun sanitize(raw: String): String {
        return sanitizeAndExpand(raw).firstOrNull() ?: cleanSingleFormula(raw)
    }

    private fun sanitizeAndExpand(raw: String): List<String> {
        val base = cleanSingleFormula(raw)
        if (base.isEmpty()) return emptyList()

        // If the formula contains ± or \pm, expand into 2 distinct formulas (+ and -)
        if (base.contains("±") || base.contains("\\pm")) {
            val fPlus = cleanSingleFormula(base.replace("±", "+").replace("\\pm", "+"))
            val fMinus = cleanSingleFormula(base.replace("±", "-").replace("\\pm", "-"))
            return listOf(fPlus, fMinus).filter { it.isNotBlank() && hasDigits(it) }
        }

        // Check if multiple expressions are separated by comma or semicolon
        if (base.contains(";") || base.contains(" hoặc ", ignoreCase = true) || base.contains(" or ", ignoreCase = true)) {
            val parts = base.split(Regex(";| hoặc | or ", RegexOption.IGNORE_CASE))
            val cleanedParts = parts.map { cleanSingleFormula(it) }.filter { it.isNotBlank() && hasDigits(it) }
            if (cleanedParts.isNotEmpty()) return cleanedParts
        }

        return listOf(base)
    }

    private fun cleanSingleFormula(raw: String): String {
        var s = raw.trim()

        // Remove code backticks
        s = s.replace("`", "")

        // Remove trailing punctuation
        s = s.trimEnd('.', ';', ',', '!', ':', '"', '\'')

        // Strip prefixes like "x =", "x1 =", "x2 =", "y =", "Ans =", "Kết quả =", "f(x) ="
        s = s.replace(
            Regex("""^(?:x\d*|y\d*|z\d*|ans|kết\s*quả|nghiệm\s*x\d*|f\(x\))\s*[:=]\s*""", RegexOption.IGNORE_CASE),
            ""
        )

        // Convert LaTeX \frac{A}{B} to (A)/(B)
        val fracRegex = Regex("""\\frac\{([^{}]+)\}\{([^{}]+)\}""")
        while (fracRegex.containsMatchIn(s)) {
            s = fracRegex.replace(s) { m -> "(${m.groupValues[1]})/(${m.groupValues[2]})" }
        }

        // Convert LaTeX \sqrt{A} to sqrt(A)
        val sqrtRegex = Regex("""\\sqrt\{([^{}]+)\}""")
        while (sqrtRegex.containsMatchIn(s)) {
            s = sqrtRegex.replace(s) { m -> "sqrt(${m.groupValues[1]})" }
        }

        // Convert LaTeX operators & symbols
        s = s
            .replace("\\cdot", "*")
            .replace("\\times", "*")
            .replace("\\div", "/")
            .replace("\\left", "")
            .replace("\\right", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("$", "")
            .replace("{", "(")
            .replace("}", ")")
            .replace("[", "(")
            .replace("]", ")")
            .replace("²", "^2")
            .replace("³", "^3")

        // Remove any remaining equation lhs if "= " is present
        if (s.contains("=")) {
            val parts = s.split("=")
            // Pick the right-hand side or the side with arithmetic operations
            val rhs = parts.last().trim()
            val lhs = parts.first().trim()
            s = if (hasDigits(rhs) && (rhs.contains("+") || rhs.contains("-") || rhs.contains("*") || rhs.contains("/") || rhs.contains("sqrt"))) {
                rhs
            } else if (hasDigits(lhs)) {
                lhs
            } else {
                rhs
            }
        }

        // Strip leading equal or colon signs
        s = s.trimStart('=', ':').trim()

        return s
    }

    private fun hasDigits(text: String): Boolean {
        return text.any { it.isDigit() }
    }
}
