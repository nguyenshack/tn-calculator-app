package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.math.ConversionUnit
import com.example.math.UnitConverter

@Composable
fun ConversionScreen(
    onPasteToCalculator: (String) -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = UnitConverter.categories
    val currentCategory = categories[selectedCategoryIndex]

    var fromUnitIndex by remember { mutableIntStateOf(0) }
    var toUnitIndex by remember { mutableIntStateOf(1) }

    var inputValueText by remember { mutableStateOf("1") }
    var convertedValue by remember { mutableStateOf<Double?>(null) }

    fun doConvert() {
        val input = inputValueText.toDoubleOrNull() ?: 1.0
        val fromUnit = currentCategory.units.getOrElse(fromUnitIndex) { currentCategory.units.first() }
        val toUnit = currentCategory.units.getOrElse(toUnitIndex) { currentCategory.units.last() }
        convertedValue = UnitConverter.convert(input, fromUnit, toUnit)
    }

    LaunchedEffect(selectedCategoryIndex, fromUnitIndex, toUnitIndex, inputValueText) {
        doConvert()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Category Chips
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("CHỌN LOẠI ĐƠN VỊ KỸ THUẬT:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val chunkedCategories = remember(categories) { categories.chunked(3) }
                chunkedCategories.forEachIndexed { rowIdx, rowCats ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        rowCats.forEach { cat ->
                            val idx = categories.indexOf(cat)
                            val isSel = selectedCategoryIndex == idx
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF0284C7) else Color(0xFF0F172A))
                                    .clickable {
                                        selectedCategoryIndex = idx
                                        fromUnitIndex = 0
                                        toUnitIndex = 1
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(cat.name, color = Color.White, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                        // Fill extra weight if row has fewer than 3 items
                        repeat(3 - rowCats.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Conversion Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Input value
                OutlinedTextField(
                    value = inputValueText,
                    onValueChange = { inputValueText = it },
                    label = { Text("Giá trị cần đổi", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // From Unit Dropdown & To Unit Dropdown
                val fromUnit = currentCategory.units.getOrElse(fromUnitIndex) { currentCategory.units.first() }
                val toUnit = currentCategory.units.getOrElse(toUnitIndex) { currentCategory.units.last() }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Từ đơn vị:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        UnitDropdown(
                            units = currentCategory.units,
                            selectedUnit = fromUnit,
                            onSelect = { fromUnitIndex = currentCategory.units.indexOf(it) }
                        )
                    }

                    IconButton(
                        onClick = {
                            val temp = fromUnitIndex
                            fromUnitIndex = toUnitIndex
                            toUnitIndex = temp
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Đảo chiều", tint = Color(0xFF38BDF8))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sang đơn vị:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        UnitDropdown(
                            units = currentCategory.units,
                            selectedUnit = toUnit,
                            onSelect = { toUnitIndex = currentCategory.units.indexOf(it) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result Card
        if (convertedValue != null) {
            val fromUnit = currentCategory.units.getOrElse(fromUnitIndex) { currentCategory.units.first() }
            val toUnit = currentCategory.units.getOrElse(toUnitIndex) { currentCategory.units.last() }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "$inputValueText ${fromUnit.symbol} =",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ComplexNumber.cleanDouble(convertedValue!!)} ${toUnit.symbol}",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onPasteToCalculator(ComplexNumber.cleanDouble(convertedValue!!)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Chèn kết quả vào máy tính", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UnitDropdown(
    units: List<ConversionUnit>,
    selectedUnit: ConversionUnit,
    onSelect: (ConversionUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
        ) {
            Text(
                text = "${selectedUnit.name} (${selectedUnit.symbol})",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp),
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E293B))
        ) {
            units.forEach { u ->
                DropdownMenuItem(
                    text = { Text("${u.name} (${u.symbol})", color = Color.White, fontSize = 12.sp) },
                    onClick = {
                        onSelect(u)
                        expanded = false
                    }
                )
            }
        }
    }
}
