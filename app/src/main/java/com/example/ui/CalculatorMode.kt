package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class CalculatorMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val code: String
) {
    CALCULATE("calc", "Calculate", "Tính toán cơ bản & nâng cao", Icons.Default.Calculate, "1"),
    COMPLEX("complex", "Complex", "Số phức, modun, argument, cực", Icons.Default.Transform, "2"),
    BASE_N("base_n", "Base-N", "Hệ DEC, HEX, BIN, OCT & logic", Icons.Default.Code, "3"),
    MATRIX("matrix", "Matrix", "Ma trận 4x4, định thức, nghịch đảo", Icons.Default.GridOn, "4"),
    VECTOR("vector", "Vector", "Véc-tơ 2D/3D, tích vô hướng, góc", Icons.Default.Explore, "5"),
    STATISTICS("statistics", "Statistics", "Thống kê 1 biến & hồi quy 2 biến", Icons.Default.BarChart, "6"),
    DISTRIBUTION("distribution", "Distribution", "Phân phối Chuẩn, Nhị thức, Poisson", Icons.Default.ShowChart, "7"),
    SPREADSHEET("spreadsheet", "Spreadsheet", "Bảng tính 5 cột x 45 hàng, CSC/CSN", Icons.Default.TableChart, "8"),
    TABLE("table", "Table", "Bảng giá trị f(x), g(x) & đồ thị QR", Icons.Default.CalendarViewMonth, "9"),
    EQUATION("equation", "Equation", "Phương trình bậc 2-4 & Hệ PT 2-4 ẩn", Icons.Default.Functions, "A"),
    INEQUALITY("inequality", "Inequality", "Bất phương trình bậc 2, 3, 4", Icons.Default.CompareArrows, "B"),
    RATIO("ratio", "Ratio", "Tỷ lệ thức A:B = X:D / A:B = C:X", Icons.Default.Percent, "C"),
    MATH_BOX("math_box", "Math Box", "Tung xu, xúc xắc, vòng tròn, trục số", Icons.Default.Casino, "D"),
    VERIFY("verify", "Verify", "Kiểm tra Đúng/Sai biểu thức", Icons.Default.CheckCircle, "E"),
    ATOMIC_TABLE("atomic", "Bảng Nguyên tố & Hằng số", "Tra cứu khối lượng, Z & CODATA", Icons.Default.Science, "F"),
    CONVERSION("conversion", "Đổi đơn vị", "Độ dài, khối lượng, nhiệt độ, time, storage...", Icons.Default.SwapHoriz, "0"),
    FINANCE("finance", "Tài chính & Tiền tệ", "Tiền tệ, Tip, Giảm giá, Vay, Thuế/VAT", Icons.Default.AttachMoney, "G"),
    HEALTH("health", "Sức khỏe (BMI / Calo)", "Chỉ số BMI, BMR, TDEE calo mỗi ngày", Icons.Default.Favorite, "H"),
    DATE_TIME("date_time", "Ngày & Giờ", "Khoảng cách ngày, +/- Ngày, Tính tuổi", Icons.Default.Today, "I"),
    TN_AI("tn_ai", "TN AI", "Trợ lý giải bài tập Toán - Lý - Hóa", Icons.Default.AutoAwesome, "AI")
}

enum class LcdTheme(val displayName: String) {
    DARK("Tối hiện đại (Dark)"),
    LIGHT("Sáng thanh lịch (Light)"),
    AMOLED("Đen sâu (AMOLED)"),
    SOFT_BLUE("Xanh dịu (Slate)")
}
