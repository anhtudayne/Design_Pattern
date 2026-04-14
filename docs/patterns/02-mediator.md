# Pattern 02 — Mediator (Trung Gian)

## 1. Lý thuyết

**Mediator** thuộc nhóm Behavioral. Ý tưởng: thay vì các đối tượng biết và gọi nhau trực tiếp (tạo ra mạng lưới phụ thuộc chằng chịt), mọi giao tiếp đều đi qua **một đối tượng trung tâm** (Mediator). Các đối tượng chỉ biết Mediator, không biết nhau.

```
❌ Trước (gọi trực tiếp):           ✅ Sau (qua Mediator):
A ←→ B                              A → Mediator → B
A ←→ C                              A → Mediator → C
B ←→ C                              B → Mediator → C
```

---

## 2. Vấn đề trong dự án (Trước khi áp dụng)

Khi MoMo xác nhận thanh toán thành công, hệ thống cần thực hiện **5 tác vụ theo thứ tự**:
1. Cập nhật trạng thái Booking → `CONFIRMED`
2. Cộng `totalSpending` cho Customer
3. Tạo Ticket cho từng ghế đã chọn
4. Cập nhật trạng thái Payment → `COMPLETED`
5. Gửi email vé đến khách hàng

Nếu không có Mediator:

```java
// ❌ Trước — PaymentController/CallbackService làm tất cả
public void handleCallback(MomoCallbackRequest request) {
    // 1. Cập nhật Booking
    Booking booking = bookingRepository.findById(...).orElseThrow();
    booking.confirm();
    bookingRepository.save(booking);

    // 2. Cộng spending
    Customer customer = booking.getCustomer();
    customerRepository.increaseTotalSpending(customer.getUserId(), request.getAmount());

    // 3. Tạo Ticket (vòng lặp dài)
    for (Integer seatId : seatIds) {
        Seat seat = seatRepository.findById(seatId)...;
        ticketRepository.save(new Ticket(...));
    }

    // 4. Cập nhật Payment
    payment.setStatus(PaymentStatus.COMPLETED);
    paymentRepository.save(payment);

    // 5. Gửi email
    emailService.sendTicketEmail(booking.getBookingId());
}
```

**Hậu quả:**
- `CallbackService` phụ thuộc trực tiếp vào 6-7 repository/service khác nhau — **coupling** cực cao
- Thêm tác vụ mới (ví dụ: gửi push notification) → phải sửa file callback
- Khó test từng tác vụ riêng lẻ
- Thứ tự thực thi bị ràng buộc cứng vào 1 method

---

## 3. Giải pháp — Áp dụng Mediator

`PostPaymentMediator` đóng vai trò trung tâm. Mỗi tác vụ là một `PaymentColleague` độc lập — không biết nhau, chỉ nhận lệnh từ Mediator.

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `PaymentColleague.java` | `patterns/mediator/` | **Interface** — contract cho mọi colleague |
| `MomoCallbackContext.java` | `patterns/mediator/` | **DTO** — dữ liệu chia sẻ giữa các colleague |
| `PostPaymentMediator.java` | `patterns/mediator/` | **Mediator** — điều phối theo thứ tự |
| `BookingStatusUpdater.java` | `patterns/mediator/` | Tác vụ 1: cập nhật Booking |
| `UserSpendingUpdater.java` | `patterns/mediator/` | Tác vụ 2: cộng spending cho Customer |
| `TicketIssuer.java` | `patterns/mediator/` | Tác vụ 3: tạo Ticket rows |
| `PaymentStatusUpdater.java` | `patterns/mediator/` | Tác vụ 4: cập nhật Payment |
| `TicketEmailNotifier.java` | `patterns/mediator/` | Tác vụ 5: gửi email vé |

---

## 5. Code thực tế (key files)

### Interface — `PaymentColleague.java`
```java
public interface PaymentColleague {
    void onPaymentSuccess(MomoCallbackContext context);
    void onPaymentFailure(MomoCallbackContext context);
}
```

### Mediator — `PostPaymentMediator.java`
```java
@Service
public class PostPaymentMediator {

    private final List<PaymentColleague> colleagues;

    @Autowired
    public PostPaymentMediator(
            BookingStatusUpdater bookingStatusUpdater,
            UserSpendingUpdater userSpendingUpdater,
            TicketIssuer ticketIssuer,
            PaymentStatusUpdater paymentStatusUpdater,
            TicketEmailNotifier ticketEmailNotifier) {
        // Thứ tự thực hiện được định nghĩa tại đây — 1 chỗ duy nhất
        this.colleagues = Arrays.asList(
                bookingStatusUpdater,
                userSpendingUpdater,
                ticketIssuer,
                paymentStatusUpdater,
                ticketEmailNotifier
        );
    }

    public void settleSuccess(MomoCallbackContext context) {
        for (PaymentColleague colleague : colleagues) {
            colleague.onPaymentSuccess(context);
        }
    }

    public void settleFailure(MomoCallbackContext context) {
        for (PaymentColleague colleague : colleagues) {
            colleague.onPaymentFailure(context);
        }
    }
}
```

