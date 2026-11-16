package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.math.ComplexNumber
import com.example.math.MathEvaluator
import kotlin.math.abs

enum class VerifyRelation(val symbol: String, val title: String) {
    EQUAL("=", "Bằng nhau"),
    NOT_EQUAL("≠", "Khác nhau"),
    GREATER(">", "Lớn hơn"),
    LESS("<", "Nhỏ hơn"),
    GREATER_OR_EQUAL("≥", "Lớn hơn hoặc bằng"),
    LESS_OR_EQUAL("≤", "Nhỏ hơn hoặc bằng")
}

@Composable
fun VerifyScreen() {
    var leftExpr by remember { mutableStateOf("sin(30)^2 + cos(30)^2") }
    var rightExpr by remember { mutableStateOf("1") }
    var selectedRelation by remember { mutableStateOf(VerifyRelation.EQUAL) }

    var isTrue by remember { mutableStateOf<Boolean?>(null) }
    var leftVal by remember { mutableStateOf<Double?>(null) }
    var rightVal by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val evaluator = remember { MathEvaluator() }

    fun checkVerify() {
        errorMessage = null
        try {
            val lv = evaluator.evaluate(leftExpr)
            val rv = evaluator.evaluate(rightExpr)
            leftVal = lv
            rightVal = rv

            val diff = lv - rv
            val ok = when (selectedRelation) {
                VerifyRelation.EQUAL -> abs(diff) < 1e-10
                VerifyRelation.NOT_EQUAL -> abs(diff) >= 1e-10
                VerifyRelation.GREATER -> lv > rv + 1e-10
                VerifyRelation.LESS -> lv < rv - 1e-10
                VerifyRelation.GREATER_OR_EQUAL -> lv >= rv - 1e-10
                VerifyRelation.LESS_OR_EQUAL -> lv <= rv + 1e-10
            }
            isTrue = ok
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi biểu thức kiểm tra"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("KIỂM TRA TÍNH ĐÚNG / SAI (VERIFY)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = leftExpr,
                    onValueChange = { leftExpr = it },
                    label = { Text("Vế trái (LHS)", color = Color(0xFFFBBF24), fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Relations Row
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    VerifyRelation.entries.forEach { rel ->
                        val isSel = selectedRelation == rel
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF0284C7) else Color(0xFF0F172A))
                                .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedRelation = rel
                                    isTrue = null
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(rel.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rightExpr,
                    onValueChange = { rightExpr = it },
                    label = { Text("Vế phải (RHS)", color = Color(0xFF38BDF8), fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { checkVerify() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("KIỂM TRA (VERIFY)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result Card: TRUE or FALSE Banner
        if (isTrue != null) {
            val pass = isTrue!!
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (pass) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (pass) Color(0xFF34D399) else Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (pass) "TRUE (ĐÚNG)" else "FALSE (SAI)",
                        color = if (pass) Color(0xFF34D399) else Color(0xFFF87171),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (leftVal != null && rightVal != null) {
                        Text(
                            text = "LHS = ${ComplexNumber.cleanDouble(leftVal!!)}  ${selectedRelation.symbol}  RHS = ${ComplexNumber.cleanDouble(rightVal!!)}",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
