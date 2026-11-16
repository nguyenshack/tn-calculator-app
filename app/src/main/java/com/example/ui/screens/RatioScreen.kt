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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber

@Composable
fun RatioScreen() {
    var ratioType by remember { mutableIntStateOf(1) } // 1: A : B = X : D, 2: A : B = C : X

    var valA by remember { mutableStateOf("2") }
    var valB by remember { mutableStateOf("3") }
    var valC by remember { mutableStateOf("6") }
    var valD by remember { mutableStateOf("9") }

    var resultX by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun solveRatio() {
        errorMessage = null
        try {
            val a = valA.toDoubleOrNull() ?: 0.0
            val b = valB.toDoubleOrNull() ?: 0.0
            if (ratioType == 1) {
                // A : B = X : D -> X = (A * D) / B
                val d = valD.toDoubleOrNull() ?: 0.0
                require(b != 0.0) { "Mẫu số B không thể bằng 0" }
                resultX = (a * d) / b
            } else {
                // A : B = C : X -> X = (B * C) / A
                val c = valC.toDoubleOrNull() ?: 0.0
                require(a != 0.0) { "Mẫu số A không thể bằng 0" }
                resultX = (b * c) / a
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi giải tỷ lệ thức"
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
                Text("GIẢI BÀI TOÁN TỶ LỆ THỨC (RATIO)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Select Form
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(Pair("1: A : B = X : D", 1), Pair("2: A : B = C : X", 2)).forEach { (lbl, type) ->
                        val isSel = ratioType == type
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF0284C7) else Color(0xFF0F172A))
                                .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable {
                                    ratioType = type
                                    resultX = null
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(lbl, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Ratio Equation Form
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = valA,
                        onValueChange = { valA = it },
                        label = { Text("A", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.width(68.dp)
                    )
                    Text(" : ", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = valB,
                        onValueChange = { valB = it },
                        label = { Text("B", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.width(68.dp)
                    )
                    Text(" = ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    if (ratioType == 1) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.size(54.dp, 50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("X", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Text(" : ", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        OutlinedTextField(
                            value = valD,
                            onValueChange = { valD = it },
                            label = { Text("D", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.width(68.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = valC,
                            onValueChange = { valC = it },
                            label = { Text("C", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.width(68.dp)
                        )
                        Text(" : ", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.size(54.dp, 50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("X", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { solveRatio() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TÍNH TOÁN ẨN SỐ X", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result Card
        if (resultX != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KẾT QUẢ TỶ LỆ THỨC:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "X = ${ComplexNumber.cleanDouble(resultX!!)}",
                        color = Color(0xFFFBBF24),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val formulaUsed = if (ratioType == 1) "X = (A × D) / B = ($valA × $valD) / $valB" else "X = (B × C) / A = ($valB × $valC) / $valA"
                    Text(formulaUsed, color = Color(0xFF64748B), fontSize = 11.sp)
                }
            }
        }
    }
}
