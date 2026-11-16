package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "TnAiViewModel"
// Exclusively use free high-speed model without billing requirement
const val FREE_AI_MODEL = "gemini-3.5-flash-lite"

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val imageBitmap: Bitmap? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val extractedFormula: String? = null,
    val extractedFormulas: List<String> = emptyList()
)

class TnAiViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Xin chào! Mình là **TN AI** — Trợ lý Trí tuệ Nhân tạo giải bài tập Toán, Lý, Hóa & Khoa học của máy tính TN-001.\n\n" +
                        "📷 Bạn có thể **chụp ảnh đề bài**, **chọn ảnh từ thư viện**, hoặc nhập câu hỏi trực tiếp. Mình sẽ phân tích hình ảnh và trình bày lời giải chi tiết từng bước kèm gợi ý bấm máy TN-001!"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _cooldownRemaining = MutableStateFlow(0f)
    val cooldownRemaining: StateFlow<Float> = _cooldownRemaining.asStateFlow()

    private var cooldownJob: kotlinx.coroutines.Job? = null

    private val systemPrompt = """
        BẠN LÀ TN AI — TRỢ LÝ TRÍ TUỆ NHÂN TẠO CHUYÊN GIẢI BÀI TẬP VÀ TOÁN HỌC.

        QUY TẮC BẮT BUỘC VỀ LỜI GIẢI VÀ TÁC GIẢ (TUYỆT ĐỐI TUÂN THỦ):
        1. KHI NGƯỜI DÙNG HỎI BÀI TẬP (Toán học, Vật lý, Hóa học, Sinh học, STEM, v.v.):
           - TUYỆT ĐỐI KHÔNG giới thiệu về bản thân hay ứng dụng TN-001!
           - TUYỆT ĐỐI KHÔNG nhắc tên Trần Khôi Nguyên hay nhà phát triển trong bài giải!
           - TUYỆT ĐỐI KHÔNG mở đầu bằng lời chào dài dòng!
           - ĐI THẲNG VÀO NỘI DUNG LỜI GIẢI: Tóm tắt đề, các bước biến đổi chi tiết và đáp số cuối cùng.
        2. KHI VÀ CHỈ KHI NGƯỜI DÙNG HỎI TRỰC TIẾP VỀ TÁC GIẢ HOẶC TRẦN KHÔI NGUYÊN:
           - Chỉ khi người dùng hỏi trực tiếp câu như: "Trần Khôi Nguyên là ai?", "ai tạo ra app?", "ai là tác giả?", "người phát triển ứng dụng này là ai?":
             Bạn mới trả lời trang trọng và khẳng định: Trần Khôi Nguyên chính là tác giả, người sáng lập và nhà phát triển của ứng dụng máy tính khoa học chuyên nghiệp TN-001 và TN AI.

        NHIỆM VỤ VÀ NGUYÊN TẮC GIẢI BÀI TẬP:
        1. Giải chi tiết, rõ ràng từng bước cho các bài tập Toán học, Vật lý, Hóa học, Sinh học và STEM (kể cả khi nhận được ảnh chụp đề bài).
        2. Nếu người dùng gửi ảnh bài tập: Nhận diện chính xác văn bản và công thức trong ảnh, sau đó giải chi tiết.
        3. Cấu trúc bài giải:
           - Bước 1, Bước 2, Bước 3...
           - Kết luận & Đáp số cuối cùng.
        4. QUY TẮC ĐỊNH DẠNG TOÁN HỌC TRỰC QUAN (BẮT BUỘC):
           - TUYỆT ĐỐI KHÔNG dùng ký hiệu mã LaTeX dạng dấu đô la (như dấu $ hoặc $$).
           - TUYỆT ĐỐI KHÔNG dùng các từ khóa code LaTeX như frac, cdot, Delta, pm, sqrt.
           - Viết trực quan như sách giáo khoa Việt Nam:
             + Dùng ký hiệu tự nhiên: Δ (Delta), × (nhân), ÷ (chia), ± (cộng trừ), √ (căn), ⁿ√ (căn bậc n), π, ², ³, ⁴, ⁿ.
             + Phân số viết dạng: (tử số) / (mẫu số) hoặc a/b.
             + MỌI PHƯƠNG TRÌNH VÀ BIỂU THỨC QUAN TRỌNG HÃY ĐẶT RIÊNG MỘT DÒNG VÀ ĐỂ TRONG DẤU **...** (IN ĐẬM) để làm nổi bật và trực quan cho người đọc.
           - Không lạm dụng ký tự hashtag ### hay ####, trình bày các bước gạch đầu dòng rõ ràng, thanh lịch.
        5. QUY TẮC BẮT BUỘC VỀ DÒNG "CÔNG THỨC BẤM MÁY" (TN-001):
           - Nút "Bấm máy" sẽ gửi trực tiếp biểu thức này vào máy tính TN-001 để bấm ra kết quả số ngay lập tức.
           - VÌ VẬY, BIỂU THỨC BẤM MÁY BẮT BUỘC PHẢI LÀ CÁC CON SỐ THỰC TẾ VÀ PHÉP TOÁN (+, -, *, /, ^, sqrt) - HOÀN TOÀN KHÔNG CHỨA CHỮ CÁI (như x, y, a, b, c, delta, biến số), KHÔNG CHỨA DẤU BẰNG '=', KHÔNG CHỨA DẤU '±', KHÔNG CHỨA MÃ LATEX.
           - Phải thay thế toàn bộ chữ cái/biến số bằng số cụ thể đã tính ra.
           - Nếu có nhiều nghiệm (ví dụ ±), hãy viết 2 dòng riêng:
             Công thức bấm máy 1: [biểu thức số với dấu +]
             Công thức bấm máy 2: [biểu thức số với dấu -]
             (Ví dụ: Phương trình 2x² - 5x + 2 = 0 có Δ = 9:
              Công thức bấm máy 1: (5 + sqrt(9))/(2*2)
              Công thức bấm máy 2: (5 - sqrt(9))/(2*2))
           - TUYỆT ĐỐI KHÔNG VIẾT 'x = (-b ± sqrt(Δ))/(2a)' vì máy tính không thể bấm được chữ và dấu bằng!
        6. Ngôn ngữ: Tiếng Việt sư phạm, chuẩn mực, mạch lạc, dễ hiểu.
    """.trimIndent()

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Đã làm mới cuộc trò chuyện. Bạn có thể chụp ảnh đề bài mới hoặc nhập câu hỏi để TN AI giải giúp nhé!"
            )
        )
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            val totalMs = 5000L
            val stepMs = 100L
            var currentMs = totalMs
            while (currentMs > 0) {
                _cooldownRemaining.value = currentMs / 1000f
                kotlinx.coroutines.delay(stepMs)
                currentMs -= stepMs
            }
            _cooldownRemaining.value = 0f
        }
    }

    fun sendMessage(userText: String, imageBitmap: Bitmap? = null) {
        if (userText.isBlank() && imageBitmap == null) return
        if (_isGenerating.value) return
        if (_cooldownRemaining.value > 0f) return

        startCooldown()

        val displayText = if (userText.isNotBlank()) userText.trim() else "Giải giúp em câu hỏi trong hình ảnh này:"
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = displayText,
            imageBitmap = imageBitmap
        )
        _messages.value = _messages.value + userMsg
        _isGenerating.value = true
        _errorMessage.value = null

        val aiPlaceholderId = UUID.randomUUID().toString()
        val aiPlaceholder = ChatMessage(
            id = aiPlaceholderId,
            sender = MessageSender.AI,
            text = "",
            isStreaming = true
        )
        _messages.value = _messages.value + aiPlaceholder

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                val generativeModel = Firebase.ai.generativeModel(
                    modelName = FREE_AI_MODEL,
                    systemInstruction = content { text(systemPrompt) },
                    generationConfig = generationConfig {
                        temperature = 0.3f
                    }
                )

                withTimeout(60_000L) {
                    if (imageBitmap != null) {
                        // Multimodal generation with camera/photo question
                        val inputContent = content {
                            image(imageBitmap)
                            text(displayText)
                        }
                        generativeModel.generateContentStream(inputContent).collect { chunk ->
                            val chunkText = chunk.text.orEmpty()
                            accumulated.append(chunkText)
                            val formulas = com.example.math.CalculatorFormulaSanitizer.extractFormulas(accumulated.toString())
                            _messages.value = _messages.value.map { msg ->
                                if (msg.id == aiPlaceholderId) {
                                    msg.copy(
                                        text = accumulated.toString(),
                                        isStreaming = true,
                                        extractedFormula = formulas.firstOrNull(),
                                        extractedFormulas = formulas
                                    )
                                } else msg
                            }
                        }
                    } else {
                        // Text conversation
                        val historyContents = _messages.value
                            .filter { it.id != aiPlaceholderId && it.sender != MessageSender.SYSTEM && it.imageBitmap == null }
                            .takeLast(8)
                            .map { msg ->
                                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                                content(role) { text(msg.text) }
                            }

                        val chatSession = generativeModel.startChat(history = historyContents.dropLast(1))
                        chatSession.sendMessageStream(displayText).collect { chunk ->
                            val chunkText = chunk.text.orEmpty()
                            accumulated.append(chunkText)
                            val formulas = com.example.math.CalculatorFormulaSanitizer.extractFormulas(accumulated.toString())
                            _messages.value = _messages.value.map { msg ->
                                if (msg.id == aiPlaceholderId) {
                                    msg.copy(
                                        text = accumulated.toString(),
                                        isStreaming = true,
                                        extractedFormula = formulas.firstOrNull(),
                                        extractedFormulas = formulas
                                    )
                                } else msg
                            }
                        }
                    }
                }

                val finalFormulas = com.example.math.CalculatorFormulaSanitizer.extractFormulas(accumulated.toString())
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == aiPlaceholderId) {
                        msg.copy(
                            text = if (accumulated.isNotBlank()) accumulated.toString() else "Không nhận được phản hồi từ TN AI.",
                            isStreaming = false,
                            extractedFormula = finalFormulas.firstOrNull(),
                            extractedFormulas = finalFormulas
                        )
                    } else msg
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "TN AI timeout", e)
                val timeoutText = accumulated.toString() + "\n\n⚠️ *Quá thời gian phản hồi (60 giây). Vui lòng thử lại với câu hỏi ngắn gọn hơn.*"
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == aiPlaceholderId) {
                        msg.copy(text = timeoutText, isStreaming = false)
                    } else msg
                }
                _errorMessage.value = "Quá thời gian phản hồi."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "TN AI error", e)
                val errorMsg = e.localizedMessage ?: "Lỗi kết nối AI"
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == aiPlaceholderId) {
                        msg.copy(
                            text = if (accumulated.isNotBlank()) accumulated.toString() + "\n\n⚠️ [Lỗi: $errorMsg]" else "⚠️ Đã xảy ra lỗi khi giải bài: $errorMsg",
                            isStreaming = false
                        )
                    } else msg
                }
                _errorMessage.value = errorMsg
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun extractCalculatorFormula(text: String): String? {
        return com.example.math.CalculatorFormulaSanitizer.extractFormulas(text).firstOrNull()
    }
}
