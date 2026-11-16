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
import com.example.math.Matrix

@Composable
fun MatrixScreen() {
    var selectedMatrixIndex by remember { mutableIntStateOf(0) } // 0: MatA, 1: MatB, 2: MatC, 3: MatD
    val matrixNames = listOf("MatA", "MatB", "MatC", "MatD")

    // Store dimensions and 4x4 data for each matrix
    var matricesData by remember {
        mutableStateOf(
            List(4) {
                Array(4) { r -> DoubleArray(4) { c -> if (r == c) 1.0 else 0.0 } }
            }
        )
    }
    var matrixDims by remember {
        mutableStateOf(List(4) { Pair(3, 3) })
    }

    var resultMatrix by remember { mutableStateOf<Matrix?>(null) }
    var resultScalar by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val currentDim = matrixDims[selectedMatrixIndex]
    val currentRows = currentDim.first
    val currentCols = currentDim.second
    val currentData = matricesData[selectedMatrixIndex]

    fun getCurrentMatrixObj(): Matrix {
        return Matrix(currentRows, currentCols, List(currentRows) { r -> List(currentCols) { c -> currentData[r][c] } })
    }

    fun getMatrixObjAt(index: Int): Matrix {
        val dim = matrixDims[index]
        return Matrix(dim.first, dim.second, List(dim.first) { r -> List(dim.second) { c -> matricesData[index][r][c] } })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Matrix Selector Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            matrixNames.forEachIndexed { idx, name ->
                val isSel = idx == selectedMatrixIndex
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) Color(0xFF0284C7) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedMatrixIndex = idx
                            resultMatrix = null
                            resultScalar = null
                            statusMessage = null
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${matrixDims[idx].first}×${matrixDims[idx].second}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dimension Selector Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kích thước ${matrixNames[selectedMatrixIndex]}:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(Pair(2, 2), Pair(3, 3), Pair(4, 4), Pair(2, 3), Pair(3, 1)).forEach { (r, c) ->
                    val isSel = currentRows == r && currentCols == c
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B))
                            .clickable {
                                val newDims = matrixDims.toMutableList()
                                newDims[selectedMatrixIndex] = Pair(r, c)
                                matrixDims = newDims
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("$r×$c", color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Matrix Grid Editor
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                for (r in 0 until currentRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        for (c in 0 until currentCols) {
                            var textVal by remember(selectedMatrixIndex, r, c, currentData[r][c]) {
                                mutableStateOf(ComplexNumber.cleanDouble(currentData[r][c]))
                            }
                            OutlinedTextField(
                                value = textVal,
                                onValueChange = {
                                    textVal = it
                                    val parsed = it.toDoubleOrNull() ?: 0.0
                                    currentData[r][c] = parsed
                                },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Operations Buttons Grid
        val activeName = matrixNames[selectedMatrixIndex]
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    try {
                        val m = getCurrentMatrixObj()
                        val det = m.determinant()
                        resultScalar = "det($activeName) = ${ComplexNumber.cleanDouble(det)}"
                        resultMatrix = null
                        statusMessage = null
                    } catch (e: Exception) {
                        statusMessage = e.message
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("det(A)", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    try {
                        val m = getCurrentMatrixObj()
                        resultMatrix = m.inverse()
                        resultScalar = "Ma trận nghịch đảo $activeName⁻¹"
                        statusMessage = null
                    } catch (e: Exception) {
                        statusMessage = e.message
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("A⁻¹", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    val m = getCurrentMatrixObj()
                    resultMatrix = m.transpose()
                    resultScalar = "Ma trận chuyển vị ${activeName}ᵀ"
                    statusMessage = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Aᵀ", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    val m = getCurrentMatrixObj()
                    resultMatrix = m.rref()
                    resultScalar = "Bậc thang thu gọn (RREF)"
                    statusMessage = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1f)
            ) {
                Text("RREF", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Binary Operations between matrices
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    try {
                        val a = getMatrixObjAt(0)
                        val b = getMatrixObjAt(1)
                        resultMatrix = a * b
                        resultScalar = "Kết quả: MatA × MatB"
                        statusMessage = null
                    } catch (e: Exception) {
                        statusMessage = e.message
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("MatA × MatB", fontSize = 10.sp, color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    try {
                        val a = getMatrixObjAt(0)
                        val b = getMatrixObjAt(1)
                        resultMatrix = a + b
                        resultScalar = "Kết quả: MatA + MatB"
                        statusMessage = null
                    } catch (e: Exception) {
                        statusMessage = e.message
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("MatA + MatB", fontSize = 10.sp, color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    try {
                        val m = getCurrentMatrixObj()
                        resultMatrix = m * m
                        resultScalar = "Kết quả: $activeName²"
                        statusMessage = null
                    } catch (e: Exception) {
                        statusMessage = e.message
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("A²", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result Display Card
        if (resultScalar != null || resultMatrix != null || statusMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (resultScalar != null) {
                        Text(resultScalar!!, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (resultMatrix != null) {
                        val m = resultMatrix!!
                        Spacer(modifier = Modifier.height(6.dp))
                        for (r in 0 until m.rows) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceAround,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                for (c in 0 until m.cols) {
                                    Text(
                                        text = ComplexNumber.cleanDouble(m[r, c]),
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (statusMessage != null) {
                        Text(statusMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
