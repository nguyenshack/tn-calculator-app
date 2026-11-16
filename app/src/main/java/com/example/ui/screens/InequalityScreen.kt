package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber
import com.example.math.InequalityResult
import com.example.math.InequalitySign
import com.example.math.InequalitySolver

@Composable
fun InequalityScreen() {
    var degree by remember { mutableIntStateOf(2) } // 2, 3, 4
    var selectedSign by remember { mutableStateOf(InequalitySign.GREATER) }

    var coeffA by remember { mutableStateOf("1") }
    var coeffB by remember { mutableStateOf("-3") }
    var coeffC by remember { mutableStateOf("2") }
    var coeffD by remember { mutableStateOf("0") }
    var coeffE by remember { mutableStateOf("0") }

    var result by remember { mutableStateOf<InequalityResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun solve() {
        errorMessage = null
        try {
            val a = coeffA.toDoubleOrNull() ?: 0.0
            val b = coeffB.toDoubleOrNull() ?: 0.0
            val c = coeffC.toDoubleOrNull() ?: 0.0
            val d = coeffD.toDoubleOrNull() ?: 0.0
            val e = coeffE.toDoubleOrNull() ?: 0.0

            result = when (degree) {
                2 -> InequalitySolver.solveDegree2(a, b, c, selectedSign)
                3 -> InequalitySolver.solveDegree3(a, b, c, d, selectedSign)
                4 -> InequalitySolver.solveDegree4(a, b, c, d, e, selectedSign)
                else -> null
            }
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Lỗi giải bất phương trình"
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
                Text("GIẢI BẤT PHƯƠNG TRÌNH ĐA THỨC BẬC 2, 3, 4", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Degree Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bậc (Degree):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(2, 3, 4).forEach { deg ->
                            val isSel = degree == deg
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF0F172A))
                                    .clickable { degree = deg }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Bậc $deg", color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Inequality Sign Selector: >, >=, <, <=
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    InequalitySign.entries.forEach { sign ->
                        val isSel = selectedSign == sign
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF0284C7) else Color(0xFF0F172A))
                                .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable { selectedSign = sign }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(sign.symbol + " 0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coefficients Input
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = coeffA, onValueChange = { coeffA = it }, label = { Text("a", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = coeffB, onValueChange = { coeffB = it }, label = { Text("b", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = coeffC, onValueChange = { coeffC = it }, label = { Text("c", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                }

                if (degree >= 3) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = coeffD, onValueChange = { coeffD = it }, label = { Text("d", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                        if (degree >= 4) {
                            OutlinedTextField(value = coeffE, onValueChange = { coeffE = it }, label = { Text("e", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { solve() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GIẢI BẤT PHƯƠNG TRÌNH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Solution Output Card & Visual Axis
        if (result != null) {
            val res = result!!
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TẬP NGHIỆM CHÍNH XÁC:", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = res.solutionText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )

                    if (res.realRoots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Các điểm nghiệm mốc: " + res.realRoots.joinToString(", ") { ComplexNumber.cleanDouble(it) },
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual Number Line for the Inequality Solution
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val y = size.height / 2f
                            val w = size.width

                            // Draw axis line
                            drawLine(Color(0xFF64748B), Offset(0f, y), Offset(w, y), strokeWidth = 2f)

                            val minX = (res.realRoots.minOrNull() ?: -3.0) - 2.0
                            val maxX = (res.realRoots.maxOrNull() ?: 3.0) + 2.0
                            val range = (maxX - minX).coerceAtLeast(1e-4)

                            fun toPx(v: Double) = (((v - minX) / range) * w).toFloat().coerceIn(0f, w)

                            // Highlight solution intervals
                            for (interval in res.intervals) {
                                val x1 = if (interval.first <= -1e8) 0f else toPx(interval.first)
                                val x2 = if (interval.second >= 1e8) w else toPx(interval.second)
                                drawLine(Color(0xFF38BDF8), Offset(x1, y), Offset(x2, y), strokeWidth = 5f)
                            }

                            // Draw root markers
                            for (root in res.realRoots) {
                                val xr = toPx(root)
                                drawCircle(Color(0xFFFBBF24), radius = 4f, center = Offset(xr, y))
                            }
                        }
                    }
                }
            }
        }
    }
}
