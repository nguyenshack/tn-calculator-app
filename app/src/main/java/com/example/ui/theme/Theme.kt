package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CalculatorColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color(0xFF0F172A),
    secondary = CoralAccent,
    onSecondary = Color.White,
    tertiary = GoldAccent,
    onTertiary = Color(0xFF0F172A),
    background = DarkNavy,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        typography = Typography,
        content = content
    )
}
