package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.math.AngleUnit
import com.example.math.ComplexNumber
import com.example.math.Vector3D

@Composable
fun VectorScreen(angleUnit: AngleUnit) {
    var selectedVectorIndex by remember { mutableIntStateOf(0) } // 0: VctA, 1: VctB, 2: VctC, 3: VctD
    val vectorNames = listOf("VctA", "VctB", "VctC", "VctD")

    var vectorsData by remember {
        mutableStateOf(
            List(4) { idx ->
                when (idx) {
                    0 -> Vector3D(1.0, 2.0, 3.0)
                    1 -> Vector3D(4.0, 5.0, 6.0)
                    else -> Vector3D(0.0, 0.0, 0.0)
                }
            }
        )
    }

    var is3D by remember { mutableStateOf(true) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultVector by remember { mutableStateOf<Vector3D?>(null) }

    val currentVec = vectorsData[selectedVectorIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Vector Selection Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            vectorNames.forEachIndexed { idx, name ->
                val isSel = idx == selectedVectorIndex
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) Color(0xFF0284C7) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedVectorIndex = idx
                            resultText = null
                            resultVector = null
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        val v = vectorsData[idx]
                        Text(if (is3D) "(${v.x.toInt()}, ${v.y.toInt()}, ${v.z.toInt()})" else "(${v.x.toInt()}, ${v.y.toInt()})", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dimension Toggle (2D / 3D)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Không gian véc-tơ:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(Pair("2D (Mặt phẳng)", false), Pair("3D (Không gian)", true)).forEach { (lbl, val3D) ->
                    val isSel = is3D == val3D
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B))
                            .clickable { is3D = val3D }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(lbl, color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vector Component Editor
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Tọa độ ${vectorNames[selectedVectorIndex]}:", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    var xText by remember(selectedVectorIndex, currentVec.x) { mutableStateOf(ComplexNumber.cleanDouble(currentVec.x)) }
                    OutlinedTextField(
                        value = xText,
                        onValueChange = {
                            xText = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            val updated = vectorsData.toMutableList()
                            updated[selectedVectorIndex] = currentVec.copy(x = parsed)
                            vectorsData = updated
                        },
                        label = { Text("X", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )

                    var yText by remember(selectedVectorIndex, currentVec.y) { mutableStateOf(ComplexNumber.cleanDouble(currentVec.y)) }
                    OutlinedTextField(
                        value = yText,
                        onValueChange = {
                            yText = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            val updated = vectorsData.toMutableList()
                            updated[selectedVectorIndex] = currentVec.copy(y = parsed)
                            vectorsData = updated
                        },
                        label = { Text("Y", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )

                    if (is3D) {
                        var zText by remember(selectedVectorIndex, currentVec.z) { mutableStateOf(ComplexNumber.cleanDouble(currentVec.z)) }
                        OutlinedTextField(
                            value = zText,
                            onValueChange = {
                                zText = it
                                val parsed = it.toDoubleOrNull() ?: 0.0
                                val updated = vectorsData.toMutableList()
                                updated[selectedVectorIndex] = currentVec.copy(z = parsed)
                                vectorsData = updated
                            },
                            label = { Text("Z", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vector Operations
        val activeName = vectorNames[selectedVectorIndex]
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val u = vectorsData[0]
                    val v = vectorsData[1]
                    val dot = u.dot(v)
                    resultText = "Tích vô hướng VctA • VctB = ${ComplexNumber.cleanDouble(dot)}"
                    resultVector = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("VctA • VctB", fontSize = 10.sp)
            }

            Button(
                onClick = {
                    val u = vectorsData[0]
                    val v = vectorsData[1]
                    val cross = u.cross(v)
                    resultVector = cross
                    resultText = "Tích có hướng [VctA, VctB]:"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("VctA × VctB", fontSize = 10.sp)
            }

            Button(
                onClick = {
                    val u = vectorsData[0]
                    val v = vectorsData[1]
                    val angle = if (angleUnit == AngleUnit.DEG) "${ComplexNumber.cleanDouble(u.angleDeg(v))}°" else "${ComplexNumber.cleanDouble(u.angleRad(v))} rad"
                    resultText = "Góc giữa VctA và VctB: $angle"
                    resultVector = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Angle(A, B)", fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    val norm = currentVec.norm()
                    resultText = "Độ dài |$activeName| = ${ComplexNumber.cleanDouble(norm)}"
                    resultVector = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Độ dài |$activeName|", fontSize = 11.sp, color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    val unit = currentVec.unit()
                    resultVector = unit
                    resultText = "Véc-tơ đơn vị của $activeName:"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Unit Vector", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result Card
        if (resultText != null || resultVector != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (resultText != null) {
                        Text(resultText!!, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    if (resultVector != null) {
                        val v = resultVector!!
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (is3D) "(${ComplexNumber.cleanDouble(v.x)}, ${ComplexNumber.cleanDouble(v.y)}, ${ComplexNumber.cleanDouble(v.z)})"
                            else "(${ComplexNumber.cleanDouble(v.x)}, ${ComplexNumber.cleanDouble(v.y)})",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
