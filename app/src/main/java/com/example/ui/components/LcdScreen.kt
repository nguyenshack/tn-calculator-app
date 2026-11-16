package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleUnit
import com.example.ui.CalculatorMode
import com.example.ui.LcdTheme

data class LcdColorPalette(
    val screenBg: Color,
    val contentColor: Color,
    val statusColor: Color,
    val gridBorder: Color,
    val containerBg: Color
)

@Composable
fun LcdScreen(
    expression: String,
    result: String,
    angleUnit: AngleUnit,
    isShiftActive: Boolean,
    isAlphaActive: Boolean,
    contrastLevel: Int, // 1..4
    lcdTheme: LcdTheme,
    modifier: Modifier = Modifier,
    cursorPosition: Int = expression.length,
    hasAns: Boolean = true,
    exactFraction: String? = null,
    isHomeMenuVisible: Boolean = false,
    selectedModeIndex: Int = 0,
    onSelectHomeMode: (CalculatorMode) -> Unit = {},
    onToggleSd: () -> Unit = {},
    onOpenQr: () -> Unit = {},
    onCycleContrast: () -> Unit = {},
    onToggleHomeMenu: () -> Unit = {},
    onToggleAngleUnit: () -> Unit = {}
) {
    val contrastAlpha = when (contrastLevel) {
        1 -> 0.75f
        2 -> 0.88f
        3 -> 1.0f
        else -> 1.0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink_transition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    val palette = when (lcdTheme) {
        LcdTheme.DARK -> LcdColorPalette(
            screenBg = Color(0xFF1E222B),
            contentColor = Color(0xFFF8FAFC),
            statusColor = Color(0xFF94A3B8),
            gridBorder = Color(0xFF333A48),
            containerBg = Color(0xFF14171E)
        )
        LcdTheme.LIGHT -> LcdColorPalette(
            screenBg = Color(0xFFF8FAFC),
            contentColor = Color(0xFF0F172A),
            statusColor = Color(0xFF475569),
            gridBorder = Color(0xFFE2E8F0),
            containerBg = Color(0xFFEDF1F7)
        )
        LcdTheme.AMOLED -> LcdColorPalette(
            screenBg = Color(0xFF000000),
            contentColor = Color(0xFF38BDF8),
            statusColor = Color(0xFF0284C7),
            gridBorder = Color(0xFF1E293B),
            containerBg = Color(0xFF0A0C10)
        )
        LcdTheme.SOFT_BLUE -> LcdColorPalette(
            screenBg = Color(0xFF172033),
            contentColor = Color(0xFF93C5FD),
            statusColor = Color(0xFF60A5FA),
            gridBorder = Color(0xFF25334D),
            containerBg = Color(0xFF0F1626)
        )
    }

    // Modern Flat Android Display Card (Zero shadows, clean Material 3 surface)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.containerBg)
            .border(1.dp, palette.gridBorder, RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        // Inner Display Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.screenBg)
                .border(1.dp, palette.gridBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // 1. Top Status Icons Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isShiftActive) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "SHIFT",
                                color = Color(0xFF78350F),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    if (isAlphaActive) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFB7185), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ALPHA",
                                color = Color(0xFF4C0519),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (angleUnit == AngleUnit.RAD) Color(0xFF6366F1).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { onToggleAngleUnit() }
                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = angleUnit.name,
                            color = if (angleUnit == AngleUnit.RAD) Color(0xFF818CF8) else palette.statusColor.copy(alpha = contrastAlpha),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "MATH",
                        color = palette.statusColor.copy(alpha = contrastAlpha),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (hasAns) {
                        Text(
                            text = "Ans",
                            color = palette.statusColor.copy(alpha = contrastAlpha * 0.85f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Quick Mode and Tool Shortcuts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.statusColor.copy(alpha = 0.15f))
                            .clickable { onCycleContrast() }
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "L$contrastLevel",
                            color = palette.statusColor.copy(alpha = contrastAlpha),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onOpenQr,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Mã QR",
                            tint = palette.contentColor.copy(alpha = contrastAlpha),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleHomeMenu,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = if (isHomeMenuVisible) Icons.Default.Calculate else Icons.Default.GridView,
                            contentDescription = "Chuyển giao diện Menu/Tính toán",
                            tint = palette.contentColor.copy(alpha = contrastAlpha),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isHomeMenuVisible) {
                // 6-BOX ICON MENU FOR MAIN MODES
                val menuModes = listOf(
                    CalculatorMode.CALCULATE,
                    CalculatorMode.STATISTICS,
                    CalculatorMode.DISTRIBUTION,
                    CalculatorMode.SPREADSHEET,
                    CalculatorMode.TABLE,
                    CalculatorMode.EQUATION
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(98.dp)
                        .padding(vertical = 2.dp)
                ) {
                    for (row in 0 until 2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (col in 0 until 3) {
                                val idx = row * 3 + col
                                val mode = menuModes[idx]
                                val isSelected = idx == selectedModeIndex

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) palette.contentColor else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) palette.contentColor else palette.statusColor.copy(alpha = 0.35f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectHomeMode(mode) }
                                        ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = mode.icon,
                                            contentDescription = mode.title,
                                            tint = if (isSelected) palette.screenBg else palette.contentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = mode.title,
                                            color = if (isSelected) palette.screenBg else palette.contentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "[${idx + 1}]",
                                            color = if (isSelected) palette.screenBg.copy(alpha = 0.8f) else palette.statusColor,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                        if (row == 0) Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            } else {
                // Natural Multi-Line Calculation Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(98.dp)
                        .clickable { onToggleHomeMenu() }
                ) {
                    val exprScroll = rememberScrollState()

                    LaunchedEffect(cursorPosition, expression.length) {
                        if (exprScroll.maxValue > 0) {
                            val targetScroll = ((cursorPosition.toFloat() / maxOf(1, expression.length)) * exprScroll.maxValue).toInt()
                            exprScroll.animateScrollTo(targetScroll)
                        }
                    }

                    // Top expression line with Natural Textbook Display (fractions, radicals, powers, and cursor)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .horizontalScroll(exprScroll)
                    ) {
                        NaturalMathDisplay(
                            expression = expression,
                            cursorPosition = cursorPosition,
                            contentColor = palette.contentColor,
                            contrastAlpha = contrastAlpha,
                            cursorAlpha = cursorAlpha
                        )
                    }

                    // Bottom result & Natural S<=>D fraction line
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        if (exactFraction != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(palette.statusColor.copy(alpha = 0.20f))
                                    .clickable { onToggleSd() }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "S⇔D ",
                                        color = palette.contentColor.copy(alpha = contrastAlpha),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    val parts = exactFraction.split("/")
                                    if (parts.size == 2) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = parts[0],
                                                color = palette.contentColor.copy(alpha = contrastAlpha),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(14.dp)
                                                    .height(1.2.dp)
                                                    .background(palette.contentColor.copy(alpha = contrastAlpha))
                                            )
                                            Text(
                                                text = parts[1],
                                                color = palette.contentColor.copy(alpha = contrastAlpha),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = exactFraction,
                                            color = palette.contentColor.copy(alpha = contrastAlpha),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = result,
                            color = palette.contentColor.copy(alpha = contrastAlpha),
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
