package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber
import com.example.math.DistributionEngine
import kotlin.math.exp

@Composable
fun DistributionScreen() {
    var selectedDist by remember { mutableIntStateOf(0) } // 0: Normal, 1: Binomial, 2: Poisson
    val distNames = listOf("Chuẩn (Normal)", "Nhị thức (Binomial)", "Poisson")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Distribution Type Selector
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            distNames.forEachIndexed { idx, name ->
                val isSel = idx == selectedDist
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) Color(0xFF0284C7) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedDist = idx }
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedDist) {
            0 -> NormalDistributionView()
            1 -> BinomialDistributionView()
            2 -> PoissonDistributionView()
        }
    }
}

@Composable
fun NormalDistributionView() {
    var subMode by remember { mutableIntStateOf(1) } // 0: PD, 1: CD, 2: Inverse
    var meanText by remember { mutableStateOf("0") }
    var sigmaText by remember { mutableStateOf("1") }
    var xText by remember { mutableStateOf("1.96") }
    var lowerText by remember { mutableStateOf("-1.96") }
    var upperText by remember { mutableStateOf("1.96") }
    var pText by remember { mutableStateOf("0.95") }

    var resultStr by remember { mutableStateOf<String?>(null) }

    fun compute() {
        val mu = meanText.toDoubleOrNull() ?: 0.0
        val sigma = sigmaText.toDoubleOrNull() ?: 1.0
        if (sigma <= 0) return

        resultStr = when (subMode) {
            0 -> {
                val x = xText.toDoubleOrNull() ?: 0.0
                val pd = DistributionEngine.normalPD(x, mu, sigma)
                "Normal PD: f($x) = ${ComplexNumber.cleanDouble(pd)}"
            }
            1 -> {
                val low = lowerText.toDoubleOrNull() ?: -1e9
                val up = upperText.toDoubleOrNull() ?: 1e9
                val cd = DistributionEngine.normalCD(low, up, mu, sigma)
                "P($low ≤ X ≤ $up) = ${ComplexNumber.cleanDouble(cd)} (${String.format("%.2f", cd * 100)}%)"
            }
            2 -> {
                val p = pText.toDoubleOrNull() ?: 0.5
                val xInv = DistributionEngine.inverseNormal(p, mu, sigma)
                "Inverse Normal: x = ${ComplexNumber.cleanDouble(xInv)} sao cho P(X ≤ x) = $p"
            }
            else -> ""
        }
    }

    LaunchedEffect(subMode, meanText, sigmaText, xText, lowerText, upperText, pText) {
        compute()
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Submode tabs
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Normal PD (Mật độ)", "Normal CD (Tích lũy)", "Inverse Normal").forEachIndexed { idx, title ->
                    val isSel = subMode == idx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF0F172A))
                            .clickable { subMode = idx }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(title, color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Bell Curve
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(110.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val path = Path()

                    val nPoints = 80
                    for (i in 0..nPoints) {
                        val z = -3.5 + (7.0 * i / nPoints)
                        val yNorm = exp(-0.5 * z * z)
                        val px = (i.toFloat() / nPoints) * w
                        val py = h - 10f - (yNorm.toFloat() * (h - 25f))
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(path, Color(0xFF38BDF8), style = Stroke(width = 2.5f))
                    drawLine(Color(0xFF64748B), Offset(0f, h - 10f), Offset(w, h - 10f), strokeWidth = 1.5f)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Parameters: Mean & Sigma
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = meanText,
                    onValueChange = { meanText = it },
                    label = { Text("Kỳ vọng μ", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sigmaText,
                    onValueChange = { sigmaText = it },
                    label = { Text("Độ lệch chuẩn σ", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (subMode) {
                0 -> {
                    OutlinedTextField(
                        value = xText,
                        onValueChange = { xText = it },
                        label = { Text("Giá trị x", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                1 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = lowerText,
                            onValueChange = { lowerText = it },
                            label = { Text("Cận dưới (Lower)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = upperText,
                            onValueChange = { upperText = it },
                            label = { Text("Cận trên (Upper)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                2 -> {
                    OutlinedTextField(
                        value = pText,
                        onValueChange = { pText = it },
                        label = { Text("Diện tích xác suất p (0..1)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (resultStr != null) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBBF24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = resultStr!!,
                        color = Color(0xFFFBBF24),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BinomialDistributionView() {
    var isCD by remember { mutableStateOf(false) }
    var kText by remember { mutableStateOf("3") }
    var nText by remember { mutableStateOf("10") }
    var pText by remember { mutableStateOf("0.5") }
    var resultStr by remember { mutableStateOf("") }

    fun compute() {
        val k = kText.toIntOrNull() ?: 0
        val n = nText.toIntOrNull() ?: 1
        val p = pText.toDoubleOrNull() ?: 0.5
        if (n >= 0 && k in 0..n && p in 0.0..1.0) {
            val prob = if (!isCD) DistributionEngine.binomialPD(k, n, p) else DistributionEngine.binomialCD(k, n, p)
            val lbl = if (!isCD) "P(X = $k)" else "P(X ≤ $k)"
            resultStr = "$lbl = ${ComplexNumber.cleanDouble(prob)} (${String.format("%.2f", prob * 100)}%)"
        }
    }

    LaunchedEffect(isCD, kText, nText, pText) { compute() }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("PHÂN PHỐI NHỊ THỨC (BINOMIAL: Bpd & Bcd)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { isCD = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isCD) Color(0xFF0284C7) else Color(0xFF0F172A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Binomial PD (X = k)", fontSize = 10.sp)
                }
                Button(
                    onClick = { isCD = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCD) Color(0xFF0284C7) else Color(0xFF0F172A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Binomial CD (X ≤ k)", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = kText, onValueChange = { kText = it }, label = { Text("Số thành công k", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = nText, onValueChange = { nText = it }, label = { Text("Số phép thử n", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = pText, onValueChange = { pText = it }, label = { Text("Xác suất thành công p (0..1)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(10.dp))

            Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(resultStr, color = Color(0xFFFBBF24), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
fun PoissonDistributionView() {
    var isCD by remember { mutableStateOf(false) }
    var kText by remember { mutableStateOf("2") }
    var lambdaText by remember { mutableStateOf("3.5") }
    var resultStr by remember { mutableStateOf("") }

    fun compute() {
        val k = kText.toIntOrNull() ?: 0
        val lambda = lambdaText.toDoubleOrNull() ?: 1.0
        if (k >= 0 && lambda > 0) {
            val prob = if (!isCD) DistributionEngine.poissonPD(k, lambda) else DistributionEngine.poissonCD(k, lambda)
            val lbl = if (!isCD) "P(X = $k)" else "P(X ≤ $k)"
            resultStr = "$lbl = ${ComplexNumber.cleanDouble(prob)} (${String.format("%.2f", prob * 100)}%)"
        }
    }

    LaunchedEffect(isCD, kText, lambdaText) { compute() }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("PHÂN PHỐI POISSON (POISSON: Ppd & Pcd)", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { isCD = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isCD) Color(0xFF0284C7) else Color(0xFF0F172A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Poisson PD (X = k)", fontSize = 10.sp)
                }
                Button(
                    onClick = { isCD = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCD) Color(0xFF0284C7) else Color(0xFF0F172A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Poisson CD (X ≤ k)", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = kText, onValueChange = { kText = it }, label = { Text("Số biến cố k", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = lambdaText, onValueChange = { lambdaText = it }, label = { Text("Tham số λ (Lambda)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(resultStr, color = Color(0xFFFBBF24), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}
