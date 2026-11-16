package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.math.MathEvaluator
import com.example.ui.components.KeyDomeType
import com.example.ui.components.RoundKeyDomeButton
import com.example.ui.components.SpotlightTargetKey

@Composable
fun CalculateScreen(
    evaluator: MathEvaluator,
    expression: String,
    resultStr: String,
    cursorPosition: Int,
    isShiftActive: Boolean,
    isAlphaActive: Boolean,
    exactFraction: String?,
    isDarkTheme: Boolean = true,
    isOneHanded: Boolean = false,
    isRightHanded: Boolean = true,
    onInsertToken: (String) -> Unit,
    onEvaluate: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleAlpha: () -> Unit,
    onToggleHomeMenu: () -> Unit,
    onCursorLeft: () -> Unit,
    onCursorRight: () -> Unit,
    onCursorUp: () -> Unit,
    onCursorDown: () -> Unit,
    onOpenSolveDialog: () -> Unit,
    onOpenCalcDialog: () -> Unit,
    onOpenFactDialog: () -> Unit,
    onOpenGcdLcmDialog: () -> Unit,
    onOpenSumProdDialog: () -> Unit,
    onOpenVarList: () -> Unit,
    onClearAll: () -> Unit,
    onDeleteChar: () -> Unit,
    onToggleSd: () -> Unit,
    onUpdateExpressionAndCursor: (String, Int) -> Unit,
    onReportTargetBounds: (SpotlightTargetKey, Rect) -> Unit = { _, _ -> }
) {
    fun appendToken(normal: String, shift: String? = null, alpha: String? = null) {
        val token = when {
            isShiftActive && shift != null -> {
                onToggleShift()
                shift
            }
            isAlphaActive && alpha != null -> {
                onToggleAlpha()
                alpha
            }
            else -> normal
        }
        onInsertToken(token)
    }

    fun insertFraction() {
        if (isShiftActive) {
            onToggleShift()
            onToggleSd()
            return
        }
        val cur = cursorPosition.coerceIn(0, expression.length)
        val before = expression.substring(0, cur)
        val after = expression.substring(cur)
        val opStart = com.example.ui.components.NaturalMathParser.findOperandStart(before)
        val operand = before.substring(opStart).trim()

        if (operand.isNotEmpty() && !operand.endsWith("+") && !operand.endsWith("−") &&
            !operand.endsWith("×") && !operand.endsWith("÷") && !operand.endsWith("/") &&
            !operand.endsWith("(") && !operand.endsWith("^")
        ) {
            val prefix = before.substring(0, opStart)
            val newExpr = "$prefix($operand)/()$after"
            val newCur = prefix.length + operand.length + 4 // inside denominator ()
            onUpdateExpressionAndCursor(newExpr, newCur)
        } else {
            val newExpr = "$before()/()$after"
            val newCur = before.length + 1 // inside numerator ()
            onUpdateExpressionAndCursor(newExpr, newCur)
        }
    }

    fun insertSqrt() {
        val cur = cursorPosition.coerceIn(0, expression.length)
        val before = expression.substring(0, cur)
        val after = expression.substring(cur)
        if (isShiftActive) {
            onToggleShift()
            val newExpr = "${before}root()$after"
            onUpdateExpressionAndCursor(newExpr, before.length + 5)
        } else {
            val newExpr = "${before}sqrt()$after"
            onUpdateExpressionAndCursor(newExpr, before.length + 5)
        }
    }

    fun insertSquare() {
        val cur = cursorPosition.coerceIn(0, expression.length)
        val before = expression.substring(0, cur)
        val after = expression.substring(cur)
        val trimmed = before.trimEnd()
        val needsBase = trimmed.isEmpty() || trimmed.last() in "+-−×÷/(^"
        if (isShiftActive) {
            onToggleShift()
            val token = if (needsBase) "x^3" else "^3"
            val newExpr = "$before$token$after"
            onUpdateExpressionAndCursor(newExpr, cur + token.length)
        } else {
            val token = if (needsBase) "x^2" else "^2"
            val newExpr = "$before$token$after"
            onUpdateExpressionAndCursor(newExpr, cur + token.length)
        }
    }

    fun insertPower() {
        val cur = cursorPosition.coerceIn(0, expression.length)
        val before = expression.substring(0, cur)
        val after = expression.substring(cur)
        val trimmed = before.trimEnd()
        val needsBase = trimmed.isEmpty() || trimmed.last() in "+-−×÷/(^"

        if (isShiftActive) {
            onToggleShift()
            val newExpr = "${before}root()$after"
            onUpdateExpressionAndCursor(newExpr, before.length + 5)
        } else {
            if (needsBase) {
                val newExpr = "${before}x^()$after"
                onUpdateExpressionAndCursor(newExpr, before.length + 3)
            } else {
                val newExpr = "$before^()$after"
                onUpdateExpressionAndCursor(newExpr, before.length + 2)
            }
        }
    }

    Column(
        horizontalAlignment = if (!isOneHanded) Alignment.CenterHorizontally else if (isRightHanded) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = if (isOneHanded && isRightHanded) 28.dp else 4.dp,
                end = if (isOneHanded && !isRightHanded) 28.dp else 4.dp,
                top = 2.dp,
                bottom = 2.dp
            )
    ) {
        // 1. Navigation & Quick Control Row (6 evenly spaced, perfectly symmetrical buttons)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(0.95f)
                .fillMaxWidth()
                .padding(vertical = 1.dp)
                .onGloballyPositioned { onReportTargetBounds(SpotlightTargetKey.DPAD_KEYS, it.boundsInRoot()) }
        ) {
            RoundKeyDomeButton(
                label = "⌂",
                onClick = onToggleHomeMenu,
                keyType = KeyDomeType.HOME,
                fontSize = 17,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            RoundKeyDomeButton(
                label = "▲",
                onClick = onCursorUp,
                keyType = KeyDomeType.DPAD,
                fontSize = 13,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            RoundKeyDomeButton(
                label = "◀",
                onClick = onCursorLeft,
                keyType = KeyDomeType.DPAD,
                fontSize = 13,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            RoundKeyDomeButton(
                label = "OK",
                onClick = onEvaluate,
                keyType = KeyDomeType.EQUALS,
                fontSize = 13,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1.1f).fillMaxHeight()
            )
            RoundKeyDomeButton(
                label = "▶",
                onClick = onCursorRight,
                keyType = KeyDomeType.DPAD,
                fontSize = 13,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            RoundKeyDomeButton(
                label = "▼",
                onClick = onCursorDown,
                keyType = KeyDomeType.DPAD,
                fontSize = 13,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // 2. Primary Function Row (6 buttons evenly spaced with SHIFT and ALPHA)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(0.95f)
                .fillMaxWidth()
                .padding(vertical = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .onGloballyPositioned { onReportTargetBounds(SpotlightTargetKey.SHIFT_ALPHA, it.boundsInRoot()) },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RoundKeyDomeButton(
                    label = "SHIFT",
                    onClick = onToggleShift,
                    keyType = KeyDomeType.SHIFT,
                    fontSize = 10,
                    isDarkTheme = isDarkTheme,
                    isActive = isShiftActive,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RoundKeyDomeButton(
                    label = "ALPHA",
                    onClick = onToggleAlpha,
                    keyType = KeyDomeType.ALPHA,
                    fontSize = 10,
                    isDarkTheme = isDarkTheme,
                    isActive = isAlphaActive,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            RoundKeyDomeButton(
                label = "x=",
                onClick = onOpenVarList,
                shiftLabel = "VARIABLE",
                keyType = KeyDomeType.FUNCTION,
                fontSize = 11,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            RoundKeyDomeButton(
                label = "f(x)",
                onClick = onOpenCalcDialog,
                shiftLabel = "FUNCTION",
                keyType = KeyDomeType.FUNCTION,
                fontSize = 11,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            Row(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .onGloballyPositioned { onReportTargetBounds(SpotlightTargetKey.CAT_TOOLS, it.boundsInRoot()) },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RoundKeyDomeButton(
                    label = "CAT",
                    onClick = onOpenFactDialog,
                    shiftLabel = "CATALOG",
                    keyType = KeyDomeType.FUNCTION,
                    fontSize = 11,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RoundKeyDomeButton(
                    label = "TOOLS",
                    onClick = onOpenSolveDialog,
                    shiftLabel = "SOLVE",
                    keyType = KeyDomeType.FUNCTION,
                    fontSize = 10,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        // 3 & 4. Scientific Rows Column
        Column(
            modifier = Modifier
                .weight(1.7f)
                .fillMaxWidth()
                .onGloballyPositioned { onReportTargetBounds(SpotlightTargetKey.SCIENTIFIC_KEYS, it.boundsInRoot()) }
        ) {
            // Scientific Row 1 (7 buttons evenly spaced)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                RoundKeyDomeButton(label = "x", onClick = { appendToken("x", "y", "F") }, shiftLabel = "y", alphaLabel = "F", fontSize = 13, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "a/b", onClick = { insertFraction() }, shiftLabel = "d/c", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "√", onClick = { insertSqrt() }, shiftLabel = "ⁿ√", fontSize = 14, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "x²", onClick = { insertSquare() }, shiftLabel = "x³", fontSize = 12, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "x^■", onClick = { insertPower() }, shiftLabel = "ⁿ√x", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "log", onClick = { appendToken("log(", "10^") }, shiftLabel = "10^x", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "ln", onClick = { appendToken("ln(", "e^") }, shiftLabel = "e^x", fontSize = 12, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            }

            // Scientific Row 2 (7 buttons evenly spaced)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                RoundKeyDomeButton(label = "Ans", onClick = { appendToken("Ans", "PreAns") }, shiftLabel = "PreAns", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "sin", onClick = { appendToken("sin(", "asin(", "A") }, shiftLabel = "sin⁻¹", alphaLabel = "A", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "cos", onClick = { appendToken("cos(", "acos(", "B") }, shiftLabel = "cos⁻¹", alphaLabel = "B", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "tan", onClick = { appendToken("tan(", "atan(", "C") }, shiftLabel = "tan⁻¹", alphaLabel = "C", fontSize = 11, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "(", onClick = { appendToken("(", "%", "D") }, shiftLabel = "%", alphaLabel = "D", fontSize = 13, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = ")", onClick = { appendToken(")", ",", "E") }, shiftLabel = ",", alphaLabel = "E", fontSize = 13, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "∫", onClick = { appendToken("∫(", "d/dx(") }, shiftLabel = "d/dx", fontSize = 13, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }

        // 5. Numeric Row 1: [7] [8] [9] [DEL] [AC]
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            RoundKeyDomeButton(label = "7", onClick = { appendToken("7") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "8", onClick = { appendToken("8") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "9", onClick = { appendToken("9") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            Row(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .onGloballyPositioned { onReportTargetBounds(SpotlightTargetKey.DEL_AC_KEYS, it.boundsInRoot()) },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RoundKeyDomeButton(label = "⌫", onClick = onDeleteChar, keyType = KeyDomeType.ACTION_CLEAR, shiftLabel = "INS", fontSize = 19, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "AC", onClick = onClearAll, keyType = KeyDomeType.ACTION_CLEAR, shiftLabel = "OFF", fontSize = 16, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }

        // 6. Numeric Row 2: [4] [5] [6] [×] [÷]
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            RoundKeyDomeButton(label = "4", onClick = { appendToken("4") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "5", onClick = { appendToken("5") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "6", onClick = { appendToken("6") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "×", onClick = { appendToken("×", "nPr(") }, shiftLabel = "nPr", keyType = KeyDomeType.OPERATION, fontSize = 22, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "÷", onClick = { appendToken("÷", "R") }, shiftLabel = "R (mod)", keyType = KeyDomeType.OPERATION, fontSize = 22, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
        }

        // 7. Numeric Row 3: [1] [2] [3] [+] [−]
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            RoundKeyDomeButton(label = "1", onClick = { appendToken("1") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "2", onClick = { appendToken("2") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "3", onClick = { appendToken("3") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "+", onClick = { appendToken("+", "nCr(") }, shiftLabel = "nCr", keyType = KeyDomeType.OPERATION, fontSize = 22, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "−", onClick = { appendToken("−", "Ran#") }, shiftLabel = "Ran#", keyType = KeyDomeType.OPERATION, fontSize = 22, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
        }

        // 8. Numeric Row 4: [0] [•] [×10ˣ] [S⇔D] [=]
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            RoundKeyDomeButton(label = "0", onClick = { appendToken("0") }, keyType = KeyDomeType.NUMBER, fontSize = 21, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "•", onClick = { appendToken(".", "RanInt(") }, shiftLabel = "RanInt", keyType = KeyDomeType.NUMBER, fontSize = 22, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            RoundKeyDomeButton(label = "×10ˣ", onClick = { appendToken("*10^", "π") }, shiftLabel = "π", keyType = KeyDomeType.FUNCTION, fontSize = 12, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            Row(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .onGloballyPositioned { onReportTargetBounds(SpotlightTargetKey.EQUALS_SD_KEYS, it.boundsInRoot()) },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RoundKeyDomeButton(label = "S⇔D", onClick = onToggleSd, shiftLabel = "FORMAT", keyType = KeyDomeType.FUNCTION, fontSize = 12, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
                RoundKeyDomeButton(label = "=", onClick = onEvaluate, shiftLabel = "EXE", keyType = KeyDomeType.EQUALS, fontSize = 20, isDarkTheme = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}