### Concrete colleague — `BookingStatusUpdater.java`
```java
@Component
@RequiredArgsConstructor
public class BookingStatusUpdater implements PaymentColleague {
    private final BookingRepository bookingRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        context.getBooking().confirm();           // set status = CONFIRMED
        bookingRepository.save(context.getBooking());
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        context.getBooking().setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(context.getBooking());
    }
}
```

### Concrete colleague — `TicketIssuer.java` (phức tạp nhất)
```java
@Component
@RequiredArgsConstructor
public class TicketIssuer implements PaymentColleague {
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        Showtime showtime = showtimeRepository.findById(context.getShowtimeId()).orElseThrow();

        for (Integer seatId : context.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null) {
                BigDecimal ticketPrice = showtime.getBasePrice()
                    .add(seat.getSeatType() != null ? seat.getSeatType().getPriceSurcharge() : BigDecimal.ZERO);
                ticketRepository.save(Ticket.builder()
                    .booking(context.getBooking())
                    .seat(seat).showtime(showtime).price(ticketPrice)
                    .build());
            }
        }
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // Không tạo ticket khi thanh toán thất bại
    }
}
```

### Kết quả — CheckoutServiceImpl sau khi có Mediator
```java
// ✅ Sau — chỉ gọi 1 dòng
@Autowired
private PostPaymentMediator postPaymentMediator;

public void processMomoCallback(MomoCallbackRequest request) {
    // ... xác thực signature, tìm booking ...

    MomoCallbackContext context = MomoCallbackContext.builder()
        .callback(request).booking(booking).seatIds(seatIds).build();

    if (isSuccess) {
        postPaymentMediator.settleSuccess(context);  // 5 tác vụ chạy theo thứ tự
    } else {
        postPaymentMediator.settleFailure(context);
    }
}
```

---

## 6. Luồng hoạt động thực tế

```
MoMo gọi POST /api/payment/momo/callback
        │
        ▼
CheckoutServiceImpl.processMomoCallback()
        │ xác thực chữ ký HMAC-SHA256
        │ tìm Booking theo orderId
        ▼
PostPaymentMediator.settleSuccess(context)
        │
        ├─① BookingStatusUpdater   → Booking.status = CONFIRMED → save
        ├─② UserSpendingUpdater    → Customer.totalSpending += amount → save
        ├─③ TicketIssuer           → tạo N Ticket rows (N = số ghế) → save
        ├─④ PaymentStatusUpdater   → Payment.status = COMPLETED → save
        └─⑤ TicketEmailNotifier    → emailService.sendTicketEmail(bookingId)
```

---

## 7. SOLID

| | Chi tiết |
|-|---------|
| **S** | `BookingStatusUpdater` chỉ update Booking; `TicketIssuer` chỉ tạo ticket; không class nào làm 2 việc |
| **O** | Thêm tác vụ mới (push notification) → tạo `PushNotificationColleague` mới + thêm vào list Mediator |
| **I** | `PaymentColleague` chỉ 2 method: `onPaymentSuccess` + `onPaymentFailure` |
| **D** | `PostPaymentMediator` phụ thuộc `PaymentColleague` (interface), không biết class cụ thể |

---

## 8. Thành quả

| Trước | Sau |
|-------|-----|
| Callback method 80+ dòng, import 6-7 repository | `settleSuccess()` 3 dòng |
| Thêm tác vụ → sửa file callback | Tạo class colleague mới, thêm vào list Mediator |
| Khó test từng tác vụ (phải mock cả hệ thống) | Test từng colleague riêng với mock repository |
| Thứ tự thực thi bị ẩn trong code | Thứ tự rõ ràng, nhìn vào constructor `PostPaymentMediator` là biết |
| Coupling cao (callback biết tất cả) | Coupling thấp: callback chỉ biết `PostPaymentMediator` |

---

## 9. Test thủ công

Sau khi checkout + thanh toán MoMo thành công:

```bash
# Kiểm tra booking status
GET /api/booking/{bookingId}
→ status: "CONFIRMED"

# Kiểm tra ticket đã tạo
GET /api/tickets/booking/{bookingId}
→ danh sách N ticket tương ứng N ghế

# Kiểm tra email (cần Maildev hoặc SMTP test)
→ inbox nhận email "Vé xem phim StarCine của bạn #..."
```

**Demo mode** (không cần MoMo thật):
```
POST /api/payment/checkout/demo
Body: { ..., "success": true }
→ toàn bộ 5 tác vụ chạy, kiểm tra kết quả như trên
```
