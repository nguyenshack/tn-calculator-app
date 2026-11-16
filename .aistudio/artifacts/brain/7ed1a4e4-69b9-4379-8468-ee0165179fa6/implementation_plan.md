# Kế hoạch triển khai: Bong bóng chú thích siêu gọn có mũi tên trỏ & Chạm trực tiếp vào phím để chuyển bước

Người dùng phản hồi: Vùng khoanh sáng Spotlight đã chỉ đúng vị trí các nút 100%, nhưng thẻ chữ hướng dẫn cũ quá to làm che mất các phím xung quanh.

Theo lựa chọn của bạn:
1. **Hình thức thể hiện:** **Bong bóng chú thích nhỏ gọn (Compact Tooltip Bubble)** kèm mũi tên chỉ hướng trỏ thẳng vào cụm phím, tự động neo ngay phía trên hoặc phía dưới vùng sáng mà không che khuất phím.
2. **Cách tương tác:** **Cho phép chạm thẳng vào vùng sáng** hoặc chạm bất kỳ đâu trên màn hình để tiến tới bước tiếp theo một cách trực quan, nhanh chóng.

---

## 1. Thiết kế Bong bóng chú thích siêu gọn (`KeypadSpotlightTourOverlay.kt`)

### Cấu trúc và Kích thước:
- Chiều cao thu gọn tối đa (~100 - 110dp), chiều rộng vừa vặn (~300 - 320dp) thay vì chiếm trọn màn hình.
- **Vị trí thông minh theo tọa độ thực:**
  - Nếu cụm phím nằm ở nửa dưới màn hình (`targetRect.top > 280dp`): Bong bóng nổi **ngay phía trên cụm phím** với mũi tên nhọn `▼` trỏ xuống tâm cụm phím.
  - Nếu cụm phím nằm ở nửa trên màn hình (`targetRect.top <= 280dp`): Bong bóng nổi **ngay phía dưới cụm phím** với mũi tên nhọn `▲` trỏ lên tâm cụm phím.
- **Nội dung súc tích, tinh gọn:**
  - Tiêu đề đậm + huy hiệu bước `[1/8]`, `[2/8]`.
  - 1-2 câu giải thích ngắn gọn, đi thẳng vào công dụng cốt lõi của phím.
  - Hàng nút nhỏ: nút lùi `[◄]`, tiến `[►]` và gợi ý *"Chạm vào phím để tiếp tục"*.
- **Mũi tên chỉ hướng hoạt hóa:**
  - Tam giác / mũi tên nhọn viền neon nhấp nháy đồng bộ với vùng sáng, trỏ chuẩn xác vào trọng tâm của nhóm phím.

---

## 2. Cơ chế Chạm trực tiếp để chuyển bước

- Khi người dùng **chạm thẳng vào vùng phím đang phát sáng** (hoặc chạm vào bong bóng, hoặc chạm bất kỳ đâu):
  - Tự động chuyển mượt mà sang bước tiếp theo (`currentStepIndex++`).
  - Nếu đang ở bước cuối cùng (8/8): Chạm sẽ hoàn tất và đóng tour ngay.
- Người dùng không cần phải với tay tìm nút "Tiếp theo", tạo trải nghiệm tương tác liền mạch và thoải mái khi cầm điện thoại 1 tay.
- Nút "Bỏ qua" nhỏ gọn ở góc trên màn hình tiếp tục khả dụng.

---

## 3. Các file sẽ chỉnh sửa

- `app/src/main/java/com/example/ui/components/KeypadSpotlightTourOverlay.kt`:
  - Thiết kế lại layout thành Bong bóng Tooltip (Bubble with Arrow Pointer).
  - Tích hợp `Modifier.pointerInput` / `clickable` trên toàn màn hình và trên vùng sáng để chạm là chuyển bước.
  - Tối ưu hóa văn bản cô đọng, sắc nét.

---

## 4. Kế hoạch kiểm thử & Xác thực

- Biên dịch toàn bộ bằng `compile_applet`.
- Xác nhận bong bóng xuất hiện khéo léo ngay sát cụm phím với mũi tên trỏ, tuyệt đối không che khuất phím đang khoanh vùng.
- Kiểm tra tính năng chạm trực tiếp vào vùng phím để chuyển bước mượt mà.
