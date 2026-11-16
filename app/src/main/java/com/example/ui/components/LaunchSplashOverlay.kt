package com.example.ui.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlin.math.*
import kotlin.random.Random

private data class MathParticle(
    val symbol: String,
    val initialSpreadAngle: Float,
    val speed: Float,
    val color: Color,
    val sizeSp: Float,
    val spawnTimeFraction: Float,
    val rotationSpeed: Float
)

@Composable
fun LaunchSplashOverlay(
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    // 22 diverse mathematical symbols
    val mathSymbols = remember {
        listOf("+", "−", "×", "÷", "√", "π", "Δ", "∫", "x²", "∑", "∞", "%", "sin", "dx", "e", "λ", "θ", "f(x)", "d/dx", "log", "α", "β")
    }

    val particleColors = remember {
        listOf(
            Color(0xFFC084FC), // Light purple
            Color(0xFFA855F7), // Vivid purple
            Color(0xFF38BDF8), // Neon cyan
            Color(0xFFFBBF24), // Gold amber
            Color(0xFFF472B6), // Neon pink
            Color(0xFF34D399)  // Emerald green
        )
    }

    val particles = remember {
        val rand = Random(42)
        (0 until 22).map { i ->
            MathParticle(
                symbol = mathSymbols[i % mathSymbols.size],
                initialSpreadAngle = rand.nextFloat() * 140f + 20f, // 20 to 160 deg
                speed = rand.nextFloat() * 220f + 140f,
                color = particleColors[rand.nextInt(particleColors.size)],
                sizeSp = rand.nextFloat() * 8f + 13f, // 13sp to 21sp
                spawnTimeFraction = 0.28f + (i * 0.024f),
                rotationSpeed = (rand.nextFloat() - 0.5f) * 180f
            )
        }
    }

    // Pre-measure all particle text layouts once to prevent per-frame allocation and crashes
    val precomputedLayouts = remember(particles, textMeasurer) {
        particles.associate { p ->
            p.symbol to textMeasurer.measure(
                text = p.symbol,
                style = TextStyle(
                    fontSize = p.sizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1800,
                    easing = LinearEasing
                )
            )
            onAnimationFinished()
        } catch (_: CancellationException) {
            // Normal coroutine cancellation
        } catch (e: Exception) {
            Log.e("LaunchSplashOverlay", "Animation failed", e)
            onAnimationFinished()
        }
    }

    val progress = animProgress.value

    val overlayAlpha = when {
        progress < 0.72f -> 1f
        else -> (1f - ((progress - 0.72f) / 0.28f)).coerceIn(0f, 1f)
    }.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060911),
                        Color(0xFF0B101D),
                        Color(0xFF080C16)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAnimationFinished()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val centerX = w / 2f
            val starStartY = h * 0.44f
            val starEndY = -h * 0.15f

            val starProgress = when {
                progress < 0.28f -> 0f
                progress < 0.72f -> {
                    val p = ((progress - 0.28f) / 0.44f).coerceIn(0f, 1f)
                    p * p
                }
                else -> 1f
            }

            val starCurrentY = starStartY + (starEndY - starStartY) * starProgress

            val starScale = when {
                progress < 0.28f -> {
                    val pulse = sin(progress * 12f) * 0.08f
                    val introScale = (progress / 0.12f).coerceIn(0.01f, 1f)
                    (introScale * (1f + pulse)).coerceIn(0.01f, 1.3f)
                }
                progress < 0.72f -> {
                    (1f - starProgress * 0.25f).coerceIn(0.01f, 1f)
                }
                else -> 0.7f
            }

            // Draw glowing energy trails behind star when flying
            if (progress in 0.25f..0.75f) {
                val trailAlpha = (((progress - 0.25f) / 0.2f).coerceIn(0f, 1f) * (1f - starProgress * 0.8f)).coerceIn(0f, 1f)
                val trailHeight = (h * 0.35f * starProgress).coerceAtLeast(10f)
                if (trailAlpha > 0.01f && starScale > 0.02f) {
                    val trailBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFA855F7).copy(alpha = (0.75f * trailAlpha).coerceIn(0f, 1f)),
                            Color(0xFF8B5CF6).copy(alpha = (0.4f * trailAlpha).coerceIn(0f, 1f)),
                            Color(0xFF38BDF8).copy(alpha = (0.15f * trailAlpha).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        startY = starCurrentY,
                        endY = starCurrentY + trailHeight
                    )
                    drawRect(
                        brush = trailBrush,
                        topLeft = Offset(centerX - 18f * starScale, starCurrentY),
                        size = Size((36f * starScale).coerceAtLeast(1f), trailHeight)
                    )
                }
            }

            // Render Flying Math Symbols Particles
            if (progress >= 0.28f) {
                for (p in particles) {
                    if (progress >= p.spawnTimeFraction) {
                        val divisor = (1f - p.spawnTimeFraction).coerceAtLeast(0.01f)
                        val pTime = ((progress - p.spawnTimeFraction) / divisor).coerceIn(0f, 1f)

                        val spawnStarProgress = (((p.spawnTimeFraction - 0.28f) / 0.44f).coerceIn(0f, 1f))
                        val spawnY = starStartY + (starEndY - starStartY) * (spawnStarProgress * spawnStarProgress)

                        val rad = Math.toRadians(p.initialSpreadAngle.toDouble())
                        val currentDist = p.speed * pTime * 2.2f
                        val px = centerX + (cos(rad) * currentDist).toFloat()
                        val py = spawnY + (sin(rad) * currentDist).toFloat() + (pTime * pTime * 80f)

                        val pAlpha = (when {
                            pTime < 0.15f -> (pTime / 0.15f)
                            pTime > 0.65f -> ((1f - pTime) / 0.35f)
                            else -> 1f
                        }.coerceIn(0f, 1f) * overlayAlpha).coerceIn(0f, 1f)

                        if (pAlpha > 0.02f) {
                            val textLayout: TextLayoutResult? = precomputedLayouts[p.symbol]
                            if (textLayout != null) {
                                val rotation = p.rotationSpeed * pTime
                                rotate(degrees = rotation, pivot = Offset(px, py)) {
                                    drawText(
                                        textLayoutResult = textLayout,
                                        color = p.color.copy(alpha = pAlpha),
                                        topLeft = Offset(
                                            px - textLayout.size.width / 2f,
                                            py - textLayout.size.height / 2f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Draw the iconic 4-Pointed AI Star (Glowing Purple + Cyan core)
            if (starCurrentY > -150f && starScale > 0.01f) {
                val baseStarSize = (74f * starScale).coerceAtLeast(2f)

                // 1. Outer ambient glow ring (radius strictly > 0 to satisfy Android RadialGradient constraint)
                val glowAlpha = ((0.35f + sin(progress * 15f) * 0.12f).coerceIn(0.1f, 0.6f) * overlayAlpha).coerceIn(0f, 1f)
                val glowRadius = (baseStarSize * 1.6f).coerceAtLeast(2f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA855F7).copy(alpha = glowAlpha),
                            Color(0xFF8B5CF6).copy(alpha = (glowAlpha * 0.5f).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        center = Offset(centerX, starCurrentY),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(centerX, starCurrentY)
                )

                // 2. Star Geometry (Curved 4-pointed sparkle)
                val starPath = createAiStarPath(centerX, starCurrentY, baseStarSize)

                // Gradient Star Fill
                val starBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE9D5FF),
                        Color(0xFFA855F7),
                        Color(0xFF7C3AED),
                        Color(0xFF38BDF8)
                    ),
                    start = Offset(centerX - baseStarSize / 2f, starCurrentY - baseStarSize / 2f),
                    end = Offset(centerX + baseStarSize / 2f, starCurrentY + baseStarSize / 2f)
                )

                drawPath(path = starPath, brush = starBrush, style = Fill)

                // Crisp Cyan Rim (radius strictly >= 2f)
                drawPath(
                    path = starPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF38BDF8)),
                        center = Offset(centerX, starCurrentY),
                        radius = baseStarSize.coerceAtLeast(2f)
                    ),
                    style = Stroke(width = 1.8f)
                )

                // 3. Central light burst diamond
                drawCircle(
                    color = Color.White,
                    radius = (3.5f * starScale).coerceAtLeast(0.5f),
                    center = Offset(centerX, starCurrentY)
                )
            }
        }

        // Subtitle Text & Branding (fades out as star lifts off)
        val brandAlpha = (1f - (progress / 0.35f)).coerceIn(0f, 1f)
        if (brandAlpha > 0.05f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(top = 130.dp)
                    .alpha(brandAlpha)
            ) {
                Text(
                    text = "TN AI",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "MÁY TÍNH KHOA HỌC THÔNG MINH TN-001",
                    color = Color(0xFFA855F7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Powered by Gemini 3.5 Flash Lite • Trần Khôi Nguyên",
                    color = Color(0xFF64748B),
                    fontSize = 9.5.sp
                )
            }
        }

        // Top-right subtle "Bỏ qua" (Skip) pill button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 16.dp)
                .clickable { onAnimationFinished() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Bỏ qua",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Bỏ qua",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun createAiStarPath(cx: Float, cy: Float, size: Float): Path {
    val path = Path()
    val r = size / 2f
    val pinch = r * 0.22f

    path.moveTo(cx, cy - r)
    path.quadraticTo(cx + pinch, cy - pinch, cx + r, cy)
    path.quadraticTo(cx + pinch, cy + pinch, cx, cy + r)
    path.quadraticTo(cx - pinch, cy + pinch, cx - r, cy)
    path.quadraticTo(cx - pinch, cy - pinch, cx, cy - r)
    path.close()

    return path
}
