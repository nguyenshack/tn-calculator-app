package com.example.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.TnAiViewModel
import com.example.math.AngleUnit
import com.example.math.ComplexNumber
import com.example.math.Fraction
import com.example.math.HistoryItem
import com.example.math.MathEvaluator
import com.example.ui.components.*
import com.example.ui.screens.*

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tn_calculator_prefs", Context.MODE_PRIVATE) }
    var showLaunchSplash by rememberSaveable {
        mutableStateOf(true)
    }
    var showKeypadTour by remember { mutableStateOf(false) }
    val targetBounds = remember { mutableStateMapOf<SpotlightTargetKey, Rect>() }

    val mainInterfaceOffset by animateDpAsState(
        targetValue = if (showLaunchSplash) 200.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow),
        label = "popup_slide"
    )
    val mainInterfaceAlpha by animateFloatAsState(
        targetValue = if (showLaunchSplash) 0f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "popup_alpha"
    )

    val tnAiViewModel: TnAiViewModel = viewModel()
    var currentMode by remember { mutableStateOf(CalculatorMode.CALCULATE) }
    var angleUnit by remember { mutableStateOf(AngleUnit.DEG) }
    var contrastLevel by remember { mutableIntStateOf(3) } // 1..4
    var isDarkTheme by remember { mutableStateOf(true) }
    var lcdTheme by remember { mutableStateOf(LcdTheme.DARK) }
    var isOneHanded by remember { mutableStateOf(false) }
    var isRightHanded by remember { mutableStateOf(true) }

    val evaluator = remember { MathEvaluator(angleUnit = angleUnit) }

    LaunchedEffect(angleUnit) {
        evaluator.angleUnit = angleUnit
    }

    var expression by remember { mutableStateOf("") }
    var cursorPosition by remember { mutableIntStateOf(0) }
    var resultStr by remember { mutableStateOf("0") }
    var exactFraction by remember { mutableStateOf<String?>(null) }
    var isShiftActive by remember { mutableStateOf(false) }
    var isAlphaActive by remember { mutableStateOf(false) }
    var isEvaluated by remember { mutableStateOf(false) }
    var isHomeMenuVisible by remember { mutableStateOf(false) }
    var selectedMenuIndex by remember { mutableIntStateOf(0) }

    val historyList = remember { mutableStateListOf<HistoryItem>() }

    val homeMenuModes = remember {
        listOf(
            CalculatorMode.CALCULATE,
            CalculatorMode.STATISTICS,
            CalculatorMode.DISTRIBUTION,
            CalculatorMode.SPREADSHEET,
            CalculatorMode.TABLE,
            CalculatorMode.EQUATION
        )
    }

    fun insertToken(token: String) {
        if (isHomeMenuVisible) {
            val digit = token.toIntOrNull()
            if (digit != null && digit in 1..6) {
                currentMode = homeMenuModes[digit - 1]
                isHomeMenuVisible = false
                return
            }
        }
        if (isEvaluated) {
            val isOperator = token in listOf("+", "−", "×", "÷", "^", "%", "R") ||
                    token.startsWith("^") || token.startsWith("*") || token.startsWith("/")
            if (isOperator) {
                expression = "Ans" + token
                cursorPosition = expression.length
            } else {
                expression = token
                cursorPosition = token.length
                resultStr = "0"
                exactFraction = null
            }
            isEvaluated = false
            return
        }
        val cur = cursorPosition.coerceIn(0, expression.length)
        expression = expression.substring(0, cur) + token + expression.substring(cur)
        cursorPosition = cur + token.length
    }

    fun deleteChar() {
        isEvaluated = false
        val cur = cursorPosition.coerceIn(0, expression.length)
        if (cur > 0 && expression.isNotEmpty()) {
            val before = expression.substring(0, cur)
            val after = expression.substring(cur)
            if (before.endsWith("()/()")) {
                expression = before.substring(0, before.length - 5) + after
                cursorPosition = cur - 5
                return
            }
            if (before.endsWith("^()")) {
                expression = before.substring(0, before.length - 3) + after
                cursorPosition = cur - 3
                return
            }
            if (before.endsWith("sqrt()")) {
                expression = before.substring(0, before.length - 6) + after
                cursorPosition = cur - 6
                return
            }
            expression = expression.substring(0, cur - 1) + expression.substring(cur)
            cursorPosition = cur - 1
        }
    }

    fun navigateCursorUp() {
        isEvaluated = false
        if (isHomeMenuVisible) {
            if (selectedMenuIndex >= 3) selectedMenuIndex -= 3
            return
        }
        val segments = try { com.example.ui.components.NaturalMathParser.parse(expression) } catch (e: Exception) { emptyList() }
        for (seg in segments) {
            if (seg is com.example.ui.components.MathSegment.Fraction && cursorPosition in seg.denStart..seg.denEnd) {
                cursorPosition = seg.numEnd
                return
            }
        }
        cursorPosition = 0
    }

    fun navigateCursorDown() {
        isEvaluated = false
        if (isHomeMenuVisible) {
            if (selectedMenuIndex < 3) selectedMenuIndex += 3
            return
        }
        val segments = try { com.example.ui.components.NaturalMathParser.parse(expression) } catch (e: Exception) { emptyList() }
        for (seg in segments) {
            if (seg is com.example.ui.components.MathSegment.Fraction && cursorPosition in seg.numStart..seg.numEnd) {
                cursorPosition = seg.denStart
                return
            }
            if (seg is com.example.ui.components.MathSegment.Power && cursorPosition in seg.expStart..seg.expEnd) {
                cursorPosition = seg.endIndex
                return
            }
        }
        cursorPosition = expression.length
    }

    fun navigateCursorRight() {
        isEvaluated = false
        if (isHomeMenuVisible) {
            selectedMenuIndex = (selectedMenuIndex + 1) % 6
            return
        }
        val segments = try { com.example.ui.components.NaturalMathParser.parse(expression) } catch (e: Exception) { emptyList() }
        for (seg in segments) {
            if (seg is com.example.ui.components.MathSegment.Fraction) {
                if (cursorPosition == seg.numEnd) {
                    cursorPosition = seg.denStart
                    return
                }
                if (cursorPosition == seg.denEnd) {
                    cursorPosition = seg.endIndex
                    return
                }
            }
            if (seg is com.example.ui.components.MathSegment.Power && cursorPosition == seg.expEnd) {
                cursorPosition = seg.endIndex
                return
            }
            if (seg is com.example.ui.components.MathSegment.Radical && cursorPosition == seg.innerEnd) {
                cursorPosition = seg.endIndex
                return
            }
        }
        if (cursorPosition < expression.length) cursorPosition++
    }

    fun navigateCursorLeft() {
        isEvaluated = false
        if (isHomeMenuVisible) {
            selectedMenuIndex = (selectedMenuIndex - 1 + 6) % 6
            return
        }
        val segments = try { com.example.ui.components.NaturalMathParser.parse(expression) } catch (e: Exception) { emptyList() }
        for (seg in segments) {
            if (seg is com.example.ui.components.MathSegment.Fraction) {
                if (cursorPosition == seg.denStart) {
                    cursorPosition = seg.numEnd
                    return
                }
                if (cursorPosition == seg.numStart) {
                    cursorPosition = seg.startIndex
                    return
                }
            }
            if (seg is com.example.ui.components.MathSegment.Power && cursorPosition == seg.expStart) {
                cursorPosition = seg.startIndex
                return
            }
            if (seg is com.example.ui.components.MathSegment.Radical && cursorPosition == seg.innerStart) {
                cursorPosition = seg.startIndex
                return
            }
        }
        if (cursorPosition > 0) cursorPosition--
    }

    fun clearAll() {
        expression = ""
        cursorPosition = 0
        resultStr = "0"
        exactFraction = null
        isShiftActive = false
        isAlphaActive = false
        isEvaluated = false
    }

    // Modals visibility
    var showMenuModal by remember { mutableStateOf(false) }
    var showVarModal by remember { mutableStateOf(false) }
    var showQrModal by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var showToolsHubModal by remember { mutableStateOf(false) }
    var showCatalogHubModal by remember { mutableStateOf(false) }
    var showSolveModal by remember { mutableStateOf(false) }
    var showCalcModal by remember { mutableStateOf(false) }
    var showFactModal by remember { mutableStateOf(false) }
    var showGcdLcmModal by remember { mutableStateOf(false) }
    var showSumProdModal by remember { mutableStateOf(false) }

    BackHandler(enabled = currentMode != CalculatorMode.CALCULATE || isHomeMenuVisible) {
        if (isHomeMenuVisible) {
            isHomeMenuVisible = false
        } else {
            currentMode = CalculatorMode.CALCULATE
        }
    }

    fun evaluateCalculation() {
        if (expression.isBlank()) return
        try {
            val num = evaluator.evaluate(expression)
            resultStr = ComplexNumber.cleanDouble(num)
            val frac = Fraction.fromDouble(num)
            exactFraction = frac?.toString()
            cursorPosition = expression.length
            isEvaluated = true
            historyList.add(
                0,
                HistoryItem(
                    expression = expression,
                    result = resultStr,
                    exactFraction = exactFraction
                )
            )
        } catch (e: Exception) {
            resultStr = e.message ?: "Math ERROR"
            exactFraction = null
            isEvaluated = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = mainInterfaceOffset)
                .alpha(mainInterfaceAlpha),
            contentWindowInsets = WindowInsets.systemBars,
            containerColor = if (isDarkTheme) Color(0xFF101216) else Color(0xFFF4F6F9),
        topBar = {
            if (currentMode != CalculatorMode.TN_AI) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) Color(0xFF161920) else Color(0xFFFFFFFF))
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left Brand: TN-001 + Mode Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { isHomeMenuVisible = !isHomeMenuVisible }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDarkTheme) Color(0xFF2563EB) else Color(0xFF3B82F6),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Text(
                                text = "TN-001",
                                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            // Current active mode indicator tag
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isDarkTheme) Color(0xFF1E2430) else Color(0xFFE2E8F0),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = currentMode.title,
                                    color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                    // Top Quick Controls: TN AI FIRST (Always visible!), One-Handed, History, Theme, Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // PROMINENT TN AI BUTTON - ALWAYS VISIBLE!
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (currentMode == CalculatorMode.TN_AI) Color(0xFF7C3AED) else Color(0xFF8B5CF6),
                            modifier = Modifier
                                .clickable {
                                    currentMode = if (currentMode == CalculatorMode.TN_AI) CalculatorMode.CALCULATE else CalculatorMode.TN_AI
                                }
                                .onGloballyPositioned { targetBounds[SpotlightTargetKey.TOP_AI_BUTTON] = it.boundsInRoot() }
                                .testTag("top_tn_ai_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "TN AI Giải bài tập",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "TN AI",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // One-Handed Mode Toggle
                        IconButton(
                            onClick = { isOneHanded = !isOneHanded },
                            modifier = Modifier.size(28.dp).testTag("top_one_handed_toggle")
                        ) {
                            Icon(
                                imageVector = if (isOneHanded) Icons.Default.Swipe else Icons.Default.PanTool,
                                contentDescription = "Chế độ 1 tay",
                                tint = if (isOneHanded) Color(0xFF10B981) else if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Angle Unit Toggle Button (DEG ⇄ RAD)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (angleUnit == AngleUnit.DEG)
                                (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                            else
                                (if (isDarkTheme) Color(0xFF312E81) else Color(0xFFE0E7FF)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (angleUnit == AngleUnit.DEG)
                                    (if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1))
                                else
                                    (if (isDarkTheme) Color(0xFF6366F1) else Color(0xFF818CF8))
                            ),
                            modifier = Modifier
                                .clickable {
                                    angleUnit = if (angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
                                    if (expression.isNotBlank() && isEvaluated) {
                                        evaluateCalculation()
                                    }
                                }
                                .testTag("top_angle_unit_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = angleUnit.name,
                                    color = if (angleUnit == AngleUnit.DEG)
                                        (if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7))
                                    else
                                        (if (isDarkTheme) Color(0xFFA5B4FC) else Color(0xFF4F46E5)),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Calculation History
                        IconButton(
                            onClick = { showHistoryModal = true },
                            modifier = Modifier.size(28.dp).testTag("top_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Lịch sử tính toán",
                                tint = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Light / Dark Mode Toggle
                        IconButton(
                            onClick = {
                                isDarkTheme = !isDarkTheme
                                lcdTheme = if (isDarkTheme) LcdTheme.DARK else LcdTheme.LIGHT
                            },
                            modifier = Modifier.size(28.dp).testTag("theme_mode_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Đổi chế độ Sáng / Tối",
                                tint = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFF475569),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // All 16 Apps Menu
                        IconButton(
                            onClick = { showMenuModal = true },
                            modifier = Modifier.size(28.dp).testTag("top_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "Menu 16 ứng dụng",
                                tint = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    ) { innerPadding ->
        // Responsive Container that adapts dynamically to Screen Size
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 640.dp
            val screenHeight = maxHeight
            val buttonSize = if (screenHeight < 680.dp) 38.dp else 43.dp

            if (isWideScreen) {
                // Wide / Tablet Dual-Pane Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: Large LCD Screen and Active Tool / Results View
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    ) {
                        LcdScreen(
                            expression = expression,
                            result = resultStr,
                            angleUnit = angleUnit,
                            isShiftActive = isShiftActive,
                            isAlphaActive = isAlphaActive,
                            contrastLevel = contrastLevel,
                            lcdTheme = lcdTheme,
                            cursorPosition = cursorPosition,
                            exactFraction = exactFraction,
                            isHomeMenuVisible = isHomeMenuVisible,
                            selectedModeIndex = selectedMenuIndex,
                            onSelectHomeMode = {
                                currentMode = it
                                isHomeMenuVisible = false
                            },
                            onToggleSd = {
                                val num = resultStr.toDoubleOrNull()
                                if (num != null) {
                                    val frac = Fraction.fromDouble(num)
                                    exactFraction = frac?.toString()
                                }
                            },
                            onOpenQr = { showQrModal = true },
                            onCycleContrast = {
                                contrastLevel = if (contrastLevel >= 4) 1 else contrastLevel + 1
                            },
                            onToggleHomeMenu = { isHomeMenuVisible = !isHomeMenuVisible },
                            onToggleAngleUnit = {
                                angleUnit = if (angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
                                if (expression.isNotBlank() && isEvaluated) {
                                    evaluateCalculation()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { targetBounds[SpotlightTargetKey.LCD_SCREEN] = it.boundsInRoot() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Active Mode Subscreen Container
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDarkTheme) Color(0xFF161920) else Color(0xFFFFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF262A33) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (currentMode) {
                                CalculatorMode.CALCULATE -> {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "TN-001 - Chế độ Tính toán Chuyên nghiệp",
                                            color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF2563EB),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Nhập phép tính ở bàn phím bên phải hoặc chọn các ứng dụng nâng cao từ nút HOME / MENU biểu tượng.",
                                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                CalculatorMode.COMPLEX -> ComplexScreen(angleUnit = angleUnit, onSendToCalc = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.MATRIX -> MatrixScreen()
                                CalculatorMode.VECTOR -> VectorScreen(angleUnit = angleUnit)
                                CalculatorMode.STATISTICS -> StatisticsScreen()
                                CalculatorMode.DISTRIBUTION -> DistributionScreen()
                                CalculatorMode.SPREADSHEET -> SpreadsheetScreen()
                                CalculatorMode.TABLE -> TableScreen()
                                CalculatorMode.EQUATION -> EquationScreen()
                                CalculatorMode.INEQUALITY -> InequalityScreen()
                                CalculatorMode.RATIO -> RatioScreen()
                                CalculatorMode.BASE_N -> BaseNScreen()
                                CalculatorMode.MATH_BOX -> MathBoxScreen()
                                CalculatorMode.VERIFY -> VerifyScreen()
                                CalculatorMode.ATOMIC_TABLE -> AtomicTableScreen(onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.CONVERSION -> ConversionScreen(onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.FINANCE -> FinanceScreen(isDarkTheme = isDarkTheme, onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.HEALTH -> HealthScreen(isDarkTheme = isDarkTheme, onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.DATE_TIME -> DateTimeScreen(isDarkTheme = isDarkTheme, onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.TN_AI -> TnAiScreen(
                                    viewModel = tnAiViewModel,
                                    isDarkTheme = isDarkTheme,
                                    onBackToCalculator = { currentMode = CalculatorMode.CALCULATE },
                                    onSendToCalculator = { rawFormula ->
                                        val clean = com.example.math.CalculatorFormulaSanitizer.sanitize(rawFormula)
                                        expression = clean
                                        cursorPosition = clean.length
                                        isEvaluated = false
                                        currentMode = CalculatorMode.CALCULATE
                                        evaluateCalculation()
                                    },
                                    onOpenEquation = { currentMode = CalculatorMode.EQUATION }
                                )
                            }
                        }
                    }

                    // Right Pane: TN-001 Symmetrical Keypad
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkTheme) Color(0xFF161920) else Color(0xFFFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF262A33) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                    ) {
                        CalculateScreen(
                            evaluator = evaluator,
                            expression = expression,
                            resultStr = resultStr,
                            cursorPosition = cursorPosition,
                            isShiftActive = isShiftActive,
                            isAlphaActive = isAlphaActive,
                            exactFraction = exactFraction,
                            isDarkTheme = isDarkTheme,
                            isOneHanded = isOneHanded,
                            isRightHanded = isRightHanded,
                            onInsertToken = { insertToken(it) },
                            onEvaluate = {
                                if (isHomeMenuVisible) {
                                    currentMode = homeMenuModes[selectedMenuIndex]
                                    isHomeMenuVisible = false
                                } else {
                                    evaluateCalculation()
                                }
                            },
                            onToggleShift = { isShiftActive = !isShiftActive },
                            onToggleAlpha = { isAlphaActive = !isAlphaActive },
                            onToggleHomeMenu = { isHomeMenuVisible = !isHomeMenuVisible },
                            onCursorLeft = { navigateCursorLeft() },
                            onCursorRight = { navigateCursorRight() },
                            onCursorUp = { navigateCursorUp() },
                            onCursorDown = { navigateCursorDown() },
                            onOpenSolveDialog = { showToolsHubModal = true },
                            onOpenCalcDialog = { showCalcModal = true },
                            onOpenFactDialog = { showCatalogHubModal = true },
                            onOpenGcdLcmDialog = { showGcdLcmModal = true },
                            onOpenSumProdDialog = { showSumProdModal = true },
                            onOpenVarList = { showVarModal = true },
                            onClearAll = { clearAll() },
                            onDeleteChar = { deleteChar() },
                            onToggleSd = {
                                val num = resultStr.toDoubleOrNull()
                                if (num != null) {
                                    val frac = Fraction.fromDouble(num)
                                    exactFraction = frac?.toString()
                                }
                            },
                            onUpdateExpressionAndCursor = { expr, cur ->
                                expression = expr
                                cursorPosition = cur
                                isEvaluated = false
                            },
                            onReportTargetBounds = { key, rect -> targetBounds[key] = rect }
                        )
                    }
                }
            } else {
                // Portrait Handheld Calculator Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentMode == CalculatorMode.CALCULATE) {
                        // Large Display Screen on Top
                        LcdScreen(
                            expression = expression,
                            result = resultStr,
                            angleUnit = angleUnit,
                            isShiftActive = isShiftActive,
                            isAlphaActive = isAlphaActive,
                            contrastLevel = contrastLevel,
                            lcdTheme = lcdTheme,
                            cursorPosition = cursorPosition,
                            exactFraction = exactFraction,
                            isHomeMenuVisible = isHomeMenuVisible,
                            selectedModeIndex = selectedMenuIndex,
                            onSelectHomeMode = {
                                currentMode = it
                                isHomeMenuVisible = false
                            },
                            onToggleSd = {
                                val num = resultStr.toDoubleOrNull()
                                if (num != null) {
                                    val frac = Fraction.fromDouble(num)
                                    exactFraction = frac?.toString()
                                }
                            },
                            onOpenQr = { showQrModal = true },
                            onCycleContrast = {
                                contrastLevel = if (contrastLevel >= 4) 1 else contrastLevel + 1
                            },
                            onToggleHomeMenu = { isHomeMenuVisible = !isHomeMenuVisible },
                            onToggleAngleUnit = {
                                angleUnit = if (angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
                                if (expression.isNotBlank() && isEvaluated) {
                                    evaluateCalculation()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { targetBounds[SpotlightTargetKey.LCD_SCREEN] = it.boundsInRoot() }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Full TN-001 Symmetrical Flat Keypad
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            CalculateScreen(
                                evaluator = evaluator,
                                expression = expression,
                                resultStr = resultStr,
                                cursorPosition = cursorPosition,
                                isShiftActive = isShiftActive,
                                isAlphaActive = isAlphaActive,
                                exactFraction = exactFraction,
                                isDarkTheme = isDarkTheme,
                                isOneHanded = isOneHanded,
                                isRightHanded = isRightHanded,
                                onInsertToken = { insertToken(it) },
                                onEvaluate = {
                                    if (isHomeMenuVisible) {
                                        currentMode = homeMenuModes[selectedMenuIndex]
                                        isHomeMenuVisible = false
                                    } else {
                                        evaluateCalculation()
                                    }
                                },
                                onToggleShift = { isShiftActive = !isShiftActive },
                                onToggleAlpha = { isAlphaActive = !isAlphaActive },
                                onToggleHomeMenu = { isHomeMenuVisible = !isHomeMenuVisible },
                                onCursorLeft = { navigateCursorLeft() },
                                onCursorRight = { navigateCursorRight() },
                                onCursorUp = { navigateCursorUp() },
                                onCursorDown = { navigateCursorDown() },
                                onOpenSolveDialog = { showToolsHubModal = true },
                                onOpenCalcDialog = { showCalcModal = true },
                                onOpenFactDialog = { showCatalogHubModal = true },
                                onOpenGcdLcmDialog = { showGcdLcmModal = true },
                                onOpenSumProdDialog = { showSumProdModal = true },
                                onOpenVarList = { showVarModal = true },
                                onClearAll = { clearAll() },
                                onDeleteChar = { deleteChar() },
                                onToggleSd = {
                                    val num = resultStr.toDoubleOrNull()
                                    if (num != null) {
                                        val frac = Fraction.fromDouble(num)
                                        exactFraction = frac?.toString()
                                    }
                                },
                                onUpdateExpressionAndCursor = { expr, cur ->
                                    expression = expr
                                    cursorPosition = cur
                                    isEvaluated = false
                                },
                                onReportTargetBounds = { key, rect -> targetBounds[key] = rect }
                            )
                        }
                    } else {
                        // Header bar when inside a sub-tool (Equation, Matrix, Math Box...), hidden for TN AI which has its own sleek header
                        if (currentMode != CalculatorMode.TN_AI) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Button(
                                    onClick = { currentMode = CalculatorMode.CALCULATE },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF1E2430) else Color(0xFFE2E8F0)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Về Máy tính", color = if (isDarkTheme) Color.White else Color(0xFF0F172A), fontSize = 11.sp)
                                }

                                Text(
                                    text = currentMode.title,
                                    color = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (currentMode) {
                                CalculatorMode.COMPLEX -> ComplexScreen(angleUnit = angleUnit, onSendToCalc = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.MATRIX -> MatrixScreen()
                                CalculatorMode.VECTOR -> VectorScreen(angleUnit = angleUnit)
                                CalculatorMode.STATISTICS -> StatisticsScreen()
                                CalculatorMode.DISTRIBUTION -> DistributionScreen()
                                CalculatorMode.SPREADSHEET -> SpreadsheetScreen()
                                CalculatorMode.TABLE -> TableScreen()
                                CalculatorMode.EQUATION -> EquationScreen()
                                CalculatorMode.INEQUALITY -> InequalityScreen()
                                CalculatorMode.RATIO -> RatioScreen()
                                CalculatorMode.BASE_N -> BaseNScreen()
                                CalculatorMode.MATH_BOX -> MathBoxScreen()
                                CalculatorMode.VERIFY -> VerifyScreen()
                                CalculatorMode.ATOMIC_TABLE -> AtomicTableScreen(onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.CONVERSION -> ConversionScreen(onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.FINANCE -> FinanceScreen(isDarkTheme = isDarkTheme, onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.HEALTH -> HealthScreen(isDarkTheme = isDarkTheme, onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.DATE_TIME -> DateTimeScreen(isDarkTheme = isDarkTheme, onPasteToCalculator = { insertToken(it); currentMode = CalculatorMode.CALCULATE })
                                CalculatorMode.TN_AI -> TnAiScreen(
                                    viewModel = tnAiViewModel,
                                    isDarkTheme = isDarkTheme,
                                    onBackToCalculator = { currentMode = CalculatorMode.CALCULATE },
                                    onSendToCalculator = { rawFormula ->
                                        val clean = com.example.math.CalculatorFormulaSanitizer.sanitize(rawFormula)
                                        expression = clean
                                        cursorPosition = clean.length
                                        isEvaluated = false
                                        currentMode = CalculatorMode.CALCULATE
                                        evaluateCalculation()
                                    },
                                    onOpenEquation = { currentMode = CalculatorMode.EQUATION }
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showMenuModal) {
        IconMenuModal(
            currentMode = currentMode,
            onSelectMode = {
                currentMode = it
                isHomeMenuVisible = false
            },
            onDismiss = { showMenuModal = false },
            onReplaySplash = {
                showMenuModal = false
                showLaunchSplash = true
            },
            onOpenKeypadTour = {
                showMenuModal = false
                showKeypadTour = true
            }
        )
    }

    if (showVarModal) {
        VariableListModal(
            variables = evaluator.variables,
            ansValue = evaluator.ans,
            onSetVariable = { name, v -> evaluator.setVariable(name, v) },
            onInsertVariable = { name -> insertToken(name) },
            onClearAll = {
                listOf("A", "B", "C", "D", "E", "F", "x", "y", "z").forEach {
                    evaluator.setVariable(it, 0.0)
                }
            },
            onDismiss = { showVarModal = false }
        )
    }

    if (showQrModal) {
        val payload = "TN-001 Calculator:\nExpression: $expression\nResult: $resultStr" +
                (if (exactFraction != null) "\nExact Fraction: $exactFraction" else "")
        QrCodeDialog(
            title = "Kết quả phép tính",
            dataToEncode = payload,
            onDismiss = { showQrModal = false }
        )
    }

    if (showSolveModal) {
        SolveDialog(
            evaluator = evaluator,
            initialEquation = expression,
            onApplySolution = {
                resultStr = ComplexNumber.cleanDouble(it)
                showSolveModal = false
            },
            onDismiss = { showSolveModal = false }
        )
    }

    if (showCalcModal) {
        CalcDialog(
            evaluator = evaluator,
            currentExpression = expression,
            onResultComputed = {
                resultStr = ComplexNumber.cleanDouble(it)
                showCalcModal = false
            },
            onDismiss = { showCalcModal = false }
        )
    }

    if (showFactModal) {
        val ansLong = evaluator.ans.toLong()
        FactDialog(
            initialValue = if (ansLong > 0) ansLong else 3600L,
            onDismiss = { showFactModal = false }
        )
    }

    if (showGcdLcmModal) {
        GcdLcmDialog(onDismiss = { showGcdLcmModal = false })
    }

    if (showSumProdModal) {
        SumProdDialog(
            evaluator = evaluator,
            onResultComputed = {
                resultStr = ComplexNumber.cleanDouble(it)
                showSumProdModal = false
            },
            onDismiss = { showSumProdModal = false }
        )
    }

    if (showHistoryModal) {
        HistoryDialog(
            historyList = historyList,
            isDarkTheme = isDarkTheme,
            onSelectExpression = {
                expression = it
                cursorPosition = it.length
                isEvaluated = false
                showHistoryModal = false
            },
            onSelectResult = {
                insertToken(it)
                showHistoryModal = false
            },
            onDeleteItem = { id ->
                historyList.removeAll { it.id == id }
            },
            onClearAll = {
                historyList.clear()
            },
            onDismiss = { showHistoryModal = false }
        )
    }

    if (showToolsHubModal) {
        ToolsHubDialog(
            evaluator = evaluator,
            currentExpression = expression,
            isDarkTheme = isDarkTheme,
            onOpenSolve = { showSolveModal = true },
            onOpenCalc = { showCalcModal = true },
            onOpenGcdLcm = { showGcdLcmModal = true },
            onOpenSumProd = { showSumProdModal = true },
            onOpenFinance = { currentMode = CalculatorMode.FINANCE },
            onOpenHealth = { currentMode = CalculatorMode.HEALTH },
            onOpenDateTime = { currentMode = CalculatorMode.DATE_TIME },
            onOpenHistory = { showHistoryModal = true },
            onDismiss = { showToolsHubModal = false }
        )
    }

    if (showCatalogHubModal) {
        CatalogHubDialog(
            onOpenFact = { showFactModal = true },
            onOpenConversion = { currentMode = CalculatorMode.CONVERSION },
            onOpenBaseN = { currentMode = CalculatorMode.BASE_N },
            onOpenAtomicTable = { currentMode = CalculatorMode.ATOMIC_TABLE },
            onInsertSymbol = { insertToken(it) },
            isDarkTheme = isDarkTheme,
            onDismiss = { showCatalogHubModal = false }
        )
    }

    if (showLaunchSplash) {
        LaunchSplashOverlay(
            onAnimationFinished = {
                showLaunchSplash = false
                if (!prefs.getBoolean("has_completed_keypad_tour", false)) {
                    showKeypadTour = true
                }
            }
        )
    }

    if (showKeypadTour) {
        KeypadSpotlightTourOverlay(
            targetBounds = targetBounds,
            onDismissTour = {
                showKeypadTour = false
                prefs.edit().putBoolean("has_completed_keypad_tour", true).apply()
            }
        )
    }
}
}


