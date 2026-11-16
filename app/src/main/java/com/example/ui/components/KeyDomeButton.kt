package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class KeyDomeType {
    NUMBER,
    FUNCTION,
    OPERATION,
    ACTION_CLEAR, // AC / DEL
    SHIFT,
    ALPHA,
    EQUALS,
    HOME,
    DPAD
}

@Composable
fun LargeDomeButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shiftLabel: String? = null,
    alphaLabel: String? = null,
    keyType: KeyDomeType = KeyDomeType.FUNCTION,
    fontSize: Int = 16,
    isCircle: Boolean = false,
    isDarkTheme: Boolean = true,
    isActive: Boolean = false,
    testTag: String = "key_${label.lowercase()}"
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Clean, flat Android Material 3 design with distinct dividing borders to prevent misclicks
    val (bgColor, textColor, borderColor) = if (isDarkTheme) {
        when (keyType) {
            KeyDomeType.NUMBER -> Triple(
                if (isPressed) Color(0xFF2C313C) else Color(0xFF21252D),
                Color(0xFFF8FAFC),
                Color(0xFF475569) // slate-600 crisp dividing border
            )
            KeyDomeType.OPERATION -> Triple(
                if (isPressed) Color(0xFF1E3A5F) else Color(0xFF1A2E4B),
                Color(0xFF60A5FA),
                Color(0xFF3B82F6) // blue-500 distinct border
            )
            KeyDomeType.EQUALS -> Triple(
                if (isPressed) Color(0xFF1D4ED8) else Color(0xFF2563EB),
                Color.White,
                Color(0xFF93C5FD) // blue-300 bright boundary
            )
            KeyDomeType.ACTION_CLEAR -> Triple(
                if (isPressed) Color(0xFF4C1D24) else Color(0xFF3B181E),
                Color(0xFFF87171),
                Color(0xFFEF4444) // red-500
            )
            KeyDomeType.SHIFT -> if (isActive) {
                Triple(Color(0xFFD97706), Color.Black, Color(0xFFFDE68A))
            } else {
                Triple(
                    if (isPressed) Color(0xFF4A3412) else Color(0xFF3A280E),
                    Color(0xFFFBBF24),
                    Color(0xFFF59E0B) // amber-500
                )
            }
            KeyDomeType.ALPHA -> if (isActive) {
                Triple(Color(0xFFE11D48), Color.White, Color(0xFFFECDD3))
            } else {
                Triple(
                    if (isPressed) Color(0xFF4C1826) else Color(0xFF3B131E),
                    Color(0xFFFB7185),
                    Color(0xFFF43F5E) // rose-500
                )
            }
            KeyDomeType.FUNCTION -> Triple(
                if (isPressed) Color(0xFF272B35) else Color(0xFF1C1F26),
                Color(0xFFCBD5E1),
                Color(0xFF475569) // slate-600
            )
            KeyDomeType.HOME, KeyDomeType.DPAD -> Triple(
                if (isPressed) Color(0xFF2C323E) else Color(0xFF212630),
                Color(0xFFE2E8F0),
                Color(0xFF64748B) // slate-500
            )
        }
    } else {
        // Light Theme: Clean Android flat pastel/tonal styling with clear borders
        when (keyType) {
            KeyDomeType.NUMBER -> Triple(
                if (isPressed) Color(0xFFE2E6EF) else Color(0xFFF1F3F9),
                Color(0xFF0F172A),
                Color(0xFF94A3B8) // slate-400
            )
            KeyDomeType.OPERATION -> Triple(
                if (isPressed) Color(0xFFDDE6FD) else Color(0xFFEEF2FF),
                Color(0xFF2563EB),
                Color(0xFF60A5FA) // blue-400
            )
            KeyDomeType.EQUALS -> Triple(
                if (isPressed) Color(0xFF1D4ED8) else Color(0xFF2563EB),
                Color.White,
                Color(0xFF1D4ED8)
            )
            KeyDomeType.ACTION_CLEAR -> Triple(
                if (isPressed) Color(0xFFFCE3E3) else Color(0xFFFEF2F2),
                Color(0xFFDC2626),
                Color(0xFFF87171) // red-400
            )
            KeyDomeType.SHIFT -> if (isActive) {
                Triple(Color(0xFFD97706), Color.White, Color(0xFFB45309))
            } else {
                Triple(
                    if (isPressed) Color(0xFFFEF0C7) else Color(0xFFFFFBEB),
                    Color(0xFFD97706),
                    Color(0xFFF59E0B) // amber-500
                )
            }
            KeyDomeType.ALPHA -> if (isActive) {
                Triple(Color(0xFFE11D48), Color.White, Color(0xFF9F1239))
            } else {
                Triple(
                    if (isPressed) Color(0xFFFFE0E5) else Color(0xFFFFF1F2),
                    Color(0xFFE11D48),
                    Color(0xFFFB7185) // rose-400
                )
            }
            KeyDomeType.FUNCTION -> Triple(
                if (isPressed) Color(0xFFE5E9F0) else Color(0xFFF4F6F9),
                Color(0xFF334155),
                Color(0xFF94A3B8)
            )
            KeyDomeType.HOME, KeyDomeType.DPAD -> Triple(
                if (isPressed) Color(0xFFE0E5ED) else Color(0xFFEEF1F6),
                Color(0xFF1E293B),
                Color(0xFF94A3B8)
            )
        }
    }

    val buttonShape = CircleShape

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Small labels above key (if applicable)
        if (shiftLabel != null || alphaLabel != null) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp)
            ) {
                if (shiftLabel != null) {
                    Text(
                        text = shiftLabel,
                        color = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                if (shiftLabel != null && alphaLabel != null) {
                    Spacer(modifier = Modifier.width(3.dp))
                }
                if (alphaLabel != null) {
                    Text(
                        text = alphaLabel,
                        color = if (isDarkTheme) Color(0xFFFB7185) else Color(0xFFE11D48),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Circular Button with prominent 1.5.dp border to partition each button clearly
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(buttonShape)
                .background(bgColor)
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = buttonShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
                .testTag(testTag)
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RoundKeyDomeButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shiftLabel: String? = null,
    alphaLabel: String? = null,
    keyType: KeyDomeType = KeyDomeType.FUNCTION,
    fontSize: Int = 13,
    size: Dp = 44.dp,
    height: Dp = 44.dp,
    isDarkTheme: Boolean = true,
    isActive: Boolean = false,
    testTag: String = "key_${label.lowercase()}"
) {
    LargeDomeButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        shiftLabel = shiftLabel,
        alphaLabel = alphaLabel,
        keyType = keyType,
        fontSize = fontSize,
        isCircle = true,
        isDarkTheme = isDarkTheme,
        isActive = isActive,
        testTag = testTag
    )
}

@Composable
fun KeyDomeButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shiftLabel: String? = null,
    alphaLabel: String? = null,
    keyType: KeyDomeType = KeyDomeType.FUNCTION,
    fontSize: Int = 13,
    height: Dp = 44.dp,
    isDarkTheme: Boolean = true,
    testTag: String = "key_${label.lowercase()}"
) {
    LargeDomeButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        shiftLabel = shiftLabel,
        alphaLabel = alphaLabel,
        keyType = keyType,
        fontSize = fontSize,
        isCircle = false,
        isDarkTheme = isDarkTheme,
        testTag = testTag
    )
}
