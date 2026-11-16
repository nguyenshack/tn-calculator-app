package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.ChatMessage
import com.example.ai.MessageSender
import com.example.ai.TnAiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TnAiScreen(
    viewModel: TnAiViewModel,
    isDarkTheme: Boolean = true,
    onBackToCalculator: () -> Unit = {},
    onSendToCalculator: (String) -> Unit = {},
    onOpenEquation: () -> Unit = {}
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cooldownRemaining by viewModel.cooldownRemaining.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    val listState = rememberLazyListState()

    // 1. Camera Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedImage = bitmap
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Cần cấp quyền Camera để chụp ảnh bài tập", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Photo Gallery Picker
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = decodeUriToBitmap(context, uri)
            if (bmp != null) {
                selectedImage = bmp
            } else {
                Toast.makeText(context, "Không thể tải hình ảnh này", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto scroll down as new tokens or messages arrive
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF101216) else Color(0xFFF4F6F9))
    ) {
        // 1. Compact Sleek Top Bar (Single layer, small forehead)
        Surface(
            color = if (isDarkTheme) Color(0xFF161920) else Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF262A33) else Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back to Calculator button
                    IconButton(
                        onClick = onBackToCalculator,
                        modifier = Modifier.size(34.dp).testTag("tn_ai_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại máy tính",
                            tint = if (isDarkTheme) Color(0xFF93C5FD) else Color(0xFF2563EB),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Compact "TN" Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "TN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "TN AI",
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "3.5 Flash-Lite",
                            color = Color(0xFFA78BFA),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }

                // Clear history button
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.size(34.dp).testTag("tn_ai_clear_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa lịch sử",
                        tint = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Error message banner (if any)
        AnimatedVisibility(visible = errorMessage != null) {
            Surface(
                color = if (isDarkTheme) Color(0xFF3B181E) else Color(0xFFFEF2F2),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // 2. Chat Messages Thread
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    isDarkTheme = isDarkTheme,
                    onCopyText = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("TN AI Solution", cleanMathString(msg.text)))
                        Toast.makeText(context, "Đã sao chép lời giải", Toast.LENGTH_SHORT).show()
                    },
                    onSendFormula = { formula ->
                        onSendToCalculator(formula)
                        Toast.makeText(context, "Đã gửi vào máy tính: $formula", Toast.LENGTH_SHORT).show()
                    },
                    onOpenEquation = onOpenEquation
                )
            }
        }


        // 4. Attached Photo Preview (if captured / selected)
        if (selectedImage != null) {
            Surface(
                color = if (isDarkTheme) Color(0xFF161920) else Color(0xFFEEF2F6),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            bitmap = selectedImage!!.asImageBitmap(),
                            contentDescription = "Ảnh đề bài",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(6.dp))
                        )
                        Column {
                            Text(
                                text = "Ảnh câu hỏi đã sẵn sàng",
                                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bấm gửi để TN AI giải bài tập từ ảnh này",
                                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { selectedImage = null },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Gỡ ảnh",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 5. Rate Limit 5s Cooldown Bar
        AnimatedVisibility(visible = cooldownRemaining > 0f) {
            Surface(
                color = if (isDarkTheme) Color(0xFF1E1B2E) else Color(0xFFF3E8FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Giới hạn câu hỏi: gửi tiếp sau ${String.format(java.util.Locale.US, "%.1f", cooldownRemaining)}s",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "1 câu / 5s",
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { (cooldownRemaining / 5.0f).coerceIn(0f, 1f) },
                        color = Color(0xFF8B5CF6),
                        trackColor = if (isDarkTheme) Color(0xFF2C2840) else Color(0xFFE9D5FF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // 6. Bottom Input Bar with Camera & Gallery Buttons
        Surface(
            color = if (isDarkTheme) Color(0xFF181B22) else Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2B303C) else Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                // Photo from Camera Button
                IconButton(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            takePictureLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.size(38.dp).testTag("tn_ai_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Chụp hình câu hỏi",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Choose Photo from Gallery Button
                IconButton(
                    onClick = {
                        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.size(38.dp).testTag("tn_ai_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Chọn ảnh câu hỏi từ thư viện",
                        tint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (selectedImage != null) "Ghi chú thêm về ảnh..." else "Nhập hoặc chụp ảnh câu hỏi...",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF333A48) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDarkTheme) Color(0xFF13151B) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDarkTheme) Color(0xFF13151B) else Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tn_ai_input_field")
                )

                val canSend = (inputText.trim().isNotEmpty() || selectedImage != null) && !isGenerating && cooldownRemaining <= 0f

                FloatingActionButton(
                    onClick = {
                        if (canSend) {
                            val q = inputText.trim()
                            val img = selectedImage
                            viewModel.sendMessage(q, img)
                            inputText = ""
                            selectedImage = null
                        }
                    },
                    containerColor = if (canSend) Color(0xFF8B5CF6) else if (isDarkTheme) Color(0xFF2D2545) else Color(0xFFD8B4FE),
                    contentColor = Color.White,
                    modifier = Modifier.size(40.dp).testTag("tn_ai_send_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (cooldownRemaining > 0f) {
                        Text(
                            text = "${cooldownRemaining.toInt() + 1}s",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi đề bài",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isDarkTheme: Boolean,
    onCopyText: () -> Unit,
    onSendFormula: (String) -> Unit,
    onOpenEquation: () -> Unit = {}
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isUser) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF8B5CF6),
                modifier = Modifier.size(24.dp).padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "TN",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isUser) 12.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 12.dp
                ),
                color = if (isUser) {
                    if (isDarkTheme) Color(0xFF2563EB) else Color(0xFF3B82F6)
                } else {
                    if (isDarkTheme) Color(0xFF1B1E26) else Color(0xFFFFFFFF)
                },
                border = if (!isUser) {
                    androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2D3340) else Color(0xFFE2E8F0))
                } else null,
                modifier = Modifier.widthIn(max = 350.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // If user attached an image to this message
                    if (message.imageBitmap != null) {
                        Image(
                            bitmap = message.imageBitmap.asImageBitmap(),
                            contentDescription = "Ảnh bài tập đính kèm",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (message.text.isEmpty() && message.isStreaming) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFA78BFA),
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "TN AI đang phân tích và giải bài...",
                                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    } else {
                        if (isUser) {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            )
                        } else {
                            // Rich Natural Math Formatter with Bold Equations and Clean Symbols
                            FormattedAiMessage(
                                rawText = message.text,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }

                    if (message.isStreaming && message.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6))
                            )
                            Text(
                                text = "đang gõ...",
                                color = Color(0xFFA78BFA),
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons for AI Responses
            if (!isUser && message.text.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                ) {
                    IconButton(
                        onClick = onCopyText,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Sao chép lời giải",
                            tint = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    val formulas = if (message.extractedFormulas.isNotEmpty()) message.extractedFormulas else listOfNotNull(message.extractedFormula)
                    formulas.forEachIndexed { index, formula ->
                        val cleanFormula = com.example.math.CalculatorFormulaSanitizer.sanitize(formula)
                        val label = if (formulas.size > 1) "Bấm nghiệm x${index + 1}: $cleanFormula" else "Bấm máy: $cleanFormula"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkTheme) Color(0xFF1E2A40) else Color(0xFFE0E7FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onSendFormula(cleanFormula) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    val hasEquation = message.text.contains("phương trình", ignoreCase = true) ||
                                      message.text.contains("bậc 2", ignoreCase = true) ||
                                      message.text.contains("hệ phương trình", ignoreCase = true)
                    if (hasEquation && !isUser) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkTheme) Color(0xFF142426) else Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onOpenEquation() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF059669),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Vẽ đồ thị phương trình",
                                    color = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF059669),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich Formatter for AI Responses:
 * - Strips all raw LaTeX codes, $$ and $ delimiters, ### tags
 * - Highlights mathematical equations in BOLD cards/chips so formulas are vivid and intuitive
 * - Formats step-by-step headers with neat badges
 * - Converts all variables/coefficients into clean natural textbook math
 */
@Composable
fun FormattedAiMessage(
    rawText: String,
    isDarkTheme: Boolean
) {
    val cleanedText = remember(rawText) { cleanMathString(rawText) }
    val lines = remember(cleanedText) { cleanedText.split("\n") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                return@forEach
            }

            val isStepHeader = trimmed.startsWith("Bước", ignoreCase = true) ||
                    trimmed.startsWith("Bài giải", ignoreCase = true) ||
                    trimmed.startsWith("Kết luận", ignoreCase = true) ||
                    trimmed.startsWith("Đáp số", ignoreCase = true)

            val isBullet = trimmed.startsWith("•") || trimmed.startsWith("*") || trimmed.startsWith("-")

            // Check if this line is primarily a mathematical equation or formula
            val isFormulaLine = isEquationLine(trimmed)

            when {
                isStepHeader -> {
                    // Render clean step header without raw hashtags
                    val titleText = trimmed.replace(Regex("^[*_#\\s]+|[*_#\\s]+$"), "")
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDarkTheme) Color(0xFF262C3A) else Color(0xFFE2E8F0),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = titleText,
                            color = if (isDarkTheme) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                isFormulaLine -> {
                    // BOLD mathematical expression on highlighted card for instant readability
                    val eqText = trimmed.replace(Regex("^[*_\\s]+|[*_\\s]+$"), "")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkTheme) Color(0xFF151821) else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDarkTheme) Color(0xFF333B4D) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = eqText,
                            color = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFF1E40AF),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                isBullet -> {
                    val bulletContent = trimmed.substring(1).trim().replace(Regex("^[*_\\s]+|[*_\\s]+$"), "")
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 1.dp, bottom = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = if (isDarkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        RenderInlineBoldText(
                            text = bulletContent,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                else -> {
                    RenderInlineBoldText(
                        text = trimmed,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

/**
 * Checks if a trimmed line is an equation/formula like "Δ = b² - 4ac", "2x² - 5x + 2 = 0", "x1 = 2"
 */
private fun isEquationLine(line: String): Boolean {
    val clean = line.replace(Regex("^[*_#\\s]+|[*_#\\s]+$"), "")
    if (clean.length > 80) return false // Long explanations are not standalone equations
    val hasEquals = clean.contains("=") || clean.contains("≈") || clean.contains("≠") || clean.contains("≤") || clean.contains("≥")
    if (!hasEquals) return false

    // If it starts with common explanatory phrases, it's not a standalone formula
    val explanatoryPrefixes = listOf("Vì", "Do", "Vậy", "Suy ra", "Áp dụng", "Thay", "Với", "Từ", "Ta có")
    val words = clean.split(" ")
    if (words.size > 8 && explanatoryPrefixes.any { clean.startsWith(it, ignoreCase = true) }) {
        return false
    }

    // Typical equation indicators
    val mathSymbols = listOf("Δ", "+", "−", "-", "×", "·", "/", "√", "^", "²", "³", "x", "y")
    val countMath = mathSymbols.count { clean.contains(it) }
    return countMath >= 1 || words.size <= 6
}

@Composable
private fun RenderInlineBoldText(
    text: String,
    isDarkTheme: Boolean
) {
    val annotated = remember(text, isDarkTheme) {
        buildAnnotatedString {
            var i = 0
            val len = text.length
            while (i < len) {
                val boldStart = text.indexOf("**", i)
                if (boldStart != -1) {
                    // Append normal text before **
                    if (boldStart > i) {
                        append(text.substring(i, boldStart))
                    }
                    val boldEnd = text.indexOf("**", boldStart + 2)
                    if (boldEnd != -1) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            )
                        ) {
                            append(text.substring(boldStart + 2, boldEnd))
                        }
                        i = boldEnd + 2
                    } else {
                        append(text.substring(boldStart + 2))
                        break
                    }
                } else {
                    append(text.substring(i))
                    break
                }
            }
        }
    }

    Text(
        text = annotated,
        color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF1E293B),
        fontSize = 13.5.sp,
        lineHeight = 19.sp
    )
}

/**
 * Strips raw LaTeX codes, delimiters, and transforms formulas to clean natural textbook representation.
 */
fun cleanMathString(raw: String): String {
    if (raw.isEmpty()) return raw
    var s = raw

    // 1. Strip raw markdown headers (###, ####, ##, #)
    s = s.replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")

    // 2. Strip LaTeX equation delimiters $$ and $
    s = s.replace("$$", "")
    s = s.replace("$", "")

    // 3. LaTeX Greek letters and math operators
    s = s.replace(Regex("\\\\(Delta|delta)"), "Δ")
    s = s.replace(Regex("\\\\(alpha|Alpha)"), "α")
    s = s.replace(Regex("\\\\(beta|Beta)"), "β")
    s = s.replace(Regex("\\\\(gamma|Gamma)"), "γ")
    s = s.replace(Regex("\\\\(theta|Theta)"), "θ")
    s = s.replace(Regex("\\\\(pi|Pi)"), "π")
    s = s.replace(Regex("\\\\(lambda|Lambda)"), "λ")
    s = s.replace(Regex("\\\\(sigma|Sigma)"), "σ")
    s = s.replace(Regex("\\\\(omega|Omega)"), "ω")

    // 4. Operators and dots
    s = s.replace("\\cdot", " × ")
    s = s.replace("\\times", " × ")
    s = s.replace("\\div", " ÷ ")
    s = s.replace("\\pm", "±")
    s = s.replace("\\mp", "∓")
    s = s.replace("\\leq", "≤")
    s = s.replace("\\geq", "≥")
    s = s.replace("\\neq", "≠")
    s = s.replace("\\approx", "≈")
    s = s.replace("\\sim", "≈")
    s = s.replace("\\infty", "∞")

    // 5. Fractions: \frac{A}{B} -> (A)/(B) or A/B
    s = s.replace(Regex("\\\\frac\\{([^{}]+)\\}\\{([^{}]+)\\}")) { match ->
        val num = match.groupValues[1].trim()
        val den = match.groupValues[2].trim()
        if (num.length <= 3 && den.length <= 3) "$num/$den" else "($num)/($den)"
    }

    // 6. Radicals: \sqrt[n]{A} -> ⁿ√(A), \sqrt{A} -> √(A)
    s = s.replace(Regex("\\\\sqrt\\[([^{}]+)\\]\\{([^{}]+)\\}")) { match ->
        val n = match.groupValues[1].trim()
        val rad = match.groupValues[2].trim()
        "${n}√($rad)"
    }
    s = s.replace(Regex("\\\\sqrt\\{([^{}]+)\\}")) { match ->
        val rad = match.groupValues[1].trim()
        "√($rad)"
    }
    s = s.replace("\\sqrt", "√")

    // 7. Strip \left and \right delimiters
    s = s.replace("\\left(", "(")
    s = s.replace("\\right)", ")")
    s = s.replace("\\left[", "[")
    s = s.replace("\\right]", "]")
    s = s.replace("\\left\\{", "{")
    s = s.replace("\\right\\}", "}")
    s = s.replace("\\left|", "|")
    s = s.replace("\\right|", "|")
    s = s.replace("\\left", "")
    s = s.replace("\\right", "")

    // 8. Strip lingering curly braces: {2a} -> 2a
    s = s.replace(Regex("\\{([0-9a-zA-Z_+\\-]+)\\}"), "$1")

    // 9. Natural exponents and roots
    s = s.replace("^-1", "⁻¹")
        .replace("^-2", "⁻²")
        .replace("^2", "²")
        .replace("^3", "³")
        .replace("^4", "⁴")
        .replace("^5", "⁵")
        .replace("^6", "⁶")
        .replace("^7", "⁷")
        .replace("^8", "⁸")
        .replace("^9", "⁹")
        .replace("^0", "⁰")
        .replace("^n", "ⁿ")
        .replace("^x", "ˣ")
        .replace("^y", "ʸ")

    // 10. Trigonometric inverses and comparisons
    s = s.replace("sqrt(", "√(")
        .replace("arcsin(", "sin⁻¹(")
        .replace("arccos(", "cos⁻¹(")
        .replace("arctan(", "tan⁻¹(")
        .replace("asin(", "sin⁻¹(")
        .replace("acos(", "cos⁻¹(")
        .replace("atan(", "tan⁻¹(")
        .replace("<=", "≤")
        .replace(">=", "≥")
        .replace("!=", "≠")
        .replace("+/-", "±")
        .replace("+-", "±")
        .replace("->", "→")
        .replace("=>", "⇒")
        .replace("<=>", "⇔")
        .replace("~=", "≈")

    // 11. Normalize multiply spacing
    s = s.replace(" * ", " × ")
        .replace("*10^", "×10^")
        .replace("* 10^", "× 10^")

    return s
}

private fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.setTargetSampleSize(2)
            }
        } else {
            @Suppress("DEPRECATION")
            val original = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            if (original.width > 1200 || original.height > 1200) {
                val scale = 1200f / maxOf(original.width, original.height)
                Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
            } else {
                original
            }
        }
    } catch (e: Exception) {
        null
    }
}
