package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

enum class FinanceTab(val title: String) {
    CURRENCY("Tiền tệ"),
    TIP("Tiền boa & Chia bill"),
    DISCOUNT("Giảm giá"),
    LOAN("Khoản vay / Lãi"),
    VAT("Thuế VAT")
}

data class CurrencyRate(val code: String, val name: String, val toVnd: Double)

val SUPPORTED_CURRENCIES = listOf(
    CurrencyRate("VND", "Việt Nam Đồng", 1.0),
    CurrencyRate("USD", "Đô la Mỹ", 25420.0),
    CurrencyRate("EUR", "Euro Châu Âu", 27350.0),
    CurrencyRate("JPY", "Yên Nhật", 166.5),
    CurrencyRate("GBP", "Bảng Anh", 32800.0),
    CurrencyRate("KRW", "Won Hàn Quốc", 18.6),
    CurrencyRate("CNY", "Nhân dân tệ", 3540.0),
    CurrencyRate("SGD", "Đô la Singapore", 19350.0),
    CurrencyRate("AUD", "Đô la Úc", 16600.0),
    CurrencyRate("CAD", "Đô la Canada", 18450.0),
    CurrencyRate("THB", "Baht Thái Lan", 745.0)
)

@Composable
fun FinanceScreen(
    isDarkTheme: Boolean = true,
    onPasteToCalculator: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(FinanceTab.CURRENCY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF101216) else Color(0xFFF4F6F9))
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Tab Navigation
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
                FinanceTab.entries.forEach { tab ->
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
                            fontSize = 10.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (activeTab) {
            FinanceTab.CURRENCY -> CurrencyTabContent(isDarkTheme, onPasteToCalculator)
            FinanceTab.TIP -> TipTabContent(isDarkTheme, onPasteToCalculator)
            FinanceTab.DISCOUNT -> DiscountTabContent(isDarkTheme, onPasteToCalculator)
            FinanceTab.LOAN -> LoanTabContent(isDarkTheme, onPasteToCalculator)
            FinanceTab.VAT -> VatTabContent(isDarkTheme, onPasteToCalculator)
        }
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}

