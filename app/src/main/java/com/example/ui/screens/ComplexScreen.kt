package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.components.KeyDomeButton
import com.example.ui.components.KeyDomeType

@Composable
fun ComplexScreen(
    angleUnit: AngleUnit,
    onSendToCalc: (String) -> Unit
) {
    var exprText by remember { mutableStateOf("") }
    var resultComplex by remember { mutableStateOf<ComplexNumber?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun evalComplex() {
        if (exprText.isBlank()) return
        errorMessage = null
        try {
            // Evaluates complex arithmetic: handles expressions with i
            // We can parse with MathEvaluator or direct operations
            val parsed = parseComplexExpression(exprText)
            resultComplex = parsed
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi tính toán số phức"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Output Card with Multiple Representations
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "BIỂU DIỄN SỐ PHỨC (COMPLEX FORMS)",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (resultComplex != null) {
                    val z = resultComplex!!
                    val isDeg = angleUnit == AngleUnit.DEG

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dạng đại số (a + bi):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(z.toCartesianString(), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Modun |z|:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(ComplexNumber.cleanDouble(z.modulus()), color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Argument arg(z):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        val argStr = if (isDeg) "${ComplexNumber.cleanDouble(z.argumentDeg())}°" else "${ComplexNumber.cleanDouble(z.argumentRad())} rad"
                        Text(argStr, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dạng cực (r ∠ θ):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(z.toPolarString(isDeg), color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Số phức liên hợp z̄:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(z.conjugate().toCartesianString(), color = Color(0xFFFB7185), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dạng lượng giác:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(z.toTrigString(isDeg), color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Nhập biểu thức số phức chứa đơn vị ảo 'i' (ví dụ: (3+4i)*(1-2i) hoặc 5∠45) rồi nhấn =",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        OutlinedTextField(
            value = exprText,
            onValueChange = { exprText = it },
            placeholder = { Text("Ví dụ: (2+3i)/(1-i) hoặc 4∠30", fontSize = 12.sp, color = Color(0xFF64748B)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Complex Specialized Quick Keys
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyDomeButton(
                label = "i",
                onClick = { exprText += "i" },
                keyType = KeyDomeType.FUNCTION,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
            KeyDomeButton(
                label = "∠",
                onClick = { exprText += "∠" },
                shiftLabel = "Polar",
                keyType = KeyDomeType.FUNCTION,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
            KeyDomeButton(
                label = "Conj",
                onClick = {
                    if (resultComplex != null) {
                        resultComplex = resultComplex!!.conjugate()
                    } else {
                        exprText = "conj($exprText)"
                    }
                },
                keyType = KeyDomeType.FUNCTION,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
            KeyDomeButton(
                label = "DEL",
                onClick = { if (exprText.isNotEmpty()) exprText = exprText.dropLast(1) },
                keyType = KeyDomeType.ACTION_CLEAR,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
            KeyDomeButton(
                label = "AC",
                onClick = {
                    exprText = ""
                    resultComplex = null
                    errorMessage = null
                },
                keyType = KeyDomeType.ACTION_CLEAR,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fast Preset & Sample Complex Numbers
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyDomeButton(
                label = "(3+4i)",
                onClick = { exprText += "(3+4i)" },
                keyType = KeyDomeType.FUNCTION,
                modifier = Modifier.weight(1f),
                height = 38.dp
            )
            KeyDomeButton(
                label = "(1-i)",
                onClick = { exprText += "(1-i)" },
                keyType = KeyDomeType.FUNCTION,
                modifier = Modifier.weight(1f),
                height = 38.dp
            )
            KeyDomeButton(
                label = "5∠45",
                onClick = { exprText += "5∠45" },
                keyType = KeyDomeType.FUNCTION,
                modifier = Modifier.weight(1f),
                height = 38.dp
            )
            KeyDomeButton(
                label = "=",
                onClick = { evalComplex() },
                keyType = KeyDomeType.EQUALS,
                modifier = Modifier.weight(1f),
                height = 38.dp
            )
        }
    }
}

private fun parseComplexExpression(input: String): ComplexNumber {
    val clean = input.trim().replace(" ", "")
    // Handle polar form: r∠theta
    if (clean.contains("∠")) {
        val parts = clean.split("∠")
        val r = parts[0].toDoubleOrNull() ?: 1.0
        val theta = parts[1].toDoubleOrNull() ?: 0.0
        val rad = Math.toRadians(theta)
        return ComplexNumber(r * kotlin.math.cos(rad), r * kotlin.math.sin(rad))
    }

    // Direct arithmetic parser for complex tokens: (a+bi)+(c+di), etc.
    // Replace i with standard variable or evaluate terms
    var current = ComplexNumber.ZERO
    var lastOp = '+'
    var i = 0

    // Simple robust tokenizer for complex expressions
    val terms = splitComplexTerms(clean)
    if (terms.isEmpty()) return ComplexNumber.ZERO

    var result = parseSingleComplex(terms[0].second)
    for (idx in 1 until terms.size) {
        val op = terms[idx].first
        val nextVal = parseSingleComplex(terms[idx].second)
        result = when (op) {
            '+' -> result + nextVal
            '-' -> result - nextVal
            '*' -> result * nextVal
            '/' -> result / nextVal
            else -> result
        }
    }
    return result
}

private fun splitComplexTerms(str: String): List<Pair<Char, String>> {
    val result = mutableListOf<Pair<Char, String>>()
    var depth = 0
    var currentOp = '+'
    val sb = StringBuilder()

    for (c in str) {
        if (c == '(') depth++
        else if (c == ')') depth--

        if (depth == 0 && (c == '+' || c == '-' || c == '*' || c == '/') && sb.isNotEmpty()) {
            result.add(Pair(currentOp, sb.toString()))
            currentOp = c
            sb.clear()
        } else {
            sb.append(c)
        }
    }
    if (sb.isNotEmpty()) {
        result.add(Pair(currentOp, sb.toString()))
    }
    return result
}

private fun parseSingleComplex(token: String): ComplexNumber {
    val clean = token.removePrefix("(").removeSuffix(")")
    if (clean == "i") return ComplexNumber.I
    if (clean == "-i") return ComplexNumber(0.0, -1.0)
    if (!clean.contains("i")) {
        return ComplexNumber(clean.toDoubleOrNull() ?: 0.0, 0.0)
    }

    // Form: a+bi or a-bi or bi
    val iIdx = clean.indexOf("i")
    val beforeI = clean.substring(0, iIdx)
    val lastSign = beforeI.lastIndexOfAny(charArrayOf('+', '-'))

    return if (lastSign <= 0) {
        // Just imaginary term: e.g. "3i" or "-4i"
        val coeff = if (beforeI.isEmpty() || beforeI == "+") 1.0 else if (beforeI == "-") -1.0 else beforeI.toDoubleOrNull() ?: 1.0
        ComplexNumber(0.0, coeff)
    } else {
        val reStr = beforeI.substring(0, lastSign)
        val imStr = beforeI.substring(lastSign)
        val re = reStr.toDoubleOrNull() ?: 0.0
        val im = if (imStr == "+") 1.0 else if (imStr == "-") -1.0 else imStr.toDoubleOrNull() ?: 1.0
        ComplexNumber(re, im)
    }
}
