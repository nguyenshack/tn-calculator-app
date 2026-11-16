package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ComplexNumber
import kotlin.math.*

sealed class GraphPlotData {
    data class Polynomial(
        val a: Double,
        val b: Double,
        val c: Double,
        val d: Double = 0.0,
        val e: Double = 0.0,
        val degree: Int,
        val realRoots: List<Double>,
        val vertexX: Double? = null,
        val vertexY: Double? = null,
        val isMin: Boolean? = null
    ) : GraphPlotData()

    data class LinearSystem2D(
        val a1: Double,
        val b1: Double,
        val c1: Double,
        val a2: Double,
        val b2: Double,
        val c2: Double,
        val intersectX: Double?,
        val intersectY: Double?
    ) : GraphPlotData()
}

@Composable
fun EquationGraphView(
    plotData: GraphPlotData,
    modifier: Modifier = Modifier
) {
    var zoomFactor by remember(plotData) { mutableFloatStateOf(1.0f) }
    var inspectX by remember(plotData) { mutableStateOf<Double?>(null) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0B111E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row with Title & Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ĐỒ THỊ HÀM SỐ (GRAPH VIEW)",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Zoom controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { zoomFactor = (zoomFactor * 1.3f).coerceAtMost(5.0f) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Phóng to",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = { zoomFactor = (zoomFactor / 1.3f).coerceAtLeast(0.3f) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Thu nhỏ",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            zoomFactor = 1.0f
                            inspectX = null
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Đặt lại",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Graph Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF080C14))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            ) {
                when (plotData) {
                    is GraphPlotData.Polynomial -> {
                        PolynomialCanvas(
                            data = plotData,
                            zoom = zoomFactor,
                            inspectX = inspectX,
                            onInspectXChange = { inspectX = it }
                        )
                    }
                    is GraphPlotData.LinearSystem2D -> {
                        LinearSystemCanvas(
                            data = plotData,
                            zoom = zoomFactor,
                            inspectX = inspectX,
                            onInspectXChange = { inspectX = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Legend & Details Bar
            when (plotData) {
                is GraphPlotData.Polynomial -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegendItem(color = Color(0xFF38BDF8), label = "Đường cong y=f(x)")
                            if (plotData.realRoots.isNotEmpty()) {
                                LegendItem(color = Color(0xFFF59E0B), label = "Nghiệm (${plotData.realRoots.size})")
                            }
                            if (plotData.vertexX != null) {
                                LegendItem(color = Color(0xFF10B981), label = if (plotData.isMin == true) "Cực tiểu" else "Cực đại")
                            }
                        }

                        if (inspectX != null) {
                            val curY = evalPoly(inspectX!!, plotData)
                            Text(
                                text = "x: ${ComplexNumber.cleanDouble(inspectX!!)} | y: ${ComplexNumber.cleanDouble(curY)}",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Chạm để xem tọa độ",
                                color = Color(0xFF64748B),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
                is GraphPlotData.LinearSystem2D -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegendItem(color = Color(0xFF38BDF8), label = "Đường d₁")
                            LegendItem(color = Color(0xFFF59E0B), label = "Đường d₂")
                            if (plotData.intersectX != null) {
                                LegendItem(color = Color(0xFF10B981), label = "Giao điểm")
                            }
                        }

                        if (plotData.intersectX != null && plotData.intersectY != null) {
                            Text(
                                text = "(${ComplexNumber.cleanDouble(plotData.intersectX)}, ${ComplexNumber.cleanDouble(plotData.intersectY)})",
                                color = Color(0xFF10B981),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.5.sp)
    }
}

private fun evalPoly(x: Double, p: GraphPlotData.Polynomial): Double {
    return when (p.degree) {
        2 -> p.a * x * x + p.b * x + p.c
        3 -> p.a * x.pow(3) + p.b * x * x + p.c * x + p.d
        4 -> p.a * x.pow(4) + p.b * x.pow(3) + p.c * x * x + p.d * x + p.e
        else -> p.a * x * x + p.b * x + p.c
    }
}

@Composable
private fun PolynomialCanvas(
    data: GraphPlotData.Polynomial,
    zoom: Float,
    inspectX: Double?,
    onInspectXChange: (Double?) -> Unit
) {
    // 1. Calculate domain centering around roots and vertex
    val keyPointsX = mutableListOf<Double>()
    if (data.vertexX != null) keyPointsX.add(data.vertexX)
    keyPointsX.addAll(data.realRoots)
    keyPointsX.add(0.0)

    val centerX = if (data.realRoots.isNotEmpty()) data.realRoots.average() else (data.vertexX ?: 0.0)
    val span = if (data.realRoots.size >= 2) {
        maxOf(abs(data.realRoots.maxOrNull()!! - data.realRoots.minOrNull()!!), 3.0)
    } else 4.0

    val baseHalfWidth = maxOf(span * 1.3, 3.5) / zoom
    val minX = centerX - baseHalfWidth
    val maxX = centerX + baseHalfWidth

    // 2. Sample 100 points for Y range
    val samples = (0..100).map { i ->
        val x = minX + (i.toDouble() / 100.0) * (maxX - minX)
        val y = evalPoly(x, data)
        Pair(x, y)
    }
    val validY = samples.map { it.second }.filter { !it.isNaN() && !it.isInfinite() }
    val rawMinY = (validY.minOrNull() ?: -5.0).coerceAtLeast(-500.0)
    val rawMaxY = (validY.maxOrNull() ?: 5.0).coerceAtMost(500.0)
    val yCenter = (rawMinY + rawMaxY) / 2.0
    val yHalfSpan = maxOf((rawMaxY - rawMinY) * 0.6, 3.0)
    val minY = yCenter - yHalfSpan
    val maxY = yCenter + yHalfSpan
    val yRange = (maxY - minY).coerceAtLeast(1e-4)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(minX, maxX) {
                detectTapGestures { offset ->
                    val x = minX + (offset.x / size.width.toFloat()) * (maxX - minX)
                    onInspectXChange(x)
                }
            }
            .pointerInput(minX, maxX) {
                detectDragGestures { change, _ ->
                    val x = minX + (change.position.x / size.width.toFloat()) * (maxX - minX)
                    onInspectXChange(x.coerceIn(minX, maxX))
                }
            }
    ) {
        val w = size.width
        val h = size.height

        fun toPxX(x: Double): Float = (((x - minX) / (maxX - minX)) * w).toFloat()
        fun toPxY(y: Double): Float = (h - ((y - minY) / yRange) * h).toFloat()

        // 1. Grid lines
        val stepX = calculateStep(maxX - minX)
        var gx = (floor(minX / stepX) * stepX)
        while (gx <= maxX) {
            val px = toPxX(gx)
            drawLine(Color(0xFF1E293B), Offset(px, 0f), Offset(px, h), strokeWidth = 1f)
            gx += stepX
        }

        val stepY = calculateStep(maxY - minY)
        var gy = (floor(minY / stepY) * stepY)
        while (gy <= maxY) {
            val py = toPxY(gy)
            drawLine(Color(0xFF1E293B), Offset(0f, py), Offset(w, py), strokeWidth = 1f)
            gy += stepY
        }

        // 2. Coordinate Axes
        // X-axis (y = 0)
        if (0.0 in minY..maxY) {
            val zeroY = toPxY(0.0)
            drawLine(Color(0xFF475569), Offset(0f, zeroY), Offset(w, zeroY), strokeWidth = 1.8f)
            // Arrowhead
            drawLine(Color(0xFF475569), Offset(w - 6f, zeroY - 3f), Offset(w, zeroY), strokeWidth = 2f)
            drawLine(Color(0xFF475569), Offset(w - 6f, zeroY + 3f), Offset(w, zeroY), strokeWidth = 2f)
        }
        // Y-axis (x = 0)
        if (0.0 in minX..maxX) {
            val zeroX = toPxX(0.0)
            drawLine(Color(0xFF475569), Offset(zeroX, 0f), Offset(zeroX, h), strokeWidth = 1.8f)
            // Arrowhead
            drawLine(Color(0xFF475569), Offset(zeroX - 3f, 6f), Offset(zeroX, 0f), strokeWidth = 2f)
            drawLine(Color(0xFF475569), Offset(zeroX + 3f, 6f), Offset(zeroX, 0f), strokeWidth = 2f)
        }

        // 3. Continuous Polynomial Curve
        val curvePath = Path()
        var pathStarted = false
        val curveSteps = 120
        for (i in 0..curveSteps) {
            val x = minX + (i.toDouble() / curveSteps) * (maxX - minX)
            val y = evalPoly(x, data)
            if (!y.isNaN() && !y.isInfinite()) {
                val px = toPxX(x)
                val py = toPxY(y).coerceIn(-30f, h + 30f)
                if (!pathStarted) {
                    curvePath.moveTo(px, py)
                    pathStarted = true
                } else {
                    curvePath.lineTo(px, py)
                }
            }
        }
        drawPath(curvePath, color = Color(0xFF38BDF8), style = Stroke(width = 2.8f))

        // 4. Mark Real Roots on X-axis
        for (r in data.realRoots) {
            if (r in minX..maxX) {
                val rPx = toPxX(r)
                val rPy = toPxY(0.0).coerceIn(0f, h)
                // Halo
                drawCircle(Color(0xFFF59E0B).copy(alpha = 0.3f), radius = 8f, center = Offset(rPx, rPy))
                // Solid dot
                drawCircle(Color(0xFFF59E0B), radius = 4f, center = Offset(rPx, rPy))
            }
        }

        // 5. Mark Vertex (Đỉnh)
        if (data.vertexX != null && data.vertexY != null) {
            if (data.vertexX in minX..maxX && data.vertexY in minY..maxY) {
                val vPx = toPxX(data.vertexX)
                val vPy = toPxY(data.vertexY)
                drawCircle(Color(0xFF10B981).copy(alpha = 0.35f), radius = 9f, center = Offset(vPx, vPy))
                drawCircle(Color(0xFF10B981), radius = 4.5f, center = Offset(vPx, vPy))
            }
        }

        // 6. Inspection indicator
        if (inspectX != null && inspectX in minX..maxX) {
            val iPx = toPxX(inspectX)
            val iY = evalPoly(inspectX, data)
            val iPy = toPxY(iY).coerceIn(0f, h)
            drawLine(Color(0xFF94A3B8).copy(alpha = 0.5f), Offset(iPx, 0f), Offset(iPx, h), strokeWidth = 1f)
            drawCircle(Color.White, radius = 4f, center = Offset(iPx, iPy))
        }
    }
}

@Composable
private fun LinearSystemCanvas(
    data: GraphPlotData.LinearSystem2D,
    zoom: Float,
    inspectX: Double?,
    onInspectXChange: (Double?) -> Unit
) {
    val centerX = data.intersectX ?: 0.0
    val centerY = data.intersectY ?: 0.0
    val baseSpan = 6.0 / zoom
    val minX = centerX - baseSpan
    val maxX = centerX + baseSpan
    val minY = centerY - baseSpan
    val maxY = centerY + baseSpan
    val yRange = (maxY - minY).coerceAtLeast(1e-4)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        fun toPxX(x: Double): Float = (((x - minX) / (maxX - minX)) * w).toFloat()
        fun toPxY(y: Double): Float = (h - ((y - minY) / yRange) * h).toFloat()

        // 1. Grid
        val stepX = calculateStep(maxX - minX)
        var gx = (floor(minX / stepX) * stepX)
        while (gx <= maxX) {
            val px = toPxX(gx)
            drawLine(Color(0xFF1E293B), Offset(px, 0f), Offset(px, h), strokeWidth = 1f)
            gx += stepX
        }
        val stepY = calculateStep(maxY - minY)
        var gy = (floor(minY / stepY) * stepY)
        while (gy <= maxY) {
            val py = toPxY(gy)
            drawLine(Color(0xFF1E293B), Offset(0f, py), Offset(w, py), strokeWidth = 1f)
            gy += stepY
        }

        // 2. Axes
        if (0.0 in minY..maxY) {
            val zeroY = toPxY(0.0)
            drawLine(Color(0xFF475569), Offset(0f, zeroY), Offset(w, zeroY), strokeWidth = 1.8f)
        }
        if (0.0 in minX..maxX) {
            val zeroX = toPxX(0.0)
            drawLine(Color(0xFF475569), Offset(zeroX, 0f), Offset(zeroX, h), strokeWidth = 1.8f)
        }

        // 3. Line 1: a1*x + b1*y = c1 -> y = (c1 - a1*x)/b1
        if (abs(data.b1) > 1e-9) {
            val yStart = (data.c1 - data.a1 * minX) / data.b1
            val yEnd = (data.c1 - data.a1 * maxX) / data.b1
            drawLine(Color(0xFF38BDF8), Offset(toPxX(minX), toPxY(yStart)), Offset(toPxX(maxX), toPxY(yEnd)), strokeWidth = 2.5f)
        } else if (abs(data.a1) > 1e-9) {
            val vertX = data.c1 / data.a1
            val px = toPxX(vertX)
            drawLine(Color(0xFF38BDF8), Offset(px, 0f), Offset(px, h), strokeWidth = 2.5f)
        }

        // 4. Line 2: a2*x + b2*y = c2 -> y = (c2 - a2*x)/b2
        if (abs(data.b2) > 1e-9) {
            val yStart = (data.c2 - data.a2 * minX) / data.b2
            val yEnd = (data.c2 - data.a2 * maxX) / data.b2
            drawLine(Color(0xFFF59E0B), Offset(toPxX(minX), toPxY(yStart)), Offset(toPxX(maxX), toPxY(yEnd)), strokeWidth = 2.5f)
        } else if (abs(data.a2) > 1e-9) {
            val vertX = data.c2 / data.a2
            val px = toPxX(vertX)
            drawLine(Color(0xFFF59E0B), Offset(px, 0f), Offset(px, h), strokeWidth = 2.5f)
        }

        // 5. Intersection Point
        if (data.intersectX != null && data.intersectY != null) {
            val ipX = toPxX(data.intersectX)
            val ipY = toPxY(data.intersectY)
            drawCircle(Color(0xFF10B981).copy(alpha = 0.4f), radius = 10f, center = Offset(ipX, ipY))
            drawCircle(Color(0xFF10B981), radius = 5f, center = Offset(ipX, ipY))
        }
    }
}

private fun calculateStep(range: Double): Double {
    val rawStep = range / 7.0
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude
    val stepMultiplier = when {
        normalized < 1.5 -> 1.0
        normalized < 3.5 -> 2.0
        normalized < 7.5 -> 5.0
        else -> 10.0
    }
    return stepMultiplier * magnitude
}
