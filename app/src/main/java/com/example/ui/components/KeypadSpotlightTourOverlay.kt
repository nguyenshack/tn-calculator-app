package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SpotlightTargetKey {
    TOP_AI_BUTTON,
    LCD_SCREEN,
    DPAD_KEYS,
    SHIFT_ALPHA,
    CAT_TOOLS,
    SCIENTIFIC_KEYS,
    DEL_AC_KEYS,
    EQUALS_SD_KEYS
}

data class KeypadSpotlightStep(
    val stepIndex: Int,
    val targetKey: SpotlightTargetKey,
    val title: String,
    val keyGroupLabel: String,
    val icon: ImageVector,
    val accentColor: Color,
    val description: String
)

@Composable
fun KeypadSpotlightTourOverlay(
    targetBounds: Map<SpotlightTargetKey, Rect>,
    onDismissTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // Pulsing animation for spotlight border
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val arrowBounce by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_bounce"
    )

    val steps = remember {
        listOf(
            // 1. Màn hình LCD
            KeypadSpotlightStep(
                stepIndex = 1,
                targetKey = SpotlightTargetKey.LCD_SCREEN,
                title = "Màn hình LCD Tự nhiên",
                keyGroupLabel = "Natural Display",
                icon = Icons.Default.Tv,
                accentColor = Color(0xFF38BDF8),
                description = "Hiển thị phân số 2 tầng, số mũ, căn bậc n và số phức chuẩn sách giáo khoa. Con trỏ nhấp nháy chỉ rõ vị trí đang gõ."
            ),
            // 2. Cụm điều hướng 4 hướng & Con trỏ
            KeypadSpotlightStep(
                stepIndex = 2,
                targetKey = SpotlightTargetKey.DPAD_KEYS,
                title = "Cụm điều hướng & Con trỏ",
                keyGroupLabel = "Phím [▲] [▼] [◄] [►] [OK]",
                icon = Icons.Default.Navigation,
                accentColor = Color(0xFF3B82F6),
                description = "4 phím mũi tên di chuyển con trỏ vào sâu tử số, mẫu số, số mũ để sửa phép tính nhanh mà không cần xóa đi gõ lại."
            ),
            // 3. Phím SHIFT & ALPHA
            KeypadSpotlightStep(
                stepIndex = 3,
                targetKey = SpotlightTargetKey.SHIFT_ALPHA,
                title = "Phím SHIFT & ALPHA",
                keyGroupLabel = "Phím [SHIFT] (Vàng) & [ALPHA] (Đỏ)",
                icon = Icons.Default.Tune,
                accentColor = Color(0xFFEAB308),
                description = "[SHIFT] mở các hàm phụ màu vàng (căn 3, sin⁻¹...). [ALPHA] gõ biến số A, B, C, D, E và x, y, z màu đỏ."
            ),
            // 4. Hub công cụ CAT & TOOLS
            KeypadSpotlightStep(
                stepIndex = 4,
                targetKey = SpotlightTargetKey.CAT_TOOLS,
                title = "Hub công cụ CAT & TOOLS",
                keyGroupLabel = "Phím [CAT] & [TOOLS]",
                icon = Icons.Default.Widgets,
                accentColor = Color(0xFFA855F7),
                description = "[CAT] mở đổi đơn vị, hệ cơ số, bảng tuần hoàn. [TOOLS] mở giải phương trình, CALC, tài chính, sức khỏe."
            ),
            // 5. Phím hàm Khoa học & Lượng giác
            KeypadSpotlightStep(
                stepIndex = 5,
                targetKey = SpotlightTargetKey.SCIENTIFIC_KEYS,
                title = "Hàm Khoa học & Lượng giác",
                keyGroupLabel = "[a/b] [√] [x²] [xⁿ] [log] [sin] [cos]",
                icon = Icons.Default.Calculate,
                accentColor = Color(0xFF06B6D4),
                description = "Hai hàng phím chứa đầy đủ phân số a/b, căn thức, số mũ, logarit, lượng giác, dấu ngoặc () và tích phân."
            ),
            // 6. Phím Xóa lùi [DEL] & Xóa toàn bộ [AC]
            KeypadSpotlightStep(
                stepIndex = 6,
                targetKey = SpotlightTargetKey.DEL_AC_KEYS,
                title = "Phím Xóa lùi [⌫] & Xóa hết [AC]",
                keyGroupLabel = "Phím [⌫] (Đỏ sẫm) & [AC] (Cam đỏ)",
                icon = Icons.Default.Backspace,
                accentColor = Color(0xFFEF4444),
                description = "[⌫] xóa lùi từng ký tự ngay trước con trỏ. [AC] xóa sạch toàn bộ biểu thức để bắt đầu bài toán mới."
            ),
            // 7. Phím Thực hiện [=] & Chuyển đổi [S⇔D]
            KeypadSpotlightStep(
                stepIndex = 7,
                targetKey = SpotlightTargetKey.EQUALS_SD_KEYS,
                title = "Phím Bằng [=] & Chuyển đổi [S⇔D]",
                keyGroupLabel = "Phím [=] (Xanh dương) & [S⇔D]",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF3B82F6),
                description = "[=] tính ra kết quả đáp số ngay. [S⇔D] chuyển đổi qua lại giữa số thập phân và phân số tối giản chính xác."
            ),
            // 8. Trợ lý TN AI trên thanh tiêu đề
            KeypadSpotlightStep(
                stepIndex = 8,
                targetKey = SpotlightTargetKey.TOP_AI_BUTTON,
                title = "Trợ lý AI giải toán TN AI",
                keyGroupLabel = "Nút [TN AI] trên thanh công cụ",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFC084FC),
                description = "Chạm nút [TN AI] màu tím để chụp ảnh đề bài hoặc hỏi bài tập, nhận lời giải chi tiết và công thức số bấm máy."
            )
        )
    }

    val currentStep = steps[currentStepIndex]
    val measuredRect = targetBounds[currentStep.targetKey]

    fun advanceStep() {
        if (currentStepIndex < steps.size - 1) {
            currentStepIndex++
        } else {
            onDismissTour()
        }
    }

    fun previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Tapping directly anywhere on the screen advances to the next step!
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                advanceStep()
            }
    ) {
        // Spotlight Canvas: draws dark scrim with cut-out hole and pointing arrow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val pad = 6.dp.toPx()
            val targetRect = if (measuredRect != null && measuredRect.width > 10f && measuredRect.height > 10f) {
                Rect(
                    left = (measuredRect.left - pad).coerceAtLeast(4f),
                    top = (measuredRect.top - pad).coerceAtLeast(4f),
                    right = (measuredRect.right + pad).coerceAtMost(w - 4f),
                    bottom = (measuredRect.bottom + pad).coerceAtMost(h - 4f)
                )
            } else {
                Rect(w * 0.1f, h * 0.4f, w * 0.9f, h * 0.55f)
            }

            val cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            val roundRect = RoundRect(targetRect, cornerRadius)

            // Draw dark backdrop with smooth cutout around the target
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, w, h))
                addRoundRect(roundRect)
            }
            drawPath(path = path, color = Color(0xFF04060A).copy(alpha = 0.88f))

            // Draw pulsating neon border around the spotlight cutout
            drawRoundRect(
                color = currentStep.accentColor.copy(alpha = pulseAlpha.coerceIn(0f, 1f)),
                topLeft = Offset(targetRect.left, targetRect.top),
                size = Size(targetRect.width, targetRect.height),
                cornerRadius = cornerRadius,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Outer ambient glow ring
            drawRoundRect(
                color = currentStep.accentColor.copy(alpha = (pulseAlpha * 0.35f).coerceIn(0f, 1f)),
                topLeft = Offset(targetRect.left - 4.dp.toPx(), targetRect.top - 4.dp.toPx()),
                size = Size(targetRect.width + 8.dp.toPx(), targetRect.height + 8.dp.toPx()),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Pointing Arrow toward target center
            val isTargetInBottomHalf = targetRect.top > 280.dp.toPx()
            val arrowX = targetRect.center.x.coerceIn(24.dp.toPx(), w - 24.dp.toPx())
            val arrowPath = Path()

            if (isTargetInBottomHalf) {
                // Target is in bottom half -> Tooltip is above target -> Arrow points DOWNWARD from bubble
                val arrowTipY = targetRect.top - 4.dp.toPx() + arrowBounce.dp.toPx()
                val arrowBaseY = arrowTipY - 10.dp.toPx()
                arrowPath.moveTo(arrowX, arrowTipY)
                arrowPath.lineTo(arrowX - 8.dp.toPx(), arrowBaseY)
                arrowPath.lineTo(arrowX + 8.dp.toPx(), arrowBaseY)
                arrowPath.close()
            } else {
                // Target is in top half -> Tooltip is below target -> Arrow points UPWARD from bubble
                val arrowTipY = targetRect.bottom + 4.dp.toPx() - arrowBounce.dp.toPx()
                val arrowBaseY = arrowTipY + 10.dp.toPx()
                arrowPath.moveTo(arrowX, arrowTipY)
                arrowPath.lineTo(arrowX - 8.dp.toPx(), arrowBaseY)
                arrowPath.lineTo(arrowX + 8.dp.toPx(), arrowBaseY)
                arrowPath.close()
            }

            drawPath(path = arrowPath, color = currentStep.accentColor)
        }

        // Top-right subtle "Bỏ qua" (Skip) pill button
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 10.dp, end = 12.dp)
                .clickable { onDismissTour() }
                .testTag("skip_keypad_tour_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Bỏ qua",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Bỏ qua",
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        // Compact Floating Tooltip Bubble directly adjacent to the spotlighted area
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight
            val densityCurrent = LocalDensity.current

            // Calculate vertical positioning of the bubble
            val targetTopDp = with(densityCurrent) { (measuredRect?.top ?: 400f).toDp() }
            val isTargetInBottomHalf = targetTopDp > 280.dp

            val bubbleModifier = if (isTargetInBottomHalf) {
                // Bubble is positioned at the top of the target or top screen area
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 44.dp, start = 14.dp, end = 14.dp)
            } else {
                // Target is near top -> Bubble is placed at the bottom
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 14.dp, end = 14.dp)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF111726).copy(alpha = 0.98f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, currentStep.accentColor.copy(alpha = 0.75f)),
                shadowElevation = 14.dp,
                modifier = bubbleModifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    // Clicking on bubble advances to next step too
                    .clickable { advanceStep() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    // Header Row: Icon + Title + Step Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = currentStep.accentColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = currentStep.icon,
                                        contentDescription = currentStep.title,
                                        tint = currentStep.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = currentStep.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentStep.keyGroupLabel,
                                    color = currentStep.accentColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "${currentStep.stepIndex}/8",
                                color = currentStep.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Concise 1-2 sentence description
                    Text(
                        text = currentStep.description,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 16.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer Row: Previous Button, "Chạm phím để tiếp tục", Next Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (currentStepIndex > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                                modifier = Modifier
                                    .clickable { previousStep() }
                                    .padding(end = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Quay lại",
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Trước", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // Tap anywhere hint in center
                        Text(
                            text = "👉 Chạm vào phím để tiếp tục",
                            color = currentStep.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentStep.accentColor,
                            modifier = Modifier.clickable { advanceStep() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (currentStepIndex < steps.size - 1) "Tiếp" else "Xong",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (currentStepIndex < steps.size - 1)
                                        Icons.AutoMirrored.Filled.ArrowForward
                                    else
                                        Icons.Default.Check,
                                    contentDescription = "Tiếp theo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
