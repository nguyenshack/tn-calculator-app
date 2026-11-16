package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.components.EquationGraphView
import com.example.ui.components.GraphPlotData
import kotlin.math.abs

@Composable
fun EquationScreen() {
    var isSimultaneous by remember { mutableStateOf(false) } // false: Polynomial, true: Simultaneous System
    var polyDegree by remember { mutableIntStateOf(2) } // 2, 3, 4
    var simUnknowns by remember { mutableIntStateOf(2) } // 2, 3, 4

    // Polynomial Coefficients: a, b, c, d, e
    var coeffA by remember { mutableStateOf("1") }
    var coeffB by remember { mutableStateOf("-5") }
    var coeffC by remember { mutableStateOf("6") }
    var coeffD by remember { mutableStateOf("0") }
    var coeffE by remember { mutableStateOf("0") }

    // Simultaneous linear system coeffs (up to 4x4) and constants
    var simMatrix by remember {
        mutableStateOf(
            Array(4) { r -> DoubleArray(4) { c -> if (r == c) 1.0 else 0.0 } }
        )
    }
    var simConsts by remember {
        mutableStateOf(DoubleArray(4) { 0.0 })
    }

    var quadResult by remember { mutableStateOf<QuadraticResult?>(null) }
    var polyResult by remember { mutableStateOf<PolynomialResult?>(null) }
    var systemResult by remember { mutableStateOf<LinearSystemResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun solve() {
        errorMessage = null
        try {
            if (!isSimultaneous) {
                val a = coeffA.toDoubleOrNull() ?: 0.0
                val b = coeffB.toDoubleOrNull() ?: 0.0
                val c = coeffC.toDoubleOrNull() ?: 0.0
                val d = coeffD.toDoubleOrNull() ?: 0.0
                val e = coeffE.toDoubleOrNull() ?: 0.0

                when (polyDegree) {
                    2 -> {
                        quadResult = EquationSolver.solveQuadratic(a, b, c)
                        polyResult = null
                    }
                    3 -> {
                        polyResult = EquationSolver.solveCubic(a, b, c, d)
                        quadResult = null
                    }
                    4 -> {
                        polyResult = EquationSolver.solveQuartic(a, b, c, d, e)
                        quadResult = null
                    }
                }
                systemResult = null
            } else {
                val n = simUnknowns
                val subCoeffs = List(n) { r -> List(n) { c -> simMatrix[r][c] } }
                val subConsts = List(n) { r -> simConsts[r] }
                systemResult = EquationSolver.solveLinearSystem(subCoeffs, subConsts)
                quadResult = null
                polyResult = null
            }
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Lỗi giải phương trình"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Mode Switcher: Polynomial vs Simultaneous
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isSimultaneous = false },
                colors = ButtonDefaults.buttonColors(containerColor = if (!isSimultaneous) Color(0xFF0284C7) else Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Phương trình đa thức", fontSize = 11.sp)
            }
            Button(
                onClick = { isSimultaneous = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (isSimultaneous) Color(0xFF0284C7) else Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hệ phương trình bậc nhất", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Degree or Unknowns selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (!isSimultaneous) "Bậc phương trình (Degree):" else "Số ẩn (Unknowns):",
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!isSimultaneous) {
                    listOf(2, 3, 4).forEach { deg ->
                        val isSel = polyDegree == deg
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B))
                                .clickable { polyDegree = deg }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Bậc $deg", color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    listOf(2, 3, 4).forEach { unk ->
                        val isSel = simUnknowns == unk
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B))
                                .clickable { simUnknowns = unk }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("$unk ẩn", color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Form Inputs Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isSimultaneous) {
                    val polyTitle = when (polyDegree) {
                        2 -> "ax² + bx + c = 0"
                        3 -> "ax³ + bx² + cx + d = 0"
                        else -> "ax⁴ + bx³ + cx² + dx + e = 0"
                    }
                    Text(polyTitle, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = coeffA, onValueChange = { coeffA = it }, label = { Text("a", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = coeffB, onValueChange = { coeffB = it }, label = { Text("b", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = coeffC, onValueChange = { coeffC = it }, label = { Text("c", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                    }

                    if (polyDegree >= 3) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = coeffD, onValueChange = { coeffD = it }, label = { Text("d", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                            if (polyDegree >= 4) {
                                OutlinedTextField(value = coeffE, onValueChange = { coeffE = it }, label = { Text("e", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    val varNames = listOf("x", "y", "z", "w")
                    Text("HỆ $simUnknowns PHƯƠNG TRÌNH $simUnknowns ẨN:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    for (r in 0 until simUnknowns) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            for (c in 0 until simUnknowns) {
                                var valText by remember(simUnknowns, r, c, simMatrix[r][c]) {
                                    mutableStateOf(ComplexNumber.cleanDouble(simMatrix[r][c]))
                                }
                                OutlinedTextField(
                                    value = valText,
                                    onValueChange = {
                                        valText = it
                                        val num = it.toDoubleOrNull() ?: 0.0
                                        simMatrix[r][c] = num
                                    },
                                    label = { Text(varNames[c], fontSize = 8.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(46.dp)
                                )
                            }
                            Text("=", color = Color.White, fontWeight = FontWeight.Bold)
                            var constText by remember(simUnknowns, r, simConsts[r]) {
                                mutableStateOf(ComplexNumber.cleanDouble(simConsts[r]))
                            }
                            OutlinedTextField(
                                value = constText,
                                onValueChange = {
                                    constText = it
                                    val num = it.toDoubleOrNull() ?: 0.0
                                    simConsts[r] = num
                                },
                                label = { Text("Const", fontSize = 8.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(46.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { solve() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GIẢI PHƯƠNG TRÌNH (SOLVE)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results Card
        if (quadResult != null || polyResult != null || systemResult != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("KẾT QUẢ NGHIỆM CHÍNH XÁC:", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (quadResult != null) {
                        val q = quadResult!!
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biệt thức Δ (Delta):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(ComplexNumber.cleanDouble(q.delta), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                        q.roots.forEachIndexed { idx, r ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nghiệm x${idx + 1}:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(r.toCartesianString(), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (q.isMin) "Cực tiểu Parabol (x, y):" else "Cực đại Parabol (x, y):", color = Color(0xFF34D399), fontSize = 11.sp)
                            Text("(${ComplexNumber.cleanDouble(q.vertexX)}, ${ComplexNumber.cleanDouble(q.vertexY)})", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }

                    if (polyResult != null) {
                        val p = polyResult!!
                        p.roots.forEachIndexed { idx, r ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nghiệm x${idx + 1}:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(r.toCartesianString(), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                            }
                        }
                    }

                    if (systemResult != null) {
                        val varNames = listOf("x", "y", "z", "w")
                        when (val s = systemResult!!) {
                            is LinearSystemResult.Unique -> {
                                s.values.forEachIndexed { idx, v ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${varNames[idx]} =", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(ComplexNumber.cleanDouble(v), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                }
                            }
                            is LinearSystemResult.Infinite -> {
                                Text("Hệ phương trình có vô số nghiệm", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            is LinearSystemResult.NoSolution -> {
                                Text("Hệ phương trình vô nghiệm", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Interactive Graph View after solving equation
            val plotData: GraphPlotData? = remember(quadResult, polyResult, systemResult, coeffA, coeffB, coeffC, coeffD, coeffE, simMatrix, simConsts) {
                when {
                    quadResult != null -> {
                        val a = coeffA.toDoubleOrNull() ?: 1.0
                        val b = coeffB.toDoubleOrNull() ?: 0.0
                        val c = coeffC.toDoubleOrNull() ?: 0.0
                        val realRoots = quadResult!!.roots.filter { abs(it.im) < 1e-9 }.map { it.re }
                        GraphPlotData.Polynomial(
                            a = a,
                            b = b,
                            c = c,
                            degree = 2,
                            realRoots = realRoots,
                            vertexX = quadResult!!.vertexX,
                            vertexY = quadResult!!.vertexY,
                            isMin = quadResult!!.isMin
                        )
                    }
                    polyResult != null -> {
                        val a = coeffA.toDoubleOrNull() ?: 1.0
                        val b = coeffB.toDoubleOrNull() ?: 0.0
                        val c = coeffC.toDoubleOrNull() ?: 0.0
                        val d = coeffD.toDoubleOrNull() ?: 0.0
                        val e = coeffE.toDoubleOrNull() ?: 0.0
                        val realRoots = polyResult!!.roots.filter { abs(it.im) < 1e-9 }.map { it.re }
                        GraphPlotData.Polynomial(
                            a = a,
                            b = b,
                            c = c,
                            d = d,
                            e = e,
                            degree = polyDegree,
                            realRoots = realRoots
                        )
                    }
                    systemResult is LinearSystemResult.Unique && simUnknowns == 2 -> {
                        val s = systemResult as LinearSystemResult.Unique
                        GraphPlotData.LinearSystem2D(
                            a1 = simMatrix[0][0],
                            b1 = simMatrix[0][1],
                            c1 = simConsts[0],
                            a2 = simMatrix[1][0],
                            b2 = simMatrix[1][1],
                            c2 = simConsts[1],
                            intersectX = s.values.getOrNull(0),
                            intersectY = s.values.getOrNull(1)
                        )
                    }
                    else -> null
                }
            }

            if (plotData != null) {
                Spacer(modifier = Modifier.height(10.dp))
                EquationGraphView(plotData = plotData)
            }
        }
    }
}
