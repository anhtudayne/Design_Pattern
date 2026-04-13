# Pattern 01 — Chain of Responsibility (Chuỗi Trách Nhiệm)

## 1. Lý thuyết

**Chain of Responsibility** thuộc nhóm Behavioral (Hành vi). Ý tưởng cốt lõi: thay vì một đối tượng xử lý tất cả mọi thứ, ta tạo ra một **chuỗi các handler**. Mỗi handler chỉ làm một việc duy nhất — nếu không xử lý được, chuyển cho handler kế tiếp.

```
Request → Handler1 → Handler2 → Handler3 → ... → Response
```

Người gọi chỉ biết đầu chuỗi. Không biết bên trong có bao nhiêu handler.

---

## 2. Vấn đề trong dự án (Trước khi áp dụng)

Khi user đặt vé, `CheckoutServiceImpl.createBooking()` phải kiểm tra nhiều điều kiện:

```java
// ❌ Trước — toàn bộ validation nhồi trong 1 method dài
public String createBooking(...) {
    // Kiểm tra số ghế
    if (seatIds == null || seatIds.isEmpty()) throw new RuntimeException("...");
    if (seatIds.size() > 8) throw new RuntimeException("...");

    // Kiểm tra user
    User user = userRepository.findById(userId).orElseThrow(...);
    if (!(user instanceof Customer)) throw new RuntimeException("...");

    // Kiểm tra suất chiếu
    Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow(...);

    // Kiểm tra ghế chưa bán
    for (Integer seatId : seatIds) {
        if (ticketRepository.existsByShowtime_...AndSeat_...(showtimeId, seatId))
            throw new RuntimeException("...");
    }

    // ... 50 dòng logic booking tiếp theo ...
}
```

**Hậu quả:**
- Method 100+ dòng, vi phạm **SRP** (Single Responsibility)
- Thêm rule mới (ví dụ: kiểm tra showtime chưa chiếu) → phải mở `createBooking()` ra sửa → vi phạm **OCP**
- Khó test từng rule riêng lẻ
- Khó đọc, khó maintain

---

## 3. Giải pháp — Áp dụng Chain of Responsibility

Tách mỗi rule validation thành một class handler riêng, nối thành chuỗi. `CheckoutServiceImpl` chỉ còn gọi `chain.handle(context)`.

### Chuỗi handler thực tế trong dự án

```
MaxSeatsHandler → UserExistsHandler → ShowtimeExistsHandler → SeatsNotSoldHandler
```

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `CheckoutValidationHandler.java` | `patterns/chainofresponsibility/` | **Interface** — định nghĩa contract |
| `AbstractCheckoutValidationHandler.java` | `patterns/chainofresponsibility/` | **Abstract base** — template method |
| `CheckoutValidationContext.java` | `patterns/chainofresponsibility/` | **DTO** — dữ liệu truyền qua chain |
| `MaxSeatsHandler.java` | `patterns/chainofresponsibility/` | Rule: 1–8 ghế |
| `UserExistsHandler.java` | `patterns/chainofresponsibility/` | Rule: user tồn tại + là Customer |
| `ShowtimeExistsHandler.java` | `patterns/chainofresponsibility/` | Rule: suất chiếu tồn tại |
| `SeatsNotSoldHandler.java` | `patterns/chainofresponsibility/` | Rule: ghế chưa bán |
| `CheckoutValidationConfig.java` | `patterns/chainofresponsibility/` | **@Configuration** — nối chuỗi |

---

## 5. Code thực tế (key files)

### Interface — `CheckoutValidationHandler.java`
```java
public interface CheckoutValidationHandler {
    void setNext(CheckoutValidationHandler next);
    void handle(CheckoutValidationContext context);
}
```

### Abstract base — `AbstractCheckoutValidationHandler.java`
```java
public abstract class AbstractCheckoutValidationHandler implements CheckoutValidationHandler {
    private CheckoutValidationHandler next;

    @Override
    public void handle(CheckoutValidationContext context) {
        doHandle(context);           // xử lý rule của mình
        if (next != null) {
            next.handle(context);   // chuyển sang handler tiếp theo
        }
    }

    protected abstract void doHandle(CheckoutValidationContext context);
}
```

### Concrete handler — `MaxSeatsHandler.java`
```java
@Component
public class MaxSeatsHandler extends AbstractCheckoutValidationHandler {
    private static final int MAX_SEATS_PER_BOOKING = 8;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        if (context.getSeatIds() == null || context.getSeatIds().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất 1 ghế.");
        }
        if (context.getSeatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw new RuntimeException("Không được đặt quá " + MAX_SEATS_PER_BOOKING + " ghế trong một lần.");
        }
    }
}
```

