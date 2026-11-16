package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ChemicalElement
import com.example.math.PeriodicTableData
import com.example.math.PhysicalConstant

@Composable
fun AtomicTableScreen(
    onPasteToCalculator: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Elements, 1: Constants
    var searchQuery by remember { mutableStateOf("") }
    var selectedElement by remember { mutableStateOf<ChemicalElement?>(null) }

    val filteredElements = remember(searchQuery) {
        if (searchQuery.isBlank()) PeriodicTableData.elements
        else PeriodicTableData.elements.filter {
            it.nameVi.contains(searchQuery, ignoreCase = true) ||
                    it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.symbol.contains(searchQuery, ignoreCase = true) ||
                    it.atomicNumber.toString() == searchQuery.trim()
        }
    }

    val filteredConstants = remember(searchQuery) {
        if (searchQuery.isBlank()) PeriodicTableData.constants
        else PeriodicTableData.constants.filter {
            it.nameVi.contains(searchQuery, ignoreCase = true) ||
                    it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Tab switcher
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 0) Color(0xFF0284C7) else Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Bảng Nguyên tố Hóa học", fontSize = 11.sp)
            }
            Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 1) Color(0xFF0284C7) else Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hằng số Vật lý / CODATA", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm theo tên, ký hiệu, Z hoặc ký hiệu hằng số...", fontSize = 11.sp, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // Periodic Table Elements List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredElements) { elem ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedElement = elem }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            // Atomic number badge & Symbol
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${elem.atomicNumber}", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(elem.symbol, color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${elem.nameVi} (${elem.nameEn})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("M = ${elem.atomicMass} g/mol", color = Color(0xFFFBBF24), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Cấu hình: ${elem.electronConfig}  •  ${elem.category}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    onPasteToCalculator(elem.atomicMass.toString())
                                    Toast.makeText(context, "Đã chèn M = ${elem.atomicMass} vào máy tính!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Chèn khối lượng", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Physical / CODATA Constants List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredConstants) { constItem ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFBBF24), RoundedCornerShape(8.dp))
                            ) {
                                Text(constItem.symbol, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(constItem.nameVi, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${constItem.value} ${constItem.unit}", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    onPasteToCalculator(constItem.expressionStr)
                                    Toast.makeText(context, "Đã chèn hằng số ${constItem.symbol} vào máy tính!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Chèn", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Element Detail Dialog
    if (selectedElement != null) {
        val elem = selectedElement!!
        AlertDialog(
            onDismissRequest = { selectedElement = null },
            title = { Text("${elem.nameVi} (${elem.nameEn}) - Ký hiệu: ${elem.symbol}", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Số hiệu nguyên tử (Z): ${elem.atomicNumber}", color = Color.White, fontSize = 12.sp)
                    Text("Khối lượng nguyên tử (M): ${elem.atomicMass} g/mol", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Chu kỳ: ${elem.period}  |  Nhóm: ${elem.group}", color = Color.White, fontSize = 12.sp)
                    Text("Phân loại: ${elem.category}", color = Color.White, fontSize = 12.sp)
                    Text("Cấu hình electron: ${elem.electronConfig}", color = Color(0xFF34D399), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPasteToCalculator(elem.atomicMass.toString())
                        selectedElement = null
                        Toast.makeText(context, "Đã chèn ${elem.atomicMass} vào máy tính!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Chèn vào máy tính")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedElement = null }) { Text("Đóng") }
            }
        )
    }
}
