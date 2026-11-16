package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber
import kotlin.math.*
import kotlin.random.Random

@Composable
fun MathBoxScreen() {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Coin Toss, 1: Dice Roll, 2: Unit Circle, 3: Number Line
    val tabs = listOf("Tung đồng xu", "Gieo xúc xắc", "Đường tròn đơn vị", "Trục số")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Tab Header
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { idx, name ->
                val isSel = idx == selectedTab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF0284C7) else Color(0xFF1E293B))
                        .clickable { selectedTab = idx }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = name,
                        color = if (isSel) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> CoinTossView()
            1 -> DiceRollView()
            2 -> UnitCircleView()
            3 -> NumberLineView()
        }
    }
}

@Composable
fun CoinTossView() {
    var coinCount by remember { mutableIntStateOf(1) } // 1..3
    var trials by remember { mutableIntStateOf(10) }
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }
    var lastTosses by remember { mutableStateOf(listOf<Boolean>()) } // true = Head, false = Tail

    fun toss(n: Int) {
        var h = 0
        var t = 0
        val sample = mutableListOf<Boolean>()
        for (i in 0 until n * coinCount) {
            val isHead = Random.nextBoolean()
            if (isHead) h++ else t++
            if (i < 9) sample.add(isHead)
        }
        headsCount += h
        tailsCount += t
        lastTosses = sample
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("MÔ PHỎNG TUNG ĐỒNG XU (COIN TOSS)", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Number of coins & trials
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Số đồng xu: $coinCount", color = Color.White, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 3).forEach { c ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (coinCount == c) Color(0xFF38BDF8) else Color(0xFF0F172A))
                                .clickable { coinCount = c }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("$c", color = if (coinCount == c) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual sample of last tossed coins
            if (lastTosses.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    lastTosses.take(coinCount.coerceAtLeast(1)).forEach { isH ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isH) Color(0xFFFBBF24) else Color(0xFF94A3B8))
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Text(if (isH) "H" else "T", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toss action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { toss(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), modifier = Modifier.weight(1f)) {
                    Text("+1 lần", fontSize = 11.sp)
                }
                Button(onClick = { toss(10) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), modifier = Modifier.weight(1f)) {
                    Text("+10 lần", fontSize = 11.sp)
                }
                Button(onClick = { toss(100) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), modifier = Modifier.weight(1f)) {
                    Text("+100", fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        headsCount = 0
                        tailsCount = 0
                        lastTosses = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats summary
            val total = (headsCount + tailsCount).coerceAtLeast(1)
            val hPct = (headsCount.toDouble() / total) * 100.0
            val tPct = (tailsCount.toDouble() / total) * 100.0

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tổng số lần tung: ${headsCount + tailsCount}", color = Color.White, fontSize = 12.sp)
                Text("Lý thuyết: 50% / 50%", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mặt Ngửa (Heads): $headsCount (${String.format("%.1f", hPct)}%)", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Mặt Sấp (Tails): $tailsCount (${String.format("%.1f", tPct)}%)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DiceRollView() {
    var diceCount by remember { mutableIntStateOf(2) } // 1..3
    var lastDice by remember { mutableStateOf(listOf(3, 4)) }
    var sumCounts by remember { mutableStateOf(mutableMapOf<Int, Int>()) }

    fun roll(times: Int) {
        val map = sumCounts.toMutableMap()
        var sample = listOf<Int>()
        repeat(times) {
            val d = List(diceCount) { Random.nextInt(1, 7) }
            val sum = d.sum()
            map[sum] = (map[sum] ?: 0) + 1
            sample = d
        }
        sumCounts = map
        lastDice = sample
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("MÔ PHỎNG GIEO XÚC XẮC (DICE ROLL)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Select 1, 2, 3 dice
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Số xúc xắc: $diceCount", color = Color.White, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 3).forEach { c ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (diceCount == c) Color(0xFF38BDF8) else Color(0xFF0F172A))
                                .clickable { diceCount = c }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("$c", color = if (diceCount == c) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Dice Pips
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                lastDice.take(diceCount).forEach { value ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$value",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { roll(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), modifier = Modifier.weight(1f)) {
                    Text("+1 lần", fontSize = 11.sp)
                }
                Button(onClick = { roll(10) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), modifier = Modifier.weight(1f)) {
                    Text("+10 lần", fontSize = 11.sp)
                }
                Button(onClick = { roll(100) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), modifier = Modifier.weight(1f)) {
                    Text("+100", fontSize = 11.sp)
                }
                Button(
                    onClick = { sumCounts = mutableMapOf() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Histogram of Sums
            val totalRolls = sumCounts.values.sum().coerceAtLeast(1)
            val minSum = diceCount
            val maxSum = diceCount * 6
            Text("Phân bố tổng điểm (Tổng số lần gieo: $totalRolls):", color = Color(0xFFCBD5E1), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))

            for (s in minSum..maxSum) {
                val cnt = sumCounts[s] ?: 0
                val pct = (cnt.toDouble() / totalRolls) * 100.0
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                    Text("$s:", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0F172A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((pct / 100.0).toFloat().coerceIn(0f, 1f))
                                .background(Color(0xFF38BDF8))
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("$cnt (${String.format("%.1f", pct)}%)", color = Color.White, fontSize = 9.sp, modifier = Modifier.width(65.dp))
                }
            }
        }
    }
}

@Composable
fun UnitCircleView() {
    var angleDeg by remember { mutableFloatStateOf(45f) }
    val rad = Math.toRadians(angleDeg.toDouble())
    val cosVal = cos(rad)
    val sinVal = sin(rad)
    val tanVal = if (abs(cosVal) > 1e-6) tan(rad) else Double.NaN
    val cotVal = if (abs(sinVal) > 1e-6) 1.0 / tan(rad) else Double.NaN

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("ĐƯỜNG TRÒN ĐƠN VỊ LƯỢNG GIÁC (UNIT CIRCLE)", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Canvas drawing the trigonometric unit circle
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width * 0.42f

                    // Coordinate axes
                    drawLine(Color(0xFF475569), Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1.5f)
                    drawLine(Color(0xFF475569), Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1.5f)

                    // Unit circle
                    drawCircle(Color(0xFF38BDF8), radius = radius, center = center, style = Stroke(width = 2f))

                    // Angle point
                    val px = center.x + (radius * cosVal).toFloat()
                    val py = center.y - (radius * sinVal).toFloat()

                    // Projection right triangle
                    drawLine(Color(0xFFFBBF24), center, Offset(px, center.y), strokeWidth = 2.5f) // cos (x)
                    drawLine(Color(0xFFFB7185), Offset(px, center.y), Offset(px, py), strokeWidth = 2.5f) // sin (y)
                    drawLine(Color.White, center, Offset(px, py), strokeWidth = 2f) // hypotenuse

                    // Point P(x, y)
                    drawCircle(Color.White, radius = 5f, center = Offset(px, py))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Angle Slider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Góc θ: ${angleDeg.toInt()}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(80.dp))
                Slider(
                    value = angleDeg,
                    onValueChange = { angleDeg = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF0284C7)),
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Angles Buttons (0, 30, 45, 60, 90, 180, 270, 360)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(0, 30, 45, 60, 90, 120, 180, 270).forEach { a ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (angleDeg.toInt() == a) Color(0xFF38BDF8) else Color(0xFF0F172A))
                            .clickable { angleDeg = a.toFloat() }
                            .padding(vertical = 4.dp)
                    ) {
                        Text("$a°", color = if (angleDeg.toInt() == a) Color(0xFF0F172A) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values Table
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tọa độ P(cos θ, sin θ):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("(${ComplexNumber.cleanDouble(cosVal)}, ${ComplexNumber.cleanDouble(sinVal)})", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("cos θ (trục hoành):", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(ComplexNumber.cleanDouble(cosVal), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("sin θ (trục tung):", color = Color(0xFFFB7185), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(ComplexNumber.cleanDouble(sinVal), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("tan θ = sin/cos:", color = Color(0xFF38BDF8), fontSize = 11.sp)
                Text(if (tanVal.isNaN()) "Không xác định" else ComplexNumber.cleanDouble(tanVal), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun NumberLineView() {
    var lowerBound by remember { mutableFloatStateOf(-2f) }
    var upperBound by remember { mutableFloatStateOf(3f) }
    var includeLower by remember { mutableStateOf(true) }
    var includeUpper by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("TRỤC SỐ THỰC (NUMBER LINE)", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Canvas drawing Real Number Axis with interval shading
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val yCenter = size.height / 2f
                    val axisMin = -5f
                    val axisMax = 5f
                    fun xFor(v: Float): Float = ((v - axisMin) / (axisMax - axisMin)) * size.width

                    // Axis line
                    drawLine(Color(0xFF94A3B8), Offset(10f, yCenter), Offset(size.width - 10f, yCenter), strokeWidth = 2.5f)

                    // Tick marks -5 to 5
                    for (t in -5..5) {
                        val xt = xFor(t.toFloat())
                        drawLine(Color(0xFF64748B), Offset(xt, yCenter - 8f), Offset(xt, yCenter + 8f), strokeWidth = 1.5f)
                    }

                    // Shaded interval
                    val x1 = xFor(lowerBound)
                    val x2 = xFor(upperBound)
                    drawLine(Color(0xFF38BDF8), Offset(x1, yCenter), Offset(x2, yCenter), strokeWidth = 6f)

                    // Lower point
                    drawCircle(Color(0xFF38BDF8), radius = 6f, center = Offset(x1, yCenter))
                    if (!includeLower) drawCircle(Color(0xFF1E293B), radius = 4f, center = Offset(x1, yCenter))

                    // Upper point
                    drawCircle(Color(0xFF38BDF8), radius = 6f, center = Offset(x2, yCenter))
                    if (!includeUpper) drawCircle(Color(0xFF1E293B), radius = 4f, center = Offset(x2, yCenter))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shaded range label
            val openLeft = if (includeLower) "[" else "("
            val openRight = if (includeUpper) "]" else ")"
            Text(
                text = "Tập nghiệm / Khoảng: $openLeft${ComplexNumber.cleanDouble(lowerBound.toDouble())}; ${ComplexNumber.cleanDouble(upperBound.toDouble())}$openRight",
                color = Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Lower bound slider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Đầu mút trái a: ${lowerBound.toInt()}", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                Slider(
                    value = lowerBound,
                    onValueChange = { if (it < upperBound) lowerBound = it },
                    valueRange = -5f..4f,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(checked = includeLower, onCheckedChange = { includeLower = it })
                Text(if (includeLower) "[ ]" else "( )", color = Color(0xFF94A3B8), fontSize = 10.sp)
            }

            // Upper bound slider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Đầu mút phải b: ${upperBound.toInt()}", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                Slider(
                    value = upperBound,
                    onValueChange = { if (it > lowerBound) upperBound = it },
                    valueRange = -4f..5f,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(checked = includeUpper, onCheckedChange = { includeUpper = it })
                Text(if (includeUpper) "[ ]" else "( )", color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
        }
    }
}
