package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class MathSegment {
    data class Plain(
        val text: String,
        val startIndex: Int,
        val endIndex: Int
    ) : MathSegment()

    data class Fraction(
        val numerator: String,
        val denominator: String,
        val startIndex: Int,
        val numStart: Int,
        val numEnd: Int,
        val denStart: Int,
        val denEnd: Int,
        val endIndex: Int
    ) : MathSegment()

    data class Radical(
        val inner: String,
        val startIndex: Int,
        val innerStart: Int,
        val innerEnd: Int,
        val endIndex: Int
    ) : MathSegment()

    data class NthRoot(
        val index: String,
        val inner: String,
        val startIndex: Int,
        val innerStart: Int,
        val innerEnd: Int,
        val endIndex: Int
    ) : MathSegment()

    data class Power(
        val base: String,
        val exponent: String,
        val startIndex: Int,
        val expStart: Int,
        val expEnd: Int,
        val endIndex: Int
    ) : MathSegment()
}

object NaturalMathParser {

    private data class SubExprResult(
        val contentStart: Int,
        val contentEnd: Int,
        val text: String,
        val endIndex: Int
    )

    fun parse(expr: String): List<MathSegment> {
        if (expr.isEmpty()) return emptyList()

        val segments = mutableListOf<MathSegment>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val oldI = i

            // 1. Check for Square Root: sqrt(...)
            if (expr.startsWith("sqrt(", i)) {
                val openParen = i + 4
                val closeParen = findMatchingParen(expr, openParen)
                val innerStart = openParen + 1
                val innerEnd = if (closeParen != -1) closeParen else len
                val inner = expr.substring(innerStart, innerEnd)
                val end = if (closeParen != -1) closeParen + 1 else len
                segments.add(
                    MathSegment.Radical(
                        inner = inner,
                        startIndex = i,
                        innerStart = innerStart,
                        innerEnd = innerEnd,
                        endIndex = end
                    )
                )
                i = maxOf(i + 5, end)
                continue
            }

            // 2. Check for N-th Root: root(...) or ⁿ√(...)
            if (expr.startsWith("root(", i) || expr.startsWith("ⁿ√(", i)) {
                val prefixLen = if (expr.startsWith("root(", i)) 4 else 2
                val openParen = i + prefixLen
                val closeParen = findMatchingParen(expr, openParen)
                val innerStart = openParen + 1
                val innerEnd = if (closeParen != -1) closeParen else len
                val fullInner = expr.substring(innerStart, innerEnd)
                val end = if (closeParen != -1) closeParen + 1 else len

                val commaIdx = fullInner.indexOf(',')
                val degree = if (commaIdx != -1) fullInner.substring(0, commaIdx).trim() else "n"
                val radicand = if (commaIdx != -1) fullInner.substring(commaIdx + 1).trim() else fullInner.trim()

                segments.add(
                    MathSegment.NthRoot(
                        index = degree.ifEmpty { "n" },
                        inner = radicand,
                        startIndex = i,
                        innerStart = innerStart,
                        innerEnd = innerEnd,
                        endIndex = end
                    )
                )
                i = maxOf(i + prefixLen + 1, end)
                continue
            }

            // 3. Check for Fraction: (num)/(den) or (num)/den
            if (expr[i] == '(') {
                val closeP1 = findMatchingParen(expr, i)
                if (closeP1 != -1 && closeP1 + 1 < len && expr[closeP1 + 1] == '/') {
                    val slashIdx = closeP1 + 1
                    val num = expr.substring(i + 1, closeP1)
                    val (denStart, denEnd, denText, endIdx) = parseDenominator(expr, slashIdx + 1)
                    segments.add(
                        MathSegment.Fraction(
                            numerator = num,
                            denominator = denText,
                            startIndex = i,
                            numStart = i + 1,
                            numEnd = closeP1,
                            denStart = denStart,
                            denEnd = denEnd,
                            endIndex = endIdx
                        )
                    )
                    i = maxOf(slashIdx + 1, endIdx)
                    continue
                }
            }

            // 4. Check for Fraction: operand/operand
            if (expr[i] == '/') {
                val prevSeg = segments.lastOrNull()
                val numText: String
                val globalNumStart: Int
                if (prevSeg is MathSegment.Plain) {
                    val prevText = prevSeg.text
                    val localStart = findOperandStart(prevText)
                    val beforeNumerator = prevText.substring(0, localStart)
                    numText = prevText.substring(localStart)
                    segments.removeAt(segments.size - 1)
                    if (beforeNumerator.isNotEmpty()) {
                        segments.add(MathSegment.Plain(beforeNumerator, prevSeg.startIndex, prevSeg.startIndex + localStart))
                    }
                    globalNumStart = prevSeg.startIndex + localStart
                } else {
                    numText = ""
                    globalNumStart = i
                }

                val (denStart, denEnd, denText, endIdx) = parseDenominator(expr, i + 1)
                segments.add(
                    MathSegment.Fraction(
                        numerator = numText,
                        denominator = denText,
                        startIndex = globalNumStart,
                        numStart = globalNumStart,
                        numEnd = i,
                        denStart = denStart,
                        denEnd = denEnd,
                        endIndex = endIdx
                    )
                )
                i = maxOf(i + 1, endIdx)
                continue
            }

            // 5. Check for Power: base^(exp) or base^exp
            if (expr[i] == '^') {
                val prevSeg = segments.lastOrNull()
                val baseText: String
                val baseStart: Int
                if (prevSeg is MathSegment.Plain) {
                    val prevText = prevSeg.text
                    val localBaseStart = findOperandStart(prevText)
                    val beforeBase = prevText.substring(0, localBaseStart)
                    baseText = prevText.substring(localBaseStart)
                    segments.removeAt(segments.size - 1)
                    if (beforeBase.isNotEmpty()) {
                        segments.add(MathSegment.Plain(beforeBase, prevSeg.startIndex, prevSeg.startIndex + localBaseStart))
                    }
                    baseStart = prevSeg.startIndex + localBaseStart
                } else {
                    baseText = ""
                    baseStart = i
                }

                val (expStart, expEnd, expText, endIdx) = parseExponent(expr, i + 1)
                segments.add(
                    MathSegment.Power(
                        base = baseText,
                        exponent = expText,
                        startIndex = baseStart,
                        expStart = expStart,
                        expEnd = expEnd,
                        endIndex = endIdx
                    )
                )
                i = maxOf(i + 1, endIdx)
                continue
            }

            // 6. Accumulate plain characters
            val startPlain = i
            while (i < len && expr[i] != '/' && expr[i] != '^' && !expr.startsWith("sqrt(", i) && !expr.startsWith("root(", i) && !expr.startsWith("ⁿ√(", i)) {
                if (expr[i] == '(') {
                    val closeP1 = findMatchingParen(expr, i)
                    if (closeP1 != -1 && closeP1 + 1 < len && expr[closeP1 + 1] == '/') {
                        break
                    }
                }
                i++
            }
            if (i == startPlain) {
                i++
            }
            if (i > startPlain) {
                segments.add(MathSegment.Plain(expr.substring(startPlain, i), startPlain, i))
            }

            if (i <= oldI) {
                i = oldI + 1
            }
        }