@Composable
private fun CurrencyTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var fromIndex by remember { mutableIntStateOf(1) } // USD
    var toIndex by remember { mutableIntStateOf(0) }   // VND
    var amountText by remember { mutableStateOf("100") }

    val fromCurr = SUPPORTED_CURRENCIES[fromIndex]
    val toCurr = SUPPORTED_CURRENCIES[toIndex]

    val inputAmount = amountText.toDoubleOrNull() ?: 0.0
    val convertedAmount = if (toCurr.toVnd != 0.0) {
        (inputAmount * fromCurr.toVnd) / toCurr.toVnd
    } else 0.0

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("CHUYỂN ĐỔI TIỀN TỆ QUỐC TẾ", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Số tiền cần đổi") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // From Currency Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text("Từ đơn vị:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    CurrencyDropdown(fromIndex, onSelect = { fromIndex = it }, isDarkTheme = isDarkTheme)
                }

                IconButton(
                    onClick = {
                        val temp = fromIndex
                        fromIndex = toIndex
                        toIndex = temp
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Đổi chiều", tint = Color(0xFF38BDF8))
                }

                // To Currency Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sang đơn vị:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    CurrencyDropdown(toIndex, onSelect = { toIndex = it }, isDarkTheme = isDarkTheme)
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("KẾT QUẢ QUY ĐỔI:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatMoney(convertedAmount)} ${toCurr.code}",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "1 ${fromCurr.code} = ${formatMoney(fromCurr.toVnd / toCurr.toVnd)} ${toCurr.code}",
                        color = Color(0xFF34D399),
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = { onPaste(ComplexNumber.cleanDouble(convertedAmount)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn kết quả vào Máy tính")
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(selectedIndex: Int, onSelect: (Int) -> Unit, isDarkTheme: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val current = SUPPORTED_CURRENCIES[selectedIndex]

    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text("${current.code} (${current.name})", fontSize = 11.sp, maxLines = 1)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SUPPORTED_CURRENCIES.forEachIndexed { idx, curr ->
                DropdownMenuItem(
                    text = { Text("${curr.code} - ${curr.name}", fontSize = 12.sp) },
                    onClick = {
                        onSelect(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TipTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var billText by remember { mutableStateOf("500000") }
    var tipPercentText by remember { mutableStateOf("10") }
    var splitText by remember { mutableStateOf("4") }

    val bill = billText.toDoubleOrNull() ?: 0.0
    val tipPct = tipPercentText.toDoubleOrNull() ?: 0.0
    val split = (splitText.toIntOrNull() ?: 1).coerceAtLeast(1)

    val tipAmount = bill * (tipPct / 100.0)
    val totalBill = bill + tipAmount
    val perPerson = totalBill / split

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH TIỀN BOA (TIP) & CHIA ĐỀU HOÁ ĐƠN", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            OutlinedTextField(
                value = billText,
                onValueChange = { billText = it },
                label = { Text("Tổng số tiền hoá đơn") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tipPercentText,
                    onValueChange = { tipPercentText = it },
                    label = { Text("Phần trăm Tip (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = splitText,
                    onValueChange = { splitText = it },
                    label = { Text("Số người chia") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Tip buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("5", "10", "15", "20").forEach { pct ->
                    Button(
                        onClick = { tipPercentText = pct },
                        colors = ButtonDefaults.buttonColors(containerColor = if (tipPercentText == pct) Color(0xFF0284C7) else Color(0xFF334155)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$pct%", fontSize = 11.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Mỗi người cần trả:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "${formatMoney(perPerson)} đ",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tiền boa tổng: ${formatMoney(tipAmount)} đ", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    Text("Tổng thanh toán: ${formatMoney(totalBill)} đ", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = { onPaste(ComplexNumber.cleanDouble(perPerson)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn tiền mỗi người vào Máy tính")
            }
        }
    }
}

@Composable
private fun DiscountTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var originalPriceText by remember { mutableStateOf("1200000") }
    var discountPercentText by remember { mutableStateOf("25") }
    var extraDiscountText by remember { mutableStateOf("0") }

    val originalPrice = originalPriceText.toDoubleOrNull() ?: 0.0
    val discPct = discountPercentText.toDoubleOrNull() ?: 0.0
    val extraDisc = extraDiscountText.toDoubleOrNull() ?: 0.0

    val primarySavings = originalPrice * (discPct / 100.0)
    val priceAfterPrimary = (originalPrice - primarySavings).coerceAtLeast(0.0)
    val totalSavings = primarySavings + extraDisc
    val finalPrice = (originalPrice - totalSavings).coerceAtLeast(0.0)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH GIÁ KHUYẾN MÃI & GIẢM GIÁ (DISCOUNT)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            OutlinedTextField(
                value = originalPriceText,
                onValueChange = { originalPriceText = it },
                label = { Text("Giá gốc ban đầu") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = discountPercentText,
                    onValueChange = { discountPercentText = it },
                    label = { Text("Giảm giá (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = extraDiscountText,
                    onValueChange = { extraDiscountText = it },
                    label = { Text("Giảm thêm (đ)") },
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
                    Text("GIÁ SAU KHI GIẢM:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "${formatMoney(finalPrice)} đ",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Số tiền bạn tiết kiệm được: ${formatMoney(totalSavings)} đ", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = { onPaste(ComplexNumber.cleanDouble(finalPrice)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn giá sau giảm vào Máy tính")
            }
        }
    }
}

@Composable
private fun LoanTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var loanAmountText by remember { mutableStateOf("100000000") } // 100M VND
    var yearlyRateText by remember { mutableStateOf("8.5") }       // 8.5%/year
    var monthsText by remember { mutableStateOf("24") }            // 24 months
    var isCompoundMonthly by remember { mutableStateOf(true) }     // true: Trả góp dư nợ giảm dần, false: Lãi đơn

    val p = loanAmountText.toDoubleOrNull() ?: 0.0
    val yearlyRate = yearlyRateText.toDoubleOrNull() ?: 0.0
    val n = (monthsText.toIntOrNull() ?: 12).coerceAtLeast(1)

    val monthlyRate = (yearlyRate / 100.0) / 12.0

    val (monthlyPayment, totalInterest, totalPayment) = if (isCompoundMonthly) {
        if (monthlyRate > 0) {
            val pmt = p * (monthlyRate * (1 + monthlyRate).pow(n)) / ((1 + monthlyRate).pow(n) - 1)
            val tot = pmt * n
            Triple(pmt, tot - p, tot)
        } else {
            Triple(p / n, 0.0, p)
        }
    } else {
        // Lãi đơn
        val totInt = p * (yearlyRate / 100.0) * (n / 12.0)
        val tot = p + totInt
        Triple(tot / n, totInt, tot)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH KHOẢN VAY & TRẢ GÓP LÃI SUẤT", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            OutlinedTextField(
                value = loanAmountText,
                onValueChange = { loanAmountText = it },
                label = { Text("Số tiền vay ban đầu (Gốc)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = yearlyRateText,
                    onValueChange = { yearlyRateText = it },
                    label = { Text("Lãi suất (% / Năm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { monthsText = it },
                    label = { Text("Kỳ hạn (Tháng)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { isCompoundMonthly = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCompoundMonthly) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dư nợ giảm dần", fontSize = 10.5.sp)
                }

                Button(
                    onClick = { isCompoundMonthly = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isCompoundMonthly) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lãi đơn cố định", fontSize = 10.5.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Trả góp mỗi tháng:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "${formatMoney(monthlyPayment)} đ / tháng",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tổng tiền lãi phải trả: ${formatMoney(totalInterest)} đ", color = Color(0xFFF87171), fontSize = 12.sp)
                    Text("Tổng tiền gốc + lãi: ${formatMoney(totalPayment)} đ", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = { onPaste(ComplexNumber.cleanDouble(monthlyPayment)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn số tiền trả hàng tháng vào Máy tính")
            }
        }
    }
}

@Composable
private fun VatTabContent(isDarkTheme: Boolean, onPaste: (String) -> Unit) {
    var amountText by remember { mutableStateOf("1000000") }
    var vatRateText by remember { mutableStateOf("10") }
    var isAddVat by remember { mutableStateOf(true) } // true: +VAT, false: -VAT (trích xuất giá trước thuế)

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val vatRate = vatRateText.toDoubleOrNull() ?: 10.0

    val (netAmount, vatAmount, grossAmount) = if (isAddVat) {
        val vat = amount * (vatRate / 100.0)
        Triple(amount, vat, amount + vat)
    } else {
        val net = amount / (1.0 + (vatRate / 100.0))
        val vat = amount - net
        Triple(net, vat, amount)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TÍNH THUẾ GIÁ TRỊ GIA TĂNG (VAT)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Số tiền cần tính") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("5", "8", "10").forEach { rate ->
                    Button(
                        onClick = { vatRateText = rate },
                        colors = ButtonDefaults.buttonColors(containerColor = if (vatRateText == rate) Color(0xFF0284C7) else Color(0xFF334155)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$rate% VAT", fontSize = 11.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { isAddVat = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAddVat) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Thêm VAT", fontSize = 11.sp)
                }

                Button(
                    onClick = { isAddVat = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isAddVat) Color(0xFF0284C7) else Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("- Tách VAT", fontSize = 11.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TỔNG THANH TOÁN SAU THUẾ:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = "${formatMoney(grossAmount)} đ",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tiền thuế VAT ($vatRate%): ${formatMoney(vatAmount)} đ", color = Color(0xFFFBBF24), fontSize = 12.sp)
                    Text("Giá trước thuế (Net): ${formatMoney(netAmount)} đ", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }

            Button(
                onClick = { onPaste(ComplexNumber.cleanDouble(grossAmount)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chèn số tiền vào Máy tính")
            }
        }
    }
}
