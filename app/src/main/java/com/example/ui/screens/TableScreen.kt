package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber
import com.example.math.MathEvaluator
import com.example.ui.components.QrCodeDialog

data class TableRowData(val x: Double, val fx: Double, val gx: Double?)

@Composable
fun TableScreen() {
    var fExpr by remember { mutableStateOf("x^2 - 4") }
    var gExpr by remember { mutableStateOf("2*x - 1") }
    var enableG by remember { mutableStateOf(true) }
    var startText by remember { mutableStateOf("-3") }
    var endText by remember { mutableStateOf("3") }
    var stepText by remember { mutableStateOf("0.5") }

    var tableRows by remember { mutableStateOf<List<TableRowData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showGraphView by remember { mutableStateOf(true) }

    val evaluator = remember { MathEvaluator() }

    fun generateTable() {
        errorMessage = null
        try {
            val start = startText.toDoubleOrNull() ?: -5.0
            val end = endText.toDoubleOrNull() ?: 5.0
            val step = stepText.toDoubleOrNull() ?: 1.0
            require(step > 0) { "Bước nhảy Step phải > 0" }
            require(start < end) { "Start phải nhỏ hơn End" }
            val stepsCount = ((end - start) / step).toInt()
            require(stepsCount <= 200) { "Số lượng hàng quá lớn (tối đa 200)" }

            val list = mutableListOf<TableRowData>()
            var currX = start
            while (currX <= end + 1e-9) {
                val fx = evaluator.evaluateFunctionAt(fExpr, currX)
                val gx = if (enableG && gExpr.isNotBlank()) evaluator.evaluateFunctionAt(gExpr, currX) else null
                list.add(TableRowData(currX, fx, gx))
                currX += step
            }
            tableRows = list
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi tính toán bảng hàm số"
        }
    }

    LaunchedEffect(Unit) { generateTable() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Function input card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("BẢNG GIÁ TRỊ & ĐỒ THỊ 2 HÀM SỐ f(x) & g(x)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fExpr,
                    onValueChange = { fExpr = it },
                    label = { Text("Hàm số f(x)", color = Color(0xFFFBBF24), fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gExpr,
                        onValueChange = { gExpr = it },
                        enabled = enableG,
                        label = { Text("Hàm số g(x)", color = Color(0xFF38BDF8), fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Checkbox(checked = enableG, onCheckedChange = { enableG = it })
                    Text("Bật g(x)", color = Color.White, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Start, End, Step parameters
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Start", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("End", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = stepText, onValueChange = { stepText = it }, label = { Text("Step", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { generateTable() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("TẠO BẢNG GIÁ TRỊ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showQrDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xuất mã QR", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive On-Screen Function Plotting Canvas
        if (tableRows.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ĐỒ THỊ HÀM SỐ f(x) & g(x)", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("● f(x) Vàng", color = Color(0xFFFBBF24), fontSize = 10.sp)
                            if (enableG) Text("● g(x) Xanh", color = Color(0xFF38BDF8), fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val minX = tableRows.first().x
                            val maxX = tableRows.last().x
                            val allY = tableRows.map { it.fx } + if (enableG) tableRows.mapNotNull { it.gx } else emptyList()
                            val minY = (allY.minOrNull() ?: -10.0).coerceAtLeast(-100.0)
                            val maxY = (allY.maxOrNull() ?: 10.0).coerceAtMost(100.0)
                            val yRange = (maxY - minY).coerceAtLeast(1e-4)

                            fun toPxX(x: Double): Float = (((x - minX) / (maxX - minX)) * w).toFloat()
                            fun toPxY(y: Double): Float = (h - ((y - minY) / yRange) * h).toFloat()

                            // Grid and zero axes
                            if (0.0 in minX..maxX) {
                                val zx = toPxX(0.0)
                                drawLine(Color(0xFF334155), Offset(zx, 0f), Offset(zx, h), strokeWidth = 1f)
                            }
                            if (0.0 in minY..maxY) {
                                val zy = toPxY(0.0)
                                drawLine(Color(0xFF334155), Offset(0f, zy), Offset(w, zy), strokeWidth = 1f)
                            }

                            // Plot f(x)
                            val fPath = Path()
                            tableRows.forEachIndexed { i, pt ->
                                val px = toPxX(pt.x)
                                val py = toPxY(pt.fx).coerceIn(0f, h)
                                if (i == 0) fPath.moveTo(px, py) else fPath.lineTo(px, py)
                            }
                            drawPath(fPath, Color(0xFFFBBF24), style = Stroke(width = 2.5f))

                            // Plot g(x)
                            if (enableG) {
                                val gPath = Path()
                                var first = true
                                tableRows.forEach { pt ->
                                    if (pt.gx != null) {
                                        val px = toPxX(pt.x)
                                        val py = toPxY(pt.gx).coerceIn(0f, h)
                                        if (first) {
                                            gPath.moveTo(px, py)
                                            first = false
                                        } else {
                                            gPath.lineTo(px, py)
                                        }
                                    }
                                }
                                drawPath(gPath, Color(0xFF38BDF8), style = Stroke(width = 2.5f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values Table Display
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Text("x", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("f(x)", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        if (enableG) {
                            Text("g(x)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    tableRows.take(60).forEachIndexed { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 1) Color(0xFF243044) else Color.Transparent)
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Text(ComplexNumber.cleanDouble(row.x), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(ComplexNumber.cleanDouble(row.fx), color = Color(0xFFFBBF24), fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            if (enableG) {
                                Text(if (row.gx != null) ComplexNumber.cleanDouble(row.gx) else "-", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        val qrPayload = "TN-001 Function Plot:\nf(x) = $fExpr\n" + (if (enableG) "g(x) = $gExpr\n" else "") +
                "Domain: [$startText, $endText], Step: $stepText\nSamples: " +
                tableRows.take(5).joinToString(", ") { "(${ComplexNumber.cleanDouble(it.x)}, ${ComplexNumber.cleanDouble(it.fx)})" }
        QrCodeDialog(
            title = "Đồ thị hàm số & Bảng giá trị",
            dataToEncode = qrPayload,
            onDismiss = { showQrDialog = false }
        )
    }
}