        return segments
    }

    private fun parseDenominator(expr: String, start: Int): SubExprResult {
        if (start >= expr.length) return SubExprResult(start, start, "", start)
        if (expr[start] == '(') {
            val closeP = findMatchingParen(expr, start)
            val contentStart = start + 1
            val contentEnd = if (closeP != -1) closeP else expr.length
            val end = if (closeP != -1) closeP + 1 else expr.length
            return SubExprResult(contentStart, contentEnd, expr.substring(contentStart, contentEnd), end)
        }
        val end = findOperandEnd(expr, start)
        return SubExprResult(start, end, expr.substring(start, end), end)
    }

    private fun parseExponent(expr: String, start: Int): SubExprResult {
        if (start >= expr.length) return SubExprResult(start, start, "", start)
        if (expr[start] == '(') {
            val closeP = findMatchingParen(expr, start)
            val contentStart = start + 1
            val contentEnd = if (closeP != -1) closeP else expr.length
            val end = if (closeP != -1) closeP + 1 else expr.length
            return SubExprResult(contentStart, contentEnd, expr.substring(contentStart, contentEnd), end)
        }
        val end = findOperandEnd(expr, start)
        return SubExprResult(start, end, expr.substring(start, end), end)
    }

    private fun findMatchingParen(expr: String, openPos: Int): Int {
        var depth = 1
        var j = openPos + 1
        while (j < expr.length) {
            if (expr[j] == '(') depth++
            else if (expr[j] == ')') {
                depth--
                if (depth == 0) return j
            }
            j++
        }
        return -1
    }

    fun findOperandStart(text: String): Int {
        if (text.isEmpty()) return 0
        if (text.endsWith(")")) {
            var depth = 1
            var j = text.length - 2
            while (j >= 0) {
                if (text[j] == ')') depth++
                else if (text[j] == '(') {
                    depth--
                    if (depth == 0) return j
                }
                j--
            }
            return 0
        }
        var j = text.length - 1
        while (j >= 0 && (text[j].isLetterOrDigit() || text[j] == '.')) {
            j--
        }
        return (j + 1).coerceIn(0, text.length)
    }

    private fun findOperandEnd(expr: String, start: Int): Int {
        if (start >= expr.length) return expr.length
        if (expr[start] == '(') {
            val closeP = findMatchingParen(expr, start)
            return if (closeP != -1) closeP + 1 else expr.length
        }
        var j = start
        while (j < expr.length && (expr[j].isLetterOrDigit() || expr[j] == '.')) {
            j++
        }
        return j.coerceIn(start, expr.length)
    }
}

