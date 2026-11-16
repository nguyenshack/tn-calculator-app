package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class DateTab(val title: String) {
    DIFF("Khoảng cách 2 ngày"),
    ADD_SUB("Cộng / Trừ ngày"),
    AGE("Tính tuổi chi tiết")
}

@Composable
fun DateTimeScreen(
    isDarkTheme: Boolean = true,
    onPasteToCalculator: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(DateTab.DIFF) }

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
                DateTab.entries.forEach { tab ->
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
            DateTab.DIFF -> DateDiffTabContent(isDarkTheme, onPasteToCalculator)
            DateTab.ADD_SUB -> DateAddSubTabContent(isDarkTheme, onPasteToCalculator)
            DateTab.AGE -> AgeTabContent(isDarkTheme, onPasteToCalculator)
        }
    }
}

private fun getDayOfWeekName(cal: Calendar): String {
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Thứ Hai"
        Calendar.TUESDAY -> "Thứ Ba"
        Calendar.WEDNESDAY -> "Thứ Tư"
        Calendar.THURSDAY -> "Thứ Năm"
        Calendar.FRIDAY -> "Thứ Sáu"
        Calendar.SATURDAY -> "Thứ Bảy"
        Calendar.SUNDAY -> "Chủ Nhật"
        else -> ""
    }
}

