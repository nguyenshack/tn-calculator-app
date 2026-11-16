package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.math.ComplexNumber
import com.example.math.Factorizer
import com.example.math.MathEvaluator
import com.example.ui.CalculatorMode

@Composable
fun SolveDialog(
    evaluator: MathEvaluator,
    initialEquation: String,
    onApplySolution: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var equationText by remember { mutableStateOf(initialEquation.ifBlank { "x^2 - 5 = 0" }) }
    var guessText by remember { mutableStateOf("0") }
    var solvedX by remember { mutableStateOf<Double?>(null) }
    var lMinusR by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun doSolve() {
        errorMessage = null
        try {
            val guess = guessText.toDoubleOrNull() ?: 0.0
            val x = evaluator.solve(equationText, guess)
            val parts = equationText.split("=")
            val fExpr = if (parts.size == 2) "(${parts[0]}) - (${parts[1]})" else equationText
            val diff = evaluator.evaluateFunctionAt(fExpr, x)
            solvedX = x
            lMinusR = diff
        } catch (e: Exception) {
            errorMessage = e.message ?: "Không tìm thấy nghiệm hội tụ"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("TÌM NGHIỆM GẦN ĐÚNG (SOLVE)", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Giải phương trình bất kỳ f(x) = 0 bằng phương pháp lặp Newton-Raphson:", color = Color(0xFF94A3B8), fontSize = 11.sp)

                OutlinedTextField(
                    value = equationText,
                    onValueChange = { equationText = it },
                    label = { Text("Phương trình (Ví dụ: x^3 - 2*x - 5 = 0)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = guessText,
                    onValueChange = { guessText = it },
                    label = { Text("Giá trị khởi đầu x₀ (Start Guess)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (solvedX != null) {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Nghiệm x = ${ComplexNumber.cleanDouble(solvedX!!)}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                            if (lMinusR != null) {
                                Text("Độ sai lệch L - R ≈ ${String.format("%.2e", lMinusR)}", color = Color(0xFF34D399), fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = { doSolve() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("SOLVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
fun CalcDialog(
    evaluator: MathEvaluator,
    currentExpression: String,
    onResultComputed: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var exprText by remember { mutableStateOf(currentExpression.ifBlank { "2*A + 3*B" }) }
    var valA by remember { mutableStateOf(evaluator.getVariable("A").toString()) }
    var valB by remember { mutableStateOf(evaluator.getVariable("B").toString()) }
    var valX by remember { mutableStateOf(evaluator.getVariable("x").toString()) }
    var valY by remember { mutableStateOf(evaluator.getVariable("y").toString()) }
    var calcResult by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun doCalc() {
        errorMessage = null
        try {
            evaluator.setVariable("A", valA.toDoubleOrNull() ?: 0.0)
            evaluator.setVariable("B", valB.toDoubleOrNull() ?: 0.0)
            evaluator.setVariable("x", valX.toDoubleOrNull() ?: 0.0)
            evaluator.setVariable("y", valY.toDoubleOrNull() ?: 0.0)
            val res = evaluator.evaluate(exprText)
            calcResult = res
            onResultComputed(res)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi tính toán CALC"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("TÍNH GIÁ TRỊ THEO BIẾN (CALC)", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = exprText,
                    onValueChange = { exprText = it },
                    label = { Text("Biểu thức chứa biến", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = valA, onValueChange = { valA = it }, label = { Text("A = ?", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = valB, onValueChange = { valB = it }, label = { Text("B = ?", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = valX, onValueChange = { valX = it }, label = { Text("x = ?", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = valY, onValueChange = { valY = it }, label = { Text("y = ?", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                }

                if (calcResult != null) {
                    Text("Giá trị biểu thức: ${ComplexNumber.cleanDouble(calcResult!!)}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = { doCalc() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("CALC =")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
fun FactDialog(
    initialValue: Long,
    onDismiss: () -> Unit
) {
    var numText by remember { mutableStateOf(if (initialValue > 0) initialValue.toString() else "3600") }
    var factResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun doFact() {
        errorMessage = null
        try {
            val n = numText.toLongOrNull() ?: 0L
            require(n > 0) { "Vui lòng nhập số nguyên dương > 0" }
            factResult = Factorizer.factorize(n)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi phân tích thừa số nguyên tố"
        }
    }

    LaunchedEffect(Unit) { doFact() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PHÂN TÍCH THỪA SỐ NGUYÊN TỐ (FACT)", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = numText,
                    onValueChange = { numText = it },
                    label = { Text("Số nguyên dương N", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (factResult != null) {
                    Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Kết quả FACT:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$numText = $factResult", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = { doFact() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("FACT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
fun GcdLcmDialog(
    onDismiss: () -> Unit
) {
    var numbersText by remember { mutableStateOf("24, 36, 60") }
    var gcdResult by remember { mutableStateOf<Long?>(null) }
    var lcmResult by remember { mutableStateOf<Long?>(null) }

    fun compute() {
        val nums = numbersText.split(",", " ").mapNotNull { it.trim().toLongOrNull() }.filter { it > 0 }
        if (nums.isNotEmpty()) {
            gcdResult = Factorizer.gcdList(nums)
            lcmResult = Factorizer.lcmList(nums)
        }
    }

    LaunchedEffect(numbersText) { compute() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("TÌM GCD (ƯCLN) & LCM (BCNN)", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = numbersText,
                    onValueChange = { numbersText = it },
                    label = { Text("Nhập các số (cách nhau bởi dấu phẩy)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (gcdResult != null && lcmResult != null) {
                    Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("GCD (Ước số chung lớn nhất): $gcdResult", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("LCM (Bội số chung nhỏ nhất): $lcmResult", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { compute() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("Tính")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
fun SumProdDialog(
    evaluator: MathEvaluator,
    onResultComputed: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var isSum by remember { mutableStateOf(true) } // true: Sum, false: Prod
    var exprText by remember { mutableStateOf("x^2") }
    var startText by remember { mutableStateOf("1") }
    var endText by remember { mutableStateOf("10") }
    var computedVal by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun compute() {
        errorMessage = null
        try {
            val a = startText.toIntOrNull() ?: 1
            val b = endText.toIntOrNull() ?: 10
            val res = if (isSum) evaluator.numericalSum(exprText, a, b) else evaluator.numericalProduct(exprText, a, b)
            computedVal = res
            onResultComputed(res)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi tính toán dãy số"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSum) "TÍNH TỔNG DÃY SỐ (∑)" else "TÍNH TÍCH DÃY SỐ (∏)", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { isSum = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSum) Color(0xFF0284C7) else Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tổng ∑", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { isSum = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isSum) Color(0xFF0284C7) else Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tích ∏", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = exprText,
                    onValueChange = { exprText = it },
                    label = { Text("Quy luật f(x)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Từ x = a", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("Đến x = b", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                }

                if (computedVal != null) {
                    Text("Kết quả: ${ComplexNumber.cleanDouble(computedVal!!)}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = { compute() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("Tính toán")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

data class HubItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun ToolsHubDialog(
    evaluator: MathEvaluator,
    currentExpression: String,
    isDarkTheme: Boolean = true,
    onOpenSolve: () -> Unit,
    onOpenCalc: () -> Unit,
    onOpenGcdLcm: () -> Unit,
    onOpenSumProd: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenDateTime: () -> Unit,
    onOpenHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val items = listOf(
        HubItem("Tìm nghiệm f(x) = 0 (SOLVE)", "Giải phương trình gần đúng theo x", Icons.Default.Adjust, Color(0xFFFBBF24), onOpenSolve),
        HubItem("Tính giá trị theo biến (CALC)", "Gán biến A, B, C, D, E, x, y và tính", Icons.Default.Functions, Color(0xFF38BDF8), onOpenCalc),
        HubItem("Tài chính & Tiền tệ", "Tiền tệ, Tip, Giảm giá, Khoản vay, Thuế VAT", Icons.Default.AttachMoney, Color(0xFF34D399), onOpenFinance),
        HubItem("Sức khỏe (BMI / Calo)", "Tính chỉ số BMI và calo BMR / TDEE hàng ngày", Icons.Default.Favorite, Color(0xFFFB7185), onOpenHealth),
        HubItem("Ngày và Giờ", "Khoảng cách giữa 2 ngày, +/- ngày, tính tuổi", Icons.Default.Today, Color(0xFFA78BFA), onOpenDateTime),
        HubItem("Lịch sử tính toán", "Xem lại và lấy lại kết quả các phép tính cũ", Icons.Default.History, Color(0xFF60A5FA), onOpenHistory),
        HubItem("ƯCLN (GCD) & BCNN (LCM)", "Tìm ước chung lớn nhất, bội chung nhỏ nhất", Icons.Default.AllInclusive, Color(0xFFF472B6), onOpenGcdLcm),
        HubItem("Tổng dãy số (∑) & Tích (∏)", "Tính tổng hoặc tích theo quy luật biến x", Icons.Default.FormatListNumbered, Color(0xFFF59E0B), onOpenSumProd)
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("CÔNG CỤ HỖ TRỢ (TOOLS)", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Chọn tiện ích tính toán nâng cao", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = if (isDarkTheme) Color.White else Color(0xFF0F172A))
                    }
                }

                HorizontalDivider(color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(items) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    item.onClick()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = item.color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = if (isDarkTheme) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Text(item.subtitle, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogHubDialog(
    onOpenFact: () -> Unit,
    onOpenConversion: () -> Unit,
    onOpenBaseN: () -> Unit,
    onOpenAtomicTable: () -> Unit,
    onInsertSymbol: (String) -> Unit,
    isDarkTheme: Boolean = true,
    onDismiss: () -> Unit
) {
    val catalogItems = listOf(
        HubItem("Phân tích thừa số nguyên tố (FACT)", "Phân tích số N ra tích các thừa số nguyên tố", Icons.Default.ScatterPlot, Color(0xFFFBBF24), onOpenFact),
        HubItem("Đổi đơn vị đo lường (Unit Converter)", "Độ dài, khối lượng, nhiệt độ, thời gian, lưu trữ...", Icons.Default.SwapHoriz, Color(0xFF38BDF8), onOpenConversion),
        HubItem("Chuyển đổi hệ cơ số (Base-N)", "Hệ nhị phân (BIN), bát phân (OCT), DEC, thập lục phân (HEX)", Icons.Default.Code, Color(0xFF34D399), onOpenBaseN),
        HubItem("Bảng nguyên tố & Hằng số CODATA", "Tra cứu 118 nguyên tố, hằng số vật lý quốc tế", Icons.Default.Science, Color(0xFFA78BFA), onOpenAtomicTable)
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("DANH MỤC LỆNH (CATALOG)", color = Color(0xFFFBBF24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Tra cứu toán học & tính năng chuyên sâu", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = if (isDarkTheme) Color.White else Color(0xFF0F172A))
                    }
                }

                HorizontalDivider(color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(catalogItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    item.onClick()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = item.color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = if (isDarkTheme) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Text(item.subtitle, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("CHÈN NHANH HẰNG SỐ & HÀM ĐẶC BIỆT:", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val quickTokens = listOf("π", "e", "Ran#", "sinh(", "cosh(", "tanh(", "gcd(", "lcm(")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            quickTokens.take(4).forEach { tok ->
                                Button(
                                    onClick = {
                                        onInsertSymbol(tok)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFE2E8F0)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(tok, color = if (isDarkTheme) Color.White else Color(0xFF0F172A), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            quickTokens.drop(4).forEach { tok ->
                                Button(
                                    onClick = {
                                        onInsertSymbol(tok)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFE2E8F0)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(tok, color = if (isDarkTheme) Color.White else Color(0xFF0F172A), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