fun formatNaturalSymbols(text: String): String {
    return text
        .replace("asin(", "sin⁻¹(")
        .replace("acos(", "cos⁻¹(")
        .replace("atan(", "tan⁻¹(")
        .replace("asin", "sin⁻¹")
        .replace("acos", "cos⁻¹")
        .replace("atan", "tan⁻¹")
        .replace("root(", "ⁿ√(")
        .replace("root", "ⁿ√")
        .replace("sqrt(", "√(")
        .replace("sqrt", "√")
        .replace("cbrt(", "∛(")
        .replace("cbrt", "∛")
        .replace("d/dx(", "d/dx(")
        .replace("nPr(", " P ")
        .replace("nCr(", " C ")
        .replace("nPr", " P ")
        .replace("nCr", " C ")
        .replace("*10^", "×10^")
        .replace("×10^", "×10^")
        .replace("*", "×")
        .replace("-", "−")
        .replace("pi", "π")
        .replace("ranint", "RanInt")
        .replace("Ran#", "Ran#")
        .replace("ran", "Ran#")
}

@Composable
fun NaturalMathDisplay(
    expression: String,
    cursorPosition: Int,
    contentColor: Color,
    contrastAlpha: Float,
    cursorAlpha: Float,
    modifier: Modifier = Modifier
) {
    if (expression.isEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            BlinkingCursor(contentColor, cursorAlpha, contrastAlpha, size = 18)
        }
        return
    }

    val segments = remember(expression) {
        try {
            NaturalMathParser.parse(expression)
        } catch (e: Exception) {
            emptyList()
        }
    }
    val cur = cursorPosition.coerceIn(0, expression.length)

    if (segments.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            var cursorRendered = false

            segments.forEach { segment ->
                when (segment) {
                    is MathSegment.Plain -> {
                        val inRange = cur in segment.startIndex..segment.endIndex
                        if (inRange) {
                            cursorRendered = true
                            RenderWithCursor(
                                text = segment.text,
                                localCur = cur - segment.startIndex,
                                contentColor = contentColor,
                                contrastAlpha = contrastAlpha,
                                cursorAlpha = cursorAlpha,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Medium,
                                cursorSize = 18
                            )
                        } else {
                            Text(
                                text = formatNaturalSymbols(segment.text),
                                color = contentColor.copy(alpha = contrastAlpha),
                                fontSize = 21.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    is MathSegment.Fraction -> {
                        val isCurNum = cur in segment.numStart..segment.numEnd
                        val isCurDen = cur in segment.denStart..segment.denEnd

                        if (isCurNum || isCurDen) {
                            cursorRendered = true
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        ) {
                            // 1. Numerator
                            Box(contentAlignment = Alignment.Center) {
                                if (isCurNum) {
                                    RenderWithCursor(
                                        text = segment.numerator,
                                        localCur = cur - segment.numStart,
                                        contentColor = contentColor,
                                        contrastAlpha = contrastAlpha,
                                        cursorAlpha = cursorAlpha,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        cursorSize = 13
                                    )
                                } else {
                                    Text(
                                        text = formatNaturalSymbols(segment.numerator.ifEmpty { "□" }),
                                        color = contentColor.copy(alpha = if (segment.numerator.isEmpty()) contrastAlpha * 0.45f else contrastAlpha),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // 2. Solid Fraction Division Bar
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 22.dp)
                                    .height(1.8.dp)
                                    .background(contentColor.copy(alpha = contrastAlpha))
                            )

                            // 3. Denominator
                            Box(contentAlignment = Alignment.Center) {
                                if (isCurDen) {
                                    RenderWithCursor(
                                        text = segment.denominator,
                                        localCur = cur - segment.denStart,
                                        contentColor = contentColor,
                                        contrastAlpha = contrastAlpha,
                                        cursorAlpha = cursorAlpha,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        cursorSize = 13
                                    )
                                } else {
                                    Text(
                                        text = formatNaturalSymbols(segment.denominator.ifEmpty { "□" }),
                                        color = contentColor.copy(alpha = if (segment.denominator.isEmpty()) contrastAlpha * 0.45f else contrastAlpha),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    is MathSegment.Radical -> {
                        val isCurIn = cur in segment.innerStart..segment.innerEnd
                        if (isCurIn) cursorRendered = true

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = "√",
                                color = contentColor.copy(alpha = contrastAlpha),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .drawBehind {
                                        drawLine(
                                            color = contentColor.copy(alpha = contrastAlpha),
                                            start = Offset(0f, 2f),
                                            end = Offset(size.width, 2f),
                                            strokeWidth = 2.5f
                                        )
                                    }
                                    .padding(top = 4.dp, start = 2.dp, end = 2.dp)
                            ) {
                                if (isCurIn) {
                                    RenderWithCursor(
                                        text = segment.inner,
                                        localCur = cur - segment.innerStart,
                                        contentColor = contentColor,
                                        contrastAlpha = contrastAlpha,
                                        cursorAlpha = cursorAlpha,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        cursorSize = 14
                                    )
                                } else {
                                    Text(
                                        text = formatNaturalSymbols(segment.inner.ifEmpty { "□" }),
                                        color = contentColor.copy(alpha = if (segment.inner.isEmpty()) contrastAlpha * 0.45f else contrastAlpha),
                                        fontSize = 17.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    is MathSegment.NthRoot -> {
                        val isCurIn = cur in segment.innerStart..segment.innerEnd
                        if (isCurIn) cursorRendered = true

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = formatNaturalSymbols(segment.index),
                                color = contentColor.copy(alpha = contrastAlpha),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Text(
                                text = "√",
                                color = contentColor.copy(alpha = contrastAlpha),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .drawBehind {
                                        drawLine(
                                            color = contentColor.copy(alpha = contrastAlpha),
                                            start = Offset(0f, 2f),
                                            end = Offset(size.width, 2f),
                                            strokeWidth = 2.5f
                                        )
                                    }
                                    .padding(top = 4.dp, start = 2.dp, end = 2.dp)
                            ) {
                                if (isCurIn) {
                                    RenderWithCursor(
                                        text = segment.inner,
                                        localCur = cur - segment.innerStart,
                                        contentColor = contentColor,
                                        contrastAlpha = contrastAlpha,
                                        cursorAlpha = cursorAlpha,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        cursorSize = 14
                                    )
                                } else {
                                    Text(
                                        text = formatNaturalSymbols(segment.inner.ifEmpty { "□" }),
                                        color = contentColor.copy(alpha = if (segment.inner.isEmpty()) contrastAlpha * 0.45f else contrastAlpha),
                                        fontSize = 17.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    is MathSegment.Power -> {
                        val isCurExp = cur in segment.expStart..segment.expEnd
                        if (isCurExp) cursorRendered = true

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(horizontal = 1.dp)
                        ) {
                            Text(
                                text = formatNaturalSymbols(segment.base.ifEmpty { "□" }),
                                color = contentColor.copy(alpha = if (segment.base.isEmpty()) contrastAlpha * 0.45f else contrastAlpha),
                                fontSize = 21.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 9.dp)
                            ) {
                                if (isCurExp) {
                                    RenderWithCursor(
                                        text = segment.exponent,
                                        localCur = cur - segment.expStart,
                                        contentColor = contentColor,
                                        contrastAlpha = contrastAlpha,
                                        cursorAlpha = cursorAlpha,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        cursorSize = 11
                                    )
                                } else {
                                    Text(
                                        text = formatNaturalSymbols(segment.exponent.ifEmpty { "□" }),
                                        color = contentColor.copy(alpha = if (segment.exponent.isEmpty()) contrastAlpha * 0.45f else contrastAlpha),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!cursorRendered) {
                BlinkingCursor(contentColor, cursorAlpha, contrastAlpha)
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Text(
                text = formatNaturalSymbols(expression),
                color = contentColor.copy(alpha = contrastAlpha),
                fontSize = 21.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
            BlinkingCursor(contentColor, cursorAlpha, contrastAlpha)
        }
    }
}

@Composable
fun RenderWithCursor(
    text: String,
    localCur: Int,
    contentColor: Color,
    contrastAlpha: Float,
    cursorAlpha: Float,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Medium,
    cursorSize: Int = 16
) {
    val clamped = localCur.coerceIn(0, text.length)
    val before = text.substring(0, clamped)
    val after = text.substring(clamped)

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (before.isNotEmpty()) {
            Text(
                text = formatNaturalSymbols(before),
                color = contentColor.copy(alpha = contrastAlpha),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = fontWeight
            )
        }
        BlinkingCursor(contentColor, cursorAlpha, contrastAlpha, size = cursorSize)
        if (after.isNotEmpty()) {
            Text(
                text = formatNaturalSymbols(after),
                color = contentColor.copy(alpha = contrastAlpha),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = fontWeight
            )
        }
    }
}

@Composable
fun BlinkingCursor(
    color: Color,
    cursorAlpha: Float,
    contrastAlpha: Float,
    size: Int = 18
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 0.5.dp)
            .width(1.2.dp)
            .height((size * 1.1f).dp)
            .clip(RoundedCornerShape(0.6.dp))
            .background(color.copy(alpha = contrastAlpha * cursorAlpha))
    )
}
