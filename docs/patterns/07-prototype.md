# Pattern 07 — Prototype (Khuôn Mẫu Nhân Bản)

## 1. Lý thuyết

**Prototype** thuộc nhóm Creational. Ý tưởng: thay vì tạo đối tượng từ đầu bằng `new` + setup phức tạp, hãy **clone từ một template có sẵn**. Template (prototype) được khởi tạo 1 lần, mỗi lần cần object mới → gọi `copy()` → nhận bản sao ngay tức khắc.

```
Prototype (template gốc)
    └── copy() → bản sao → điền dữ liệu thực → sử dụng
```

---

## 2. Vấn đề (Trước khi áp dụng)

Sau mỗi lần thanh toán thành công, hệ thống gửi email vé cho khách. Email có cấu trúc phức tạp: tiêu đề, header rạp phim, nội dung, footer, chính sách. Nếu tạo mới mỗi lần:

```java
// ❌ Trước — build email từ đầu mỗi lần gửi
public void sendTicketEmail(Booking booking) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setSubject("Vé xem phim StarCine của bạn #" + booking.getBookingId());
    msg.setText(
        "Chào " + booking.getCustomer().getName() + ",\n\n" +
        "Cảm ơn bạn đã đặt vé tại StarCine.\n" +
        "Mã booking: " + booking.getBookingId() + "\n" +
        // ... 20 dòng format HTML/text template cứng ...
        "Trân trọng,\nStarCine Team"
    );
    mailSender.send(msg);
}
```

**Hậu quả:**
- Template email bị nhồi nhét vào service logic — khó maintain
- Thêm loại email mới (email hủy vé, email nhắc nhở) → copy-paste toàn bộ template string
- String concatenation lặp đi lặp lại mỗi request → tốn CPU
- Không tái sử dụng được cấu trúc email

---

## 3. Giải pháp — Prototype Pattern

Định nghĩa `EmailTemplate` interface với `copy()`. Spring quản lý prototype bean là template gốc. Mỗi khi gửi email: `copy()` → điền dữ liệu → `toMessage()` → send. Template gốc không bao giờ bị sửa.

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `EmailTemplate.java` | `patterns/prototype/` | **Interface** — contract cho `copy()` và `toMessage()` |
| `TicketEmailPrototype.java` | `patterns/prototype/` | **Prototype** — template email vé xem phim |
| `TicketEmailNotifier.java` | `patterns/mediator/` | Colleague dùng prototype để gửi email |

---

## 5. Code thực tế

### Interface — `EmailTemplate.java`

```java
public interface EmailTemplate {
    /** Trả về bản sao mới (fields trống, chưa điền dữ liệu) */
    EmailTemplate copy();

    /** Build SimpleMailMessage sau khi đã điền dữ liệu */
    SimpleMailMessage toMessage();
}
```

### Prototype — `TicketEmailPrototype.java`

```java
@Component  // Spring quản lý 1 instance gốc — Singleton + Prototype kết hợp
public class TicketEmailPrototype implements EmailTemplate {

    private String to;
    private Integer bookingId;
    private String customerName;
    private String movieTitle;
    private String showtime;
    private BigDecimal totalAmount;

    // Builder-style setters trả về this để chain
    public TicketEmailPrototype to(String to)               { this.to = to; return this; }
    public TicketEmailPrototype bookingId(Integer id)        { this.bookingId = id; return this; }
    public TicketEmailPrototype customerName(String name)    { this.customerName = name; return this; }
    public TicketEmailPrototype movieTitle(String title)     { this.movieTitle = title; return this; }
    public TicketEmailPrototype showtime(String st)          { this.showtime = st; return this; }
    public TicketEmailPrototype totalAmount(BigDecimal amt)  { this.totalAmount = amt; return this; }

    @Override
    public EmailTemplate copy() {
        return new TicketEmailPrototype();  // bản sao rỗng, tất cả fields null
    }

    @Override
    public SimpleMailMessage toMessage() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Vé xem phim StarCine của bạn #" + bookingId);
        msg.setText(
            "Chào " + customerName + ",\n\n" +
            "Cảm ơn bạn đã đặt vé tại StarCine.\n" +
            "Mã số đặt vé: " + bookingId + "\n" +
            "Phim: " + movieTitle + "\n" +
            "Suất chiếu: " + showtime + "\n" +
            "Tổng tiền: " + totalAmount + " VNĐ\n\n" +
            "Dùng mã booking để nhận vé tại quầy.\n\n" +
            "Trân trọng,\nStarCine Team"
        );
        return msg;
    }
}
```

### Sử dụng trong `TicketEmailNotifier.java`

```java
@Component
@RequiredArgsConstructor
public class TicketEmailNotifier implements PaymentColleague {

    private final TicketEmailPrototype ticketEmailPrototype;  // inject prototype gốc
    private final JavaMailSender mailSender;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        Booking booking = context.getBooking();

        // ✅ copy() → điền dữ liệu → gửi — prototype gốc không bị sửa
        TicketEmailPrototype email = (TicketEmailPrototype) ticketEmailPrototype.copy();
        email.to(booking.getCustomer().getEmail())
             .bookingId(booking.getBookingId())
             .customerName(booking.getCustomer().getName())
             .movieTitle(context.getMovieTitle())
             .showtime(context.getShowtimeDisplay())
             .totalAmount(booking.getTotalAmount());

        mailSender.send(email.toMessage());
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // không gửi email khi thất bại
    }
}
```

---

## 6. Luồng hoạt động thực tế

```
Thanh toán MoMo thành công
        │
        ▼
PostPaymentMediator.settleSuccess()
        │
        └─⑤ TicketEmailNotifier.onPaymentSuccess()
                │
                ├── ticketEmailPrototype.copy()
                │       └── new TicketEmailPrototype()  ← bản sao rỗng
                │
                ├── email.to(...).bookingId(...).movieTitle(...)...
                │       └── điền dữ liệu vào bản sao (prototype gốc KHÔNG bị động)
                │
                ├── email.toMessage()
                │       └── build SimpleMailMessage
                │
                └── mailSender.send(msg)  → gửi email
```

---

## 7. SOLID

| | Chi tiết |
|-|---------|
| **S** | `TicketEmailPrototype` chỉ biết cấu trúc email vé — không biết gửi mail |
| **O** | Email hủy vé → tạo `CancellationEmailPrototype implements EmailTemplate` mới |
| **L** | `TicketEmailNotifier` có thể làm việc với bất kỳ `EmailTemplate` nào |
| **D** | `TicketEmailNotifier` phụ thuộc `EmailTemplate` (interface), không `TicketEmailPrototype` cụ thể |

---

## 8. Thành quả

| Trước | Sau |
|-------|-----|
| Template string nhồi trong service | Template đóng gói trong class riêng |
| Email mới → copy-paste toàn bộ template | Email mới → tạo class implements `EmailTemplate` |
| Khó test (phụ thuộc MailSender) | Test `toMessage()` riêng không cần MailSender |
| Prototype gốc có thể bị sửa nhầm | `copy()` luôn trả bản sao mới — prototype gốc bất biến |

---

## 9. Test thủ công

```bash
# Sau checkout + thanh toán thành công:
POST /api/payment/momo/callback   Body: { "resultCode": 0, ... }

# Kiểm tra email (dùng Maildev hoặc SMTP giả)
# Email đến inbox của customer với nội dung:
# Subject: "Vé xem phim StarCine của bạn #123"
# Body: Chào [tên], mã booking: 123, phim: ..., suất: ..., tổng: ...
```
