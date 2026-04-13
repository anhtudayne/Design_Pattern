# BÁO CÁO ÁP DỤNG DESIGN PATTERNS - ROLE STAFF (POS & BACKEND)

Dưới đây là báo cáo phân tích chi tiết về sự hiện diện của **Design Patterns** được áp dụng riêng biệt và đóng vai trò quan trọng trong các tính năng dành cho **Role Staff (Nhân viên tại quầy bán vé POS)**.

---

## 1. Command Pattern (Frontend)
**Chức năng áp dụng:** Tính năng Undo/Redo khi chọn ghế và thêm thức ăn (F&B) tại giao diện POS (Point of Sales).
**Vị trí Code:** 
- `frontend/src/pages/staff/BoxOfficePOS.jsx`
- `frontend/src/patterns/posCommands.js`

**Cách hoạt động & Phân tích:**
Trong giao diện POS, nhân viên thao tác rất nhanh khi khách yêu cầu. Họ có thể thêm/bớt ghế hoặc bắp nước liên tục và đôi khi bấm nhầm. 
Thay vì trực tiếp sửa đổi mảng State của React `[...setSeats]`, dev đã thiết kế mô hình **Command Pattern**:
1. **Command Interface / Classes:** Có các class cụ thể như `AddSeatCommand`, `RemoveSeatCommand`, `AddFnbCommand`, `RemoveFnbCommand`. Mỗi class đều đóng gói thao tác `execute()` và `undo()`.
2. **Invoker:** Một class `PosCommandInvoker` đóng vai trò lưu trữ lịch sử các Command đã thực thi (history stack) và stack con trỏ hiện tại.
3. Khi nhân viên click chọn Ghế/Bắp nước, giao diện tạo ra một Command và ném cho Invoker để thực thi.
4. **Undo/Redo:** Khi nhân viên bấm `Ctrl + Z` hoặc nhấn nút "Undo", Invoker đẩy lùi con trỏ (pop) và gọi hàm `undo()` của Command gần nhất, trả Giao diện về y hệt trạng thái trước đó một cách mượt mà.

---

## 2. Template Method Pattern (Backend)
**Chức năng áp dụng:** Quy trình thanh toán tiền mặt (Cash Checkout) cho Khách trực tiếp tại quầy POS.
**Vị trí Code:** 
- `AbstractCheckoutTemplate.java` (Class gốc)
- `StaffCashCheckoutProcess.java` (Class hiện thực hóa cho Staff)

**Cách hoạt động & Phân tích:**
Khác với Workflow của Khách hàng tự đi mua (phải qua cổng MoMo chờ redirect), quá trình thanh toán của Staff tại quầy là đưa tiền trực tiếp và xuất vé ngay.
- **AbstractCheckoutTemplate** định nghĩa một bộ khung thuật toán bất biến bọc trong một Transaction (Bộ xương thuật toán Checkout chuẩn): `validateUser` -> `validateSeats` -> `calculatePrice` -> **`determineInitialBookingStatus`** -> `createBooking` -> `saveFnbs` -> **`processPayment`** -> **`finalizeBooking`**.
- Tại **StaffCashCheckoutProcess**, các hàm được in đậm phía trên sẽ được Override lại như sau:
  - `determineInitialBookingStatus`: Ép và Trả về trạng thái `CONFIRMED` lập tức (Thay vì PENDING).
  - `processPayment`: Sinh ra ngay record hóa đơn với thuộc tính `method = CASH` và `status = SUCCESS` thay vì phải gọi tạo Payment Request tới Server của hệ thống cổng thanh toán MoMo.
  - `finalizeBooking`: Thực hiện thao tác xuất vé (Create Tickets entity) nhét DB ngay cho người dùng mà không cần chờ MoMo Webhook (IPN) ping về xác nhận.