@Composable
private fun DateDiffTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    val now = remember { Calendar.getInstance() }
    var day1 by remember { mutableStateOf(now.get(Calendar.DAY_OF_MONTH).toString()) }
    var month1 by remember { mutableStateOf((now.get(Calendar.MONTH) + 1).toString()) }
    var year1 by remember { mutableStateOf(now.get(Calendar.YEAR).toString()) }

    var day2 by remember { mutableStateOf("1") }
    var month2 by remember { mutableStateOf("1") }
    var year2 by remember { mutableStateOf((now.get(Calendar.YEAR) + 1).toString()) }

    val cal1 = Calendar.getInstance().apply {
        set(year1.toIntOrNull() ?: 2025, (month1.toIntOrNull() ?: 1) - 1, day1.toIntOrNull() ?: 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val cal2 = Calendar.getInstance().apply {
        set(year2.toIntOrNull() ?: 2026, (month2.toIntOrNull() ?: 1) - 1, day2.toIntOrNull() ?: 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMillis = kotlin.math.abs(cal2.timeInMillis - cal1.timeInMillis)
    val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val weeks = totalDays / 7
    val remainDays = totalDays % 7
    val monthsApprox = totalDays / 30.4375

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH KHOẢNG CÁCH GIỮA HAI NGÀY", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Text("Ngày bắt đầu (Ngày / Tháng / Năm):", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = day1, onValueChange = { day1 = it }, label = { Text("Ngày") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = month1, onValueChange = { month1 = it }, label = { Text("Tháng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = year1, onValueChange = { year1 = it }, label = { Text("Năm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.2f))
            }

            Text("Ngày kết thúc (Ngày / Tháng / Năm):", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = day2, onValueChange = { day2 = it }, label = { Text("Ngày") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = month2, onValueChange = { month2 = it }, label = { Text("Tháng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = year2, onValueChange = { year2 = it }, label = { Text("Năm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.2f))
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TỔNG KHOẢNG CÁCH THỜI GIAN:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "$totalDays ngày",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Quy đổi: $weeks tuần + $remainDays ngày", color = Color(0xFF34D399), fontSize = 12.sp)
                    Text("Tương đương: ${String.format(Locale.US, "%.1f", monthsApprox)} tháng (~${totalDays * 24} giờ)", color = Color(0xFF38BDF8), fontSize = 11.sp)
                }
            }

            Button(
                onClick = { onPaste(totalDays.toString()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn số ngày vào Máy tính")
            }
        }
    }
}

@Composable
private fun DateAddSubTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    val now = remember { Calendar.getInstance() }
    var baseDay by remember { mutableStateOf(now.get(Calendar.DAY_OF_MONTH).toString()) }
    var baseMonth by remember { mutableStateOf((now.get(Calendar.MONTH) + 1).toString()) }
    var baseYear by remember { mutableStateOf(now.get(Calendar.YEAR).toString()) }
    var isAdd by remember { mutableStateOf(true) }
    var offsetDaysText by remember { mutableStateOf("30") }

    val offsetDays = (offsetDaysText.toIntOrNull() ?: 0) * (if (isAdd) 1 else -1)

    val targetCal = Calendar.getInstance().apply {
        set(baseYear.toIntOrNull() ?: 2025, (baseMonth.toIntOrNull() ?: 1) - 1, baseDay.toIntOrNull() ?: 1)
        add(Calendar.DAY_OF_YEAR, offsetDays)
    }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedResultDate = sdf.format(targetCal.time)
    val dayOfWeek = getDayOfWeekName(targetCal)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("CỘNG HOẶC TRỪ NGÀY TỪ NGÀY MỐC", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Text("Ngày mốc (Ngày / Tháng / Năm):", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = baseDay, onValueChange = { baseDay = it }, label = { Text("Ngày") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = baseMonth, onValueChange = { baseMonth = it }, label = { Text("Tháng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = baseYear, onValueChange = { baseYear = it }, label = { Text("Năm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.2f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { isAdd = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAdd) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Thêm ngày", fontSize = 11.sp)
                }

                Button(
                    onClick = { isAdd = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isAdd) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("− Trừ ngày", fontSize = 11.sp)
                }
            }

            OutlinedTextField(
                value = offsetDaysText,
                onValueChange = { offsetDaysText = it },
                label = { Text("Số ngày cần cộng / trừ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("NGÀY KẾT QUẢ:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "$dayOfWeek, $formattedResultDate",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                }
            }

            Button(
                onClick = { onPaste(targetCal.get(Calendar.DAY_OF_MONTH).toString()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn ngày vào Máy tính")
            }
        }
    }
}

@Composable
private fun AgeTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var birthDay by remember { mutableStateOf("15") }
    var birthMonth by remember { mutableStateOf("8") }
    var birthYear by remember { mutableStateOf("2000") }

    val today = remember { Calendar.getInstance() }

    val bDay = (birthDay.toIntOrNull() ?: 15).coerceIn(1, 31)
    val bMonth = (birthMonth.toIntOrNull() ?: 8).coerceIn(1, 12)
    val bYear = (birthYear.toIntOrNull() ?: 2000).coerceIn(1900, today.get(Calendar.YEAR))

    val birthCal = Calendar.getInstance().apply {
        set(bYear, bMonth - 1, bDay, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var years = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
    var months = today.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
    var days = today.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months--
        val prevMonth = (today.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) {
        years--
        months += 12
    }

    val totalDaysAlive = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - birthCal.timeInMillis).coerceAtLeast(0)

    // Next birthday calculation
    val nextBday = Calendar.getInstance().apply {
        set(today.get(Calendar.YEAR), bMonth - 1, bDay, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(today)) {
            add(Calendar.YEAR, 1)
        }
    }
    val daysUntilNextBday = TimeUnit.MILLISECONDS.toDays(nextBday.timeInMillis - today.timeInMillis)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH TUỔI CHI TIẾT & SINH NHẬT", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Text("Ngày sinh của bạn (Ngày / Tháng / Năm):", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = birthDay, onValueChange = { birthDay = it }, label = { Text("Ngày") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = birthMonth, onValueChange = { birthMonth = it }, label = { Text("Tháng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = birthYear, onValueChange = { birthYear = it }, label = { Text("Năm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.2f))
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TUỔI HIỆN TẠI CHÍNH XÁC:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "$years tuổi $months tháng $days ngày",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Tổng số ngày đã sống: $totalDaysAlive ngày", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    Text("Còn $daysUntilNextBday ngày nữa là đến sinh nhật tiếp theo!", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }

            Button(
                onClick = { onPaste(years.toString()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn số tuổi vào Máy tính")
            }
        }
    }
}