### Concrete handler — `UserExistsHandler.java`
```java
@Component
@RequiredArgsConstructor
public class UserExistsHandler extends AbstractCheckoutValidationHandler {
    private final UserRepository userRepository;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        User user = userRepository.findById(context.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        if (!(user instanceof Customer)) {
            throw new RuntimeException("Chỉ có Customer mới có thể đặt vé.");
        }
        context.setUser(user);  // cache để handler sau không query lại
    }
}
```

### Config — `CheckoutValidationConfig.java` (nối chuỗi)
```java
@Configuration
public class CheckoutValidationConfig {
    @Bean
    public CheckoutValidationHandler checkoutValidationChain(
            MaxSeatsHandler maxSeatsHandler,
            UserExistsHandler userExistsHandler,
            ShowtimeExistsHandler showtimeExistsHandler,
            SeatsNotSoldHandler seatsNotSoldHandler) {

        maxSeatsHandler.setNext(userExistsHandler);
        userExistsHandler.setNext(showtimeExistsHandler);
        showtimeExistsHandler.setNext(seatsNotSoldHandler);

        return maxSeatsHandler;  // trả về đầu chuỗi
    }
}
```

### Kết quả — `CheckoutServiceImpl` sau refactor
```java
// ✅ Sau — chỉ còn 1 dòng validation
@Autowired
private CheckoutValidationHandler checkoutValidationChain;

public String createBooking(...) {
    CheckoutValidationContext ctx = CheckoutValidationContext.builder()
        .userId(userId).showtimeId(showtimeId).seatIds(seatIds).build();

    checkoutValidationChain.handle(ctx);  // toàn bộ chain chạy ở đây

    // ... tiếp tục tạo booking ...
}
```

---

## 6. Luồng hoạt động thực tế

```
POST /api/payment/checkout
        │
        ▼
CheckoutServiceImpl.createBooking()
        │
        ├── MaxSeatsHandler         → seatIds có 9 phần tử → throw "Không được đặt quá 8 ghế"
        │                              seatIds hợp lệ → next
        ├── UserExistsHandler       → userId không tồn tại → throw
        │                              user là Customer → cache context.user → next
        ├── ShowtimeExistsHandler   → showtimeId không tồn tại → throw
        │                              showtime tồn tại → cache context.showtime → next
        └── SeatsNotSoldHandler     → seatId đã có ticket → throw "Ghế đã được bán"
                                       tất cả ghế trống → pass ✅
```

---

## 7. SOLID

| | Chi tiết |
|-|---------|
| **S** | `MaxSeatsHandler` chỉ check số ghế; `UserExistsHandler` chỉ check user — mỗi class 1 trách nhiệm |
| **O** | Thêm rule "showtime chưa hết" → thêm `ShowtimeNotEndedHandler` mới, không sửa class cũ |
| **L** | Mọi handler thay thế nhau qua `CheckoutValidationHandler` interface |
| **I** | Interface chỉ 2 method hẹp: `setNext()` + `handle()` |
| **D** | `CheckoutServiceImpl` inject `CheckoutValidationHandler` (interface), Spring inject concrete chain |

---

## 8. Thành quả

| Trước | Sau |
|-------|-----|
| `createBooking()` 100+ dòng nhồi nhét validation | Method gọn, 1 dòng `chain.handle(ctx)` |
| Thêm rule → sửa method cũ | Thêm rule → tạo class mới, đăng ký vào `@Configuration` |
| Test validation khó (phải mock cả service) | Test từng handler riêng lẻ, không cần Spring context |
| Lỗi validation khó xác định nguồn gốc | Message lỗi rõ ràng từ đúng handler vi phạm |
| `context.showtime` query lại nhiều lần | Cache trên `CheckoutValidationContext` — query 1 lần |

---

## 9. Test thủ công (Postman/Swagger)

```
POST /api/payment/checkout
Authorization: Bearer {jwt_token}
Body:
{
  "showtimeId": 1,
  "seatIds": [1, 2],
  "fnbs": [],
  "promoCode": null
}
```

| Kịch bản | Thay đổi | Expected |
|----------|---------|---------|
| Happy path | Dữ liệu hợp lệ | `200 OK` + orderId MoMo |
| > 8 ghế | `seatIds` có 9 phần tử | `400` — "Không được đặt quá 8 ghế" |
| User không tồn tại | `userId = 99999` | `400` — "Người dùng không tồn tại" |
| Ghế đã bán | seatId đã có ticket | `400` — "Ghế đã được bán" |