- **Giá trị cốt lõi:** Pattern này giúp tái sử dụng lại phần lớn lượng code (ví dụ: tìm giá, lưu giỏ hàng) và cho phép thêm những Workflow thanh toán khác ở tương lai (như thanh toán thẻ ATM tại POS) mà không sợ phải đi sao chép sửa đổi code lung tung.

---

## 3. Strategy Pattern (Backend)
**Chức năng áp dụng:** Cơ chế rẽ nhánh chiến lược thanh toán tại endpoint `/payment/staff/cash-checkout`.
**Vị trí Code:** 
- `PaymentStrategyFactory.java`
- `CashPaymentStrategy.java`
- `CheckoutServiceImpl.java`

**Cách hoạt động & Phân tích:**
- Mặc dù hệ thống áp dụng Template Method để xử lý Checkout, việc Endpoint gọi tới Template nào là trách nhiệm của Strategy.
- Khi Request từ Frontend Staff POS truyền lên yêu cầu thanh toán với biến phân loại, `PaymentStrategyFactory` sẽ phân tích và chọn lấy `CashPaymentStrategy`.
- Strategy này tự động thiết lập bối cảnh (Context) chạy thông qua Template Method `StaffCashCheckoutProcess` và trả về một Map JSON kết quả tùy biến rất khác biệt so với kết quả trả về của `MomoPaymentStrategy` (chỉ trả về một URL chuyển hướng).
- Pattern tách rời logic phức tạp của mỗi cổng thành các class gọn gàng đằng sau Factory.

---

## 4. State Pattern (Backend)
**Chức năng áp dụng:** Quy trình quản lý trạng thái vé (Manager Booking States: In vé, Hủy vé, Hoàn tiền) - Tính năng độc quyền cho Staff/Admin trong Dashboard.
**Vị trí Code:**
- Nằm trong Package: `patterns/state/` (`BookingContext.java`, `ConfirmedState.java`, `PendingState.java`, `RefundedState.java`, v.v.)
- `BookingController.java` (`/{bookingId}/cancel`, `/{bookingId}/print`, `/{bookingId}/refund`)

**Cách hoạt động & Phân tích:**
Một đơn Booking có nhiều vòng đời: `PENDING` -> `CONFIRMED` -> `PRINTED` / `CANCELLED` / `REFUNDED`.
- Thay vì để một lớp Service duy nhất chứa hàng loạt cấu trúc `if (booking.getStatus() == "CONFIRMED") { throw error... } else if (booking.getStatus() == "REFUNDED") { ... }`, hệ thống dùng **State Pattern** để quy tụ hành vi của mỗi Trạng thái vào class tương ứng.
- Khi Staff gọi thao tác **Refund (Hoàn tiền)**:
  - Hệ thống tải lên `BookingContext` với trạng thái hiện tại (ví dụ: `ConfirmedState`).
  - Gọi `context.refund()` -> `ConfirmedState` chịu trách nhiệm xử lý logic và chuyển nó sang `RefundedState`.
  - Nếu Staff cố tình gọi `context.printTickets()` trên một đơn đang ở trạng thái `RefundedState`, class `RefundedState` sẽ chủ động throw Runtime Exception thông báo thao tác bất hợp lệ.
- **Giá trị cốt lõi:** Bảo vệ tính toàn vẹn và nguyên tắc Solid, khiến việc nâng cấp thêm loại thao tác nào hay trạng thái nào dễ dàng tuyệt đối (VD: thêm trạng thái Lỗi Hoàn Tiền, hay Đang Thanh Toán Ngân Hàng).

---

**Kết Luận Chung:**
Các Pattern như **Command Pattern** làm tăng tính thân thiện và UX cực chuẩn mực trong Web POS ở Frontend. Trong khi đó cụm **Template Method**, **Strategy** và **State** bảo mật luồng nghiệp vụ trên Backend khép kín, an toàn phân mảng được dữ liệu và mở rộng không ngừng đổi mới về các chuẩn giao dịch. Code hiện tại bám khá chuẩn các tiêu chí học thuật của GoF (Gang of Four).
