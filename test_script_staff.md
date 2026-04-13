# KỊCH BẢN TEST DESIGN PATTERNS - ROLE STAFF

Kịch bản này giúp bạn chứng minh trực tiếp hiệu quả của 4 Design Pattern trong lúc thuyết trình hoặc demo đồ án.

## Đăng nhập thiết lập

1. Khởi động hệ thống (Front-end: `http://localhost:5173`).
2. Mở màn hình đăng nhập.
3. Nhập email: `staff@f3.vn` / Mật khẩu: `password123`.
4. Trình duyệt tự động chuyển hướng vào màn hình POS (Point of Sales).

---

## BƯỚC 1: Hiện thực Command Pattern (Tính năng Undo/Redo)

**Mục đích:** Chứng minh tính năng lưu trữ lịch sử thao tác linh hoạt.

**Kịch bản thực hiện:**

1. Trên màn hình POS, hãy làm theo quy trình: Chọn Rạp (Vd: Galaxy) -> Chọn Phim (Dune 2) -> Chọn Suất chiếu bất kỳ.
2. Tại màn hình sơ đồ ghế, click chọn lần lượt 3 ghế (Ví dụ: `A1`, `A2`, `A3`).
3. Dùng chuột click vào ô "Thêm Bắp/Nước", chọn thêm 1 Combo Bắp & Nước.
4. **Hành động Test (Undo):**
   - Click nút **Undo** (Trả lại) hoặc bấm tổ hợp phím **Ctrl + Z**.
   - **Kết quả mong đợi:** Combo Bắp Nước tự động biến mất khỏi giỏ hàng.
   - Bấm tiếp **Ctrl + Z** thêm 1 lần nữa.
   - **Kết quả mong đợi:** Ghế `A3` bị hủy chọn trên màn hình, tổng tiền tự giảm xuống.
5. **Hành động Test (Redo):**
   - Click nút **Redo** (Làm lại) hoặc bấm tổ hợp phím **Ctrl + Y**.
   - **Kết quả mong đợi:** Ghế `A3` lại được tô màu, tiền tăng lại như cũ.

_==> Chứng tỏ: Command Pattern đang bọc từng cú click người dùng thành Object, cho phép Invoker tua lùi hoặc tiến dòng sự kiện hoàn hảo._

---

## BƯỚC 2: Hiện thực Template Method & Strategy Pattern (Staff Cash Checkout)

**Mục đích:** Chứng minh việc áp dụng Strategy chọn luồng Cash và chạy khung Template xuất vé ngay bỏ qua MoMo.

**Kịch bản thực hiện:**

1. Ngay tại giỏ hàng đang chọn ở **BƯỚC 1** (giả sử đã chọn xong ghế).
2. Click chọn nút **"Thanh toán Tiền mặt"** (hoặc Cash Checkout) ở cột bên phải.
3. **Kết quả mong đợi:**
   - Hệ thống không hề bật cửa sổ tạo mã QR hay Redirect tới MoMo như lúc một Khách hàng bình thường tự đặt vé.
   - Trả về màn hình thông báo **"THANH TOÁN THÀNH CÔNG"** màu xanh ngay lập tức sau 1 giây.
   - Màn hình POS tự động xóa trắng giỏ hàng (reset) để chuẩn bị đón khách thứ 2.
4. **Xác thực dưới Backend Database:** Bạn (hoặc Giảng viên) có thể xem lại trong bảng `bookings` và `payments` ở MySQL. Mọi vé tạo ra kiểu này ngay lập tức mang trạng thái `status = CONFIRMED` và `payment_method = CASH`, `payment_status = SUCCESS`.

_==> Chứng tỏ: Strategy Pattern đã rẽ nhánh thành công sang CashPaymentStrategy, và Template Method đã cắt bỏ khâu chờ thanh toán để xác nhận cứng (Hard Commit) ngay vào DB._

---

## BƯỚC 3: Hiện thực State Pattern (Chuyển đổi trạng thái đơn vé)

**Mục đích:** Chứng minh hệ thống từ chối các thao tác bất hợp lệ dựa trên Trạng thái của vé hiện hành thay vì dùng vòng lặp if-else dài dòng.

**Kịch bản thực hiện:**

1. Rời khỏi BoxOffice POS, dùng menu bên trái chuyển sang màn hình **Lịch sử Giao Dịch** hoặc **Quản lý Booking**.
2. Tìm kiếm đúng Đơn vé (Booking) mà bạn vừa thanh toán tiền mặt ở **BƯỚC 2** (trạng thái đang là `CONFIRMED`).
3. Click vào nút hành động **Hoàn tiền** (Refund) cho đơn vé này.
   - **Kết quả mong đợi:** Đơn vé cập nhật ngay lập tức sang Trạng thái `REFUNDED` (hoặc `CANCELLED`).
4. **Hành động Test cố ý làm lỗi:**
   - Cố tình click vào nút **In Vé** (Print Ticket) hoặc **Hoàn_Tiền** một lần nữa trên chính đơn vé bị `REFUNDED` đó.
   - **Kết quả mong đợi:** Hệ thống chặn đứng và ném ra thông báo lỗi đỏ `Thao tác không hợp lệ: Vé đã hoàn tiền, không thể in lại`.
5. **Chứng minh kịch thuật:** Tại backend, do State hiện tại là `RefundedState`, khi class này nhận được lệnh gọi từ controller `print()`, nó không có logic in mà tự bung exception `throw new RuntimeException`.

_==> Chứng tỏ: Logic trạng thái đóng cục bên trong class Object cực kì an toàn và dứt khoát._
