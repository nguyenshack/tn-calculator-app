package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
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
fun SpreadsheetScreen() {
    val totalCols = 5 // A, B, C, D, E
    val colLabels = listOf("A", "B", "C", "D", "E")
    val totalRows = 45 // 1..45

    // State matrix 5 x 45 of Double values or raw formulas
    var gridValues by remember {
        mutableStateOf(
            Array(totalRows) { DoubleArray(totalCols) { 0.0 } }
        )
    }

    var selectedCell by remember { mutableStateOf(Pair(0, 0)) } // row, col
    var formulaInput by remember { mutableStateOf("") }
    var showFillSequenceDialog by remember { mutableStateOf(false) }

    fun getCellValue(r: Int, c: Int): Double = if (r in 0 until totalRows && c in 0 until totalCols) gridValues[r][c] else 0.0

    fun evaluateFormula(formula: String): Double {
        val clean = formula.trim().removePrefix("=").uppercase()
        // SUM(A1:A5)
        if (clean.startsWith("SUM(") && clean.endsWith(")")) {
            val range = clean.removePrefix("SUM(").removeSuffix(")")
            val cells = parseRange(range)
            return cells.sumOf { (r, c) -> getCellValue(r, c) }
        }
        // MEAN(A1:A5)
        if (clean.startsWith("MEAN(") && clean.endsWith(")")) {
            val range = clean.removePrefix("MEAN(").removeSuffix(")")
            val cells = parseRange(range)
            if (cells.isEmpty()) return 0.0
            return cells.sumOf { (r, c) -> getCellValue(r, c) } / cells.size
        }
        // MIN(A1:A5)
        if (clean.startsWith("MIN(") && clean.endsWith(")")) {
            val range = clean.removePrefix("MIN(").removeSuffix(")")
            val cells = parseRange(range)
            if (cells.isEmpty()) return 0.0
            return cells.minOf { (r, c) -> getCellValue(r, c) }
        }
        // MAX(A1:A5)
        if (clean.startsWith("MAX(") && clean.endsWith(")")) {
            val range = clean.removePrefix("MAX(").removeSuffix(")")
            val cells = parseRange(range)
            if (cells.isEmpty()) return 0.0
            return cells.maxOf { (r, c) -> getCellValue(r, c) }
        }
        return formula.toDoubleOrNull() ?: 0.0
    }

    val selR = selectedCell.first
    val selC = selectedCell.second
    val selCellName = "${colLabels[selC]}${selR + 1}"

    // Quick column summary for current selected column
    val colValues = List(totalRows) { r -> gridValues[r][selC] }
    val nonZeroCol = colValues.filter { it != 0.0 }
    val colSum = colValues.sum()
    val colMean = if (nonZeroCol.isNotEmpty()) colSum / nonZeroCol.size else 0.0
    val colMin = if (nonZeroCol.isNotEmpty()) nonZeroCol.min() else 0.0
    val colMax = if (nonZeroCol.isNotEmpty()) nonZeroCol.max() else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Quick Stats Summary Ribbon
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text("Cột ${colLabels[selC]}:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("SUM: ${ComplexNumber.cleanDouble(colSum)}", color = Color(0xFFFBBF24), fontSize = 11.sp)
                Text("MEAN: ${ComplexNumber.cleanDouble(colMean)}", color = Color.White, fontSize = 11.sp)
                Text("MIN: ${ComplexNumber.cleanDouble(colMin)}", color = Color(0xFF34D399), fontSize = 10.sp)
                Text("MAX: ${ComplexNumber.cleanDouble(colMax)}", color = Color(0xFFFB7185), fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Formula Bar & Sequence Fill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(42.dp)
                    .height(44.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
            ) {
                Text(selCellName, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = formulaInput,
                onValueChange = { formulaInput = it },
                placeholder = { Text("Nhập số hoặc =SUM(A1:A5)...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val computed = evaluateFormula(formulaInput)
                    val copy = Array(totalRows) { r -> gridValues[r].clone() }
                    copy[selR][selC] = computed
                    gridValues = copy
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("EXE", fontSize = 11.sp)
            }

            IconButton(
                onClick = { showFillSequenceDialog = true },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Cấp số cộng / nhân", tint = Color(0xFFFBBF24))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Spreadsheet Grid (5 columns x 45 rows)
        val hScroll = rememberScrollState()
        val vListState = rememberLazyListState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        ) {
            // Column Headers (A, B, C, D, E)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .horizontalScroll(hScroll)
            ) {
                // Corner spacer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(36.dp)
                        .height(30.dp)
                        .border(0.5.dp, Color(0xFF334155))
                ) {
                    Text("#", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                colLabels.forEachIndexed { cIdx, colName ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(72.dp)
                            .height(30.dp)
                            .border(0.5.dp, Color(0xFF334155))
                            .background(if (cIdx == selC) Color(0xFF0369A1) else Color.Transparent)
                    ) {
                        Text(colName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Rows 1..45
            LazyColumn(
                state = vListState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(totalRows) { rIdx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(hScroll)
                    ) {
                        // Row Number index
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(36.dp)
                                .height(32.dp)
                                .background(if (rIdx == selR) Color(0xFF0369A1) else Color(0xFF1E293B))
                                .border(0.5.dp, Color(0xFF334155))
                        ) {
                            Text("${rIdx + 1}", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // 5 Cells in this row
                        for (cIdx in 0 until totalCols) {
                            val isSelected = rIdx == selR && cIdx == selC
                            val cellVal = gridValues[rIdx][cIdx]
                            val dispStr = if (cellVal == 0.0) "" else ComplexNumber.cleanDouble(cellVal)

                            Box(
                                contentAlignment = Alignment.CenterEnd,
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(32.dp)
                                    .background(if (isSelected) Color(0xFF0C4A6E) else Color(0xFF0F172A))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                                    )
                                    .clickable {
                                        selectedCell = Pair(rIdx, cIdx)
                                        formulaInput = if (cellVal == 0.0) "" else ComplexNumber.cleanDouble(cellVal)
                                    }
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = dispStr,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Fill Sequence Dialog: Cấp số cộng & Cấp số nhân
    if (showFillSequenceDialog) {
        FillSequenceDialog(
            targetCol = colLabels[selC],
            onApply = { startVal, stepVal, isArithmetic, count ->
                val copy = Array(totalRows) { r -> gridValues[r].clone() }
                var curr = startVal
                for (i in 0 until count.coerceAtMost(totalRows)) {
                    copy[i][selC] = curr
                    curr = if (isArithmetic) curr + stepVal else curr * stepVal
                }
                gridValues = copy
                showFillSequenceDialog = false
            },
            onDismiss = { showFillSequenceDialog = false }
        )
    }
}

@Composable
fun FillSequenceDialog(
    targetCol: String,
    onApply: (Double, Double, Boolean, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isArithmetic by remember { mutableStateOf(true) } // true: Cấp số cộng, false: Cấp số nhân
    var startText by remember { mutableStateOf("1") }
    var stepText by remember { mutableStateOf("2") }
    var countText by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ĐIỀN DÃY SỐ (CỘT $targetCol)", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isArithmetic = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isArithmetic) Color(0xFF0284C7) else Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cấp số cộng (uₙ = u₁ + (n-1)d)", fontSize = 9.sp)
                    }
                    Button(
                        onClick = { isArithmetic = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isArithmetic) Color(0xFF0284C7) else Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cấp số nhân (uₙ = u₁·qⁿ⁻¹)", fontSize = 9.sp)
                    }
                }

                OutlinedTextField(
                    value = startText,
                    onValueChange = { startText = it },
                    label = { Text("Số hạng đầu u₁", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stepText,
                    onValueChange = { stepText = it },
                    label = { Text(if (isArithmetic) "Công sai d" else "Công bội q", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it },
                    label = { Text("Số hàng điền (tối đa 45)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = startText.toDoubleOrNull() ?: 1.0
                    val d = stepText.toDoubleOrNull() ?: 1.0
                    val n = countText.toIntOrNull() ?: 10
                    onApply(s, d, isArithmetic, n)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("Áp dụng")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

private fun parseRange(rangeStr: String): List<Pair<Int, Int>> {
    // E.g. "A1:A5" -> [(0, 0), (1, 0), (2, 0), (3, 0), (4, 0)]
    val parts = rangeStr.split(":")
    if (parts.size != 2) return emptyList()
    val start = parseCellCoord(parts[0]) ?: return emptyList()
    val end = parseCellCoord(parts[1]) ?: return emptyList()

    val minR = minOf(start.first, end.first)
    val maxR = maxOf(start.first, end.first)
    val minC = minOf(start.second, end.second)
    val maxC = maxOf(start.second, end.second)

    val result = mutableListOf<Pair<Int, Int>>()
    for (r in minR..maxR) {
        for (c in minC..maxC) {
            result.add(Pair(r, c))
        }
    }
    return result
}

private fun parseCellCoord(cell: String): Pair<Int, Int>? {
    val clean = cell.trim().uppercase()
    if (clean.isEmpty()) return null
    val colChar = clean[0]
    val colIdx = when (colChar) {
        'A' -> 0; 'B' -> 1; 'C' -> 2; 'D' -> 3; 'E' -> 4
        else -> return null
    }
    val rowNum = clean.substring(1).toIntOrNull() ?: return null
    return Pair(rowNum - 1, colIdx)
}
