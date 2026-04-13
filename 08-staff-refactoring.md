# Kế Hoạch Refactor Chức Năng Staff (Staff POS & Order Lookup)

## Bối Cảnh
Hiện tại, luồng Staff (Nhân viên bán vé & soát vé) có bộ khung giao diện khá hoàn thiện ở React (`BoxOfficePOS.jsx`, `OrderLookup.jsx`) nhưng tầng Backend lại chưa hỗ trợ đầy đủ các Logic riêng biệt cho Nhân viên, dẫn đến việc staff đang tái sử dụng tạm các endpoint `/api/public/` của Khách hàng, không có khả năng Cancel/Refund vé linh hoạt, cũng như chưa có cơ chế tính tiền / thanh toán linh hoạt cho màn hình POS.

Dựa theo tư duy kiến trúc từ **"Dive Into Design Patterns"**, sau đây là kế hoạch sử dụng các Mẫu Thiết Kế để giải quyết dứt điểm sự lộn xộn này:

---

## 1. Mảnh ghép B: OrderLookup & Booking Management 

### Giải Quyết Vòng Đời Của Vé — Áp dụng **State Pattern**
**Vấn đề:** 
Các nút bấm ở `OrderLookup.jsx` như `In lại vé`, `Hủy đơn`, `Hoàn tiền` hiện thực thi các lệnh phụ thuộc vào vô số câu lệnh `if/else` để kiểm tra `BookingStatus` hoặc `TicketStatus`. Nếu khách đã Check-in, việc Hủy Đơn sẽ gây hỏng toàn bộ logic doanh thu.

**Giải Pháp State Pattern:**
*   Tạo ra một Interface `TicketState` (hoặc `BookingState`) định nghĩa bộ quy tắc vòng đời chung: `confirm()`, `cancel()`, `checkIn()`, `refund()`.
*   Tạo các Concrete Classes tượng trưng cho trạng thái:
    *   `PendingState`: Vé đang chờ thanh toán -> Có thể chuyển sang `Cancel` hoặc `Confirm`.
    *   `PaidState`: Vé đã thanh toán thành công -> Có thể chuyển sang `CheckIn` (Soát vé vào rạp) hoặc `Refund` (Nếu policy cho phép hoàn).
    *   `CheckedInState`: Khách đã vào rạp -> Không được `Cancel` hay `Refund`.
    *   `RefundedState` / `CancelledState`: Trạng thái cuối.
*   **Hoàn thiện:** Staff muốn Hủy Vé, Controller chỉ cần gọi `ticket.getCurrentState().cancel()`, logic bên trong State sẽ tự hiểu có được hủy hay không hoặc ném lỗi ngay lập tức. Sạch sẽ và triệt để!

### Tìm Kiếm Đơn Hàng Linh Động — Áp dụng **Specification / Builder Pattern**
**Vấn đề:** 
Staff search bar yêu cầu tìm bằng **(Booking ID) HOẶC (Phone) HOẶC (Email)**. Viết Query `@Query("SELECT b FROM Booking b WHERE b.id = ?1 OR b.customer.phone = ?2...")` cứng rất khó bảo trì và dễ lỗi null parameters.

**Giải Pháp Specification:**
*   Sử dụng Spring Data JPA Specification (`BookingSpecification` implements `Specification<Booking>`).
*   Khởi tạo `BookingSpecificationBuilder` để nối cỗ máy Tìm Kiếm.
*   *Workflow:* Nếu Staff gõ Query là số, Builder chèn điều kiện tìm kiếm theo ID. Nếu có @, Builder tự nhét đuôi tìm Email. Nếu gõ toàn số điện thoại, nhét đuôi Phone. Gom lại thành một câu lệnh truy vấn WHERE linh động nối mạng.

---

## 2. Mảnh Ghép C: Frontend Staff POS

### Hoàn Tác (Undo/Redo) Thao Tác Chọn Ghế/Đồ Ăn — Áp dụng **Command Pattern**
**Vấn đề:** 
Tại quầy POS, khách rất hay thay đổi quyết định ("Thôi bỏ ghế này, thêm cái bắp nước kia, à mà thôi bỏ bắp nước lấy lại ghế đó"). Hệ thống Redux Slice mặc định sẽ khó theo dõi lại State Cũ.

**Giải pháp Command Pattern:**
*   Đóng gói mọi tương tác thay đổi giỏ hàng thành các Đối tượng Command:
    *   `AddSeatCommand(seatId)`
    *   `RemoveSeatCommand(seatId)`
    *   `AddFnbCommand(fnbId, qty)`
*   Trong POS, thay vì sửa trực tiếp vào biến `selectedSeats`, ta có một lớp `PosInvoker` chứa mảng `CommandHistory`. Mỗi khi Staff bấm "Thêm ghế", tạo lệnh đẩy vào History.
*   *Lợi Ích:* Lúc Staff ấn `Ctrl+Z` (Hoặc bấm nút Undo), hệ thống bốc Command gần nhất ra và kích hoạt cơ chế đảo ngược (`undo()`), giúp phục hồi lại Giỏ hàng như 2 giây trước mà không bị lag khung hình hay phải reset lại nguyên giỏ hàng cực nhọc.

---

## Kế Bước Tiếp Theo Với Tầng CODE (The Execution)

Là Senior Developer, tôi sẽ tiến hành cài đặt ngay theo thứ tự:

1. **Khởi tạo Specification Pattern Backend:** Chỉnh sửa lại hàm `getTicketsByBooking()` để chấp nhận một API Endpoint `/api/tickets/search` thay vì cứng ngắt lấy mỗi ID.
2. **Khởi tạo hệ thống State Pattern (Backend):** Mở rộng `Booking` có Interface `State` đính kèm. Tiến hành code các file State cho `Pending`, `Paid`, `Cancelled`. Nới lỏng các nút Hủy Đơn và Hoàn tiền phía React để gọi API này.
3. **Mở rộng Staff Backend Checkout:** Viết thêm `StaffBoxOfficeCheckoutProcess` cùng chiến lược thanh toán Tiền Mặt.
4. **Command Pattern vào Frontend:** Refactor nhẹ state management của file `BoxOfficePOS.jsx` cung cấp thêm cơ chế History Stack phục vụ undo nhanh tại quầy bán.
