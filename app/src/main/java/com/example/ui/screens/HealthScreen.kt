package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber
import java.util.Locale

enum class HealthTab(val title: String) {
    BMI("Chỉ số BMI"),
    BMR_TDEE("Calo hàng ngày (TDEE)")
}

enum class ActivityLevel(val label: String, val factor: Double) {
    SEDENTARY("Ít vận động (ngồi văn phòng)", 1.2),
    LIGHT("Nhẹ (tập 1-3 ngày/tuần)", 1.375),
    MODERATE("Vừa (tập 3-5 ngày/tuần)", 1.55),
    ACTIVE("Nặng (tập 6-7 ngày/tuần)", 1.725),
    EXTRA_ACTIVE("Rất nặng (vận động viên)", 1.9)
}

@Composable
fun HealthScreen(
    isDarkTheme: Boolean = true,
    onPasteToCalculator: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(HealthTab.BMI) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF101216) else Color(0xFFF4F6F9))
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Tab Selector
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HealthTab.entries.forEach { tab ->
                    val isSel = activeTab == tab
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) {
                                    if (isDarkTheme) Color(0xFF0284C7) else Color(0xFF2563EB)
                                } else Color.Transparent
                            )
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSel) Color.White else if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (activeTab) {
            HealthTab.BMI -> BmiTabContent(isDarkTheme, onPasteToCalculator)
            HealthTab.BMR_TDEE -> BmrTdeeTabContent(isDarkTheme, onPasteToCalculator)
        }
    }
}

@Composable
private fun BmiTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var heightCmText by remember { mutableStateOf("170") }
    var weightKgText by remember { mutableStateOf("65") }

    val heightCm = heightCmText.toDoubleOrNull() ?: 0.0
    val weightKg = weightKgText.toDoubleOrNull() ?: 0.0

    val heightM = heightCm / 100.0
    val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0.0

    // WHO / Asian Classification
    val (statusText, statusColor) = when {
        bmi <= 0.0 -> Pair("Chưa nhập đủ thông tin", Color.Gray)
        bmi < 18.5 -> Pair("Gầy (Thiếu cân)", Color(0xFF38BDF8))
        bmi < 23.0 -> Pair("Bình thường (Lý tưởng)", Color(0xFF34D399))
        bmi < 25.0 -> Pair("Thừa cân (Tiền béo phì)", Color(0xFFFBBF24))
        else -> Pair("Béo phì", Color(0xFFEF4444))
    }

    val idealMinKg = 18.5 * heightM * heightM
    val idealMaxKg = 22.9 * heightM * heightM

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH CHỈ SỐ KHỐI CƠ THỂ (BMI)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = heightCmText,
                    onValueChange = { heightCmText = it },
                    label = { Text("Chiều cao (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = weightKgText,
                    onValueChange = { weightKgText = it },
                    label = { Text("Cân nặng (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CHỈ SỐ BMI CỦA BẠN:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = String.format(Locale.US, "%.1f", bmi),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Phân loại: $statusText",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (heightM > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cân nặng lý tưởng cho chiều cao ${heightCm.toInt()} cm: ${String.format(Locale.US, "%.1f", idealMinKg)} - ${String.format(Locale.US, "%.1f", idealMaxKg)} kg",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Button(
                onClick = { onPaste(String.format(Locale.US, "%.1f", bmi)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn chỉ số BMI vào Máy tính")
            }
        }
    }
}

@Composable
private fun BmrTdeeTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var genderIsMale by remember { mutableStateOf(true) }
    var ageText by remember { mutableStateOf("25") }
    var heightCmText by remember { mutableStateOf("170") }
    var weightKgText by remember { mutableStateOf("65") }
    var selectedActivityIndex by remember { mutableIntStateOf(1) } // Light

    val age = (ageText.toIntOrNull() ?: 25).coerceIn(10, 120)
    val h = heightCmText.toDoubleOrNull() ?: 170.0
    val w = weightKgText.toDoubleOrNull() ?: 65.0

    // Mifflin-St Jeor Formula
    val bmr = if (genderIsMale) {
        10.0 * w + 6.25 * h - 5.0 * age + 5.0
    } else {
        10.0 * w + 6.25 * h - 5.0 * age - 161.0
    }

    val activity = ActivityLevel.entries[selectedActivityIndex]
    val tdee = bmr * activity.factor

    val cutDeficit = (tdee - 500).coerceAtLeast(1200.0)
    val bulkSurplus = tdee + 400

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH CALO HÀNG NGÀY (BMR & TDEE)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            // Gender Switch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { genderIsMale = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (genderIsMale) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Nam giới", fontSize = 11.sp)
                }

                Button(
                    onClick = { genderIsMale = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!genderIsMale) Color(0xFFE11D48) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Nữ giới", fontSize = 11.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Tuổi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = heightCmText,
                    onValueChange = { heightCmText = it },
                    label = { Text("Cao (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weightKgText,
                    onValueChange = { weightKgText = it },
                    label = { Text("Nặng (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Mức độ hoạt động / vận động:", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ActivityLevel.entries.forEachIndexed { idx, act ->
                    val isSel = selectedActivityIndex == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) Color(0xFF0284C7) else if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedActivityIndex = idx }
                    ) {
                        Text(
                            text = act.label,
                            color = if (isSel) Color.White else if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF1E293B),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("LƯỢNG CALO TIÊU THỤ HÀNG NGÀY (TDEE):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "${tdee.toInt()} kcal / ngày",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Calo chuyển hóa cơ bản (BMR): ${bmr.toInt()} kcal", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    Text("Để giảm mỡ chuẩn (-500 kcal): ${cutDeficit.toInt()} kcal/ngày", color = Color(0xFF34D399), fontSize = 11.sp)
                    Text("Để tăng cơ (+400 kcal): ${bulkSurplus.toInt()} kcal/ngày", color = Color(0xFFFB7185), fontSize = 11.sp)
                }
            }

            Button(
                onClick = { onPaste(tdee.toInt().toString()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn lượng Calo vào Máy tính")
            }
        }
    }
}
