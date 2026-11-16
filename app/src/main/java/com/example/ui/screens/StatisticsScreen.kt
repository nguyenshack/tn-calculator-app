package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.math.OneVarStats
import com.example.math.StatisticsEngine
import com.example.math.TwoVarStats

@Composable
fun StatisticsScreen() {
    var isTwoVar by remember { mutableStateOf(false) } // false: 1-Var, true: 2-Var
    var dataRows by remember {
        mutableStateOf(
            mutableListOf(
                Pair(10.0, 1.0),
                Pair(15.0, 2.0),
                Pair(20.0, 3.0),
                Pair(25.0, 1.0)
            )
        )
    }

    var oneVarResult by remember { mutableStateOf<OneVarStats?>(null) }
    var twoVarResult by remember { mutableStateOf<TwoVarStats?>(null) }
    var predictXInput by remember { mutableStateOf("18") }
    var predictYOutput by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculateStats() {
        errorMessage = null
        try {
            if (!isTwoVar) {
                oneVarResult = StatisticsEngine.calculateOneVar(dataRows)
                twoVarResult = null
            } else {
                twoVarResult = StatisticsEngine.calculateTwoVar(dataRows)
                oneVarResult = null
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi tính toán thống kê"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Mode Selector: 1-Var (Thống kê 1 biến) vs 2-Var (Hồi quy 2 biến)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    isTwoVar = false
                    oneVarResult = null
                    twoVarResult = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (!isTwoVar) Color(0xFF0284C7) else Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Thống kê 1 biến (1-Var)", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    isTwoVar = true
                    oneVarResult = null
                    twoVarResult = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isTwoVar) Color(0xFF0284C7) else Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hồi quy 2 biến (2-Var)", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data Table Header
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (!isTwoVar) "BẢNG DỮ LIỆU: X VÀ TẦN SỐ (FREQ)" else "BẢNG DỮ LIỆU: CẶP (X, Y)",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = {
                            val updated = dataRows.toMutableList()
                            updated.add(Pair(0.0, 1.0))
                            dataRows = updated
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm hàng", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table rows
                dataRows.forEachIndexed { idx, row ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text("${idx + 1}.", color = Color(0xFF64748B), fontSize = 10.sp, modifier = Modifier.width(18.dp))

                        var xStr by remember(idx, row.first) { mutableStateOf(ComplexNumber.cleanDouble(row.first)) }
                        OutlinedTextField(
                            value = xStr,
                            onValueChange = {
                                xStr = it
                                val num = it.toDoubleOrNull() ?: 0.0
                                val updated = dataRows.toMutableList()
                                updated[idx] = Pair(num, row.second)
                                dataRows = updated
                            },
                            label = { Text("X", fontSize = 9.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        var yStr by remember(idx, row.second) { mutableStateOf(ComplexNumber.cleanDouble(row.second)) }
                        OutlinedTextField(
                            value = yStr,
                            onValueChange = {
                                yStr = it
                                val num = it.toDoubleOrNull() ?: 1.0
                                val updated = dataRows.toMutableList()
                                updated[idx] = Pair(row.first, num)
                                dataRows = updated
                            },
                            label = { Text(if (!isTwoVar) "Freq" else "Y", fontSize = 9.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        IconButton(
                            onClick = {
                                if (dataRows.size > 2) {
                                    val updated = dataRows.toMutableList()
                                    updated.removeAt(idx)
                                    dataRows = updated
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { calculateStats() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TÍNH TOÁN KẾT QUẢ THỐNG KÊ", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1-Var Result Output
        if (oneVarResult != null) {
            val r = oneVarResult!!
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("KẾT QUẢ THỐNG KÊ 1 BIẾN (1-VAR RESULTS)", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val items = listOf(
                        Pair("Số lượng mẫu n", ComplexNumber.cleanDouble(r.n)),
                        Pair("Giá trị trung bình x̄", ComplexNumber.cleanDouble(r.mean)),
                        Pair("Tổng ∑x", ComplexNumber.cleanDouble(r.sumX)),
                        Pair("Tổng bình phương ∑x²", ComplexNumber.cleanDouble(r.sumX2)),
                        Pair("Độ lệch chuẩn mẫu sₓ", ComplexNumber.cleanDouble(r.sampleStdDev)),
                        Pair("Phương sai mẫu sₓ²", ComplexNumber.cleanDouble(r.sampleVariance)),
                        Pair("Độ lệch chuẩn tổng thể σₓ", ComplexNumber.cleanDouble(r.popStdDev)),
                        Pair("Phương sai tổng thể σₓ²", ComplexNumber.cleanDouble(r.popVariance)),
                        Pair("Giá trị nhỏ nhất min(x)", ComplexNumber.cleanDouble(r.minX)),
                        Pair("Tứ phân vị thứ nhất Q₁", ComplexNumber.cleanDouble(r.q1)),
                        Pair("Trung vị Med", ComplexNumber.cleanDouble(r.median)),
                        Pair("Tứ phân vị thứ ba Q₃", ComplexNumber.cleanDouble(r.q3)),
                        Pair("Giá trị lớn nhất max(x)", ComplexNumber.cleanDouble(r.maxX))
                    )

                    items.forEach { (lbl, valStr) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(lbl, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(valStr, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2-Var Result Output
        if (twoVarResult != null) {
            val r = twoVarResult!!
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("KẾT QUẢ HỒI QUY 2 BIẾN (2-VAR REGRESSION)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Hồi quy tuyến tính: y = a + bx", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hệ số a:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(ComplexNumber.cleanDouble(r.linearA), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hệ số b:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(ComplexNumber.cleanDouble(r.linearB), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hệ số tương quan r:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(ComplexNumber.cleanDouble(r.correlationR), color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hệ số xác định r²:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(ComplexNumber.cleanDouble(r.rSquared), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    if (r.quadA != null && r.quadB != null && r.quadC != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Hồi quy Parabol: y = a + bx + cx²", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("a = ${ComplexNumber.cleanDouble(r.quadA)}, b = ${ComplexNumber.cleanDouble(r.quadB)}, c = ${ComplexNumber.cleanDouble(r.quadC)}", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prediction tool
                    Text("Dự đoán giá trị ŷ từ x:", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = predictXInput,
                            onValueChange = { predictXInput = it },
                            label = { Text("Nhập x", fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val x = predictXInput.toDoubleOrNull() ?: 0.0
                                val yHat = r.predictLinearY(x)
                                predictYOutput = "ŷ = ${ComplexNumber.cleanDouble(yHat)}"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Dự đoán", fontSize = 11.sp)
                        }
                    }

                    if (predictYOutput != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(predictYOutput!!, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
