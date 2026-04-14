# Báo Cáo Chi Tiết — 7 Design Pattern Cơ Bản (Pattern 01 → 07)

> Tham chiếu tổng quan: [`docs/bao-cao-tong-hop.md`](bao-cao-tong-hop.md)

---

## Pattern 01 — Chain of Responsibility (Chuỗi Trách Nhiệm)

### Bài toán

Khi người dùng đặt vé, hệ thống cần kiểm tra nhiều điều kiện: số lượng ghế hợp lệ, người dùng tồn tại, suất chiếu hợp lệ, ghế chưa bán. Ban đầu tất cả logic nằm trong một method `createBooking()` dài, khó mở rộng và vi phạm SRP.

### Giải pháp (Design Pattern)

Tách từng rule validation thành một handler độc lập, nối thành chuỗi. Handler nhận request → xử lý → chuyển sang handler tiếp theo.

```
MaxSeatsHandler → UserExistsHandler → ShowtimeExistsHandler → SeatsNotSoldHandler
```

### Cấu trúc lớp

| Lớp | Vai trò |
|-----|---------|
| `CheckoutValidationHandler` | Interface: `setNext()`, `handle()` |
| `AbstractCheckoutValidationHandler` | Abstract base: template method, gọi `doHandle()` → forward next |
| `MaxSeatsHandler` | Rule: 1 ≤ seatIds.size() ≤ 8 |
| `UserExistsHandler` | Rule: user tồn tại + là Customer |
| `ShowtimeExistsHandler` | Rule: showtimeId hợp lệ |
| `SeatsNotSoldHandler` | Rule: ghế chưa có vé |
| `CheckoutValidationConfig` | `@Configuration` — build chuỗi theo thứ tự |

**File:** `patterns/chainofresponsibility/`

### SOLID

| | Áp dụng |
|-|---------|
| S | Mỗi handler 1 rule duy nhất |
| O | Thêm rule mới = thêm class handler mới, không sửa class cũ |
| L | Mọi handler thay thế nhau qua interface |
| I | Interface chỉ 2 method hẹp |
| D | `CheckoutServiceImpl` phụ thuộc `CheckoutValidationHandler` (interface), không `new` handler |

### Test (manual — cần DB)

Chạy `POST /api/payment/checkout` với các trường hợp:

| Kịch bản | Request | Expected |
|----------|---------|----------|
| Happy path | userId hợp lệ, showtime hợp lệ, 2 ghế trống | Booking tạo thành công |
| Vượt số ghế | `seatIds` có 9 phần tử | `400 Bad Request` — "Số ghế vượt quá giới hạn" |
| User không tồn tại | `userId = 9999` | `400` — "Người dùng không tồn tại" |
| Ghế đã bán | seatId đã có ticket | `400` — "Ghế đã được bán" |

---

## Pattern 02 — Mediator (Trung Gian)

### Bài toán

Sau khi MoMo xác nhận thanh toán thành công, hệ thống cần thực hiện 5 tác vụ: cập nhật trạng thái booking, cập nhật chi tiêu user, tạo ticket, cập nhật trạng thái payment, gửi email. Nếu các service gọi lẫn nhau trực tiếp, coupling sẽ rất cao.

### Giải pháp (Design Pattern)

`PostPaymentMediator` đóng vai trò trung tâm điều phối. Mỗi tác vụ là một `PaymentColleague` độc lập — không biết nhau, chỉ biết Mediator.

```
MoMo Callback → PostPaymentMediator → [BookingStatusUpdater]
                                      [UserSpendingUpdater]
                                      [TicketIssuer]
                                      [PaymentStatusUpdater]
                                      [TicketEmailNotifier]
```

### Cấu trúc lớp

| Lớp | Vai trò |
|-----|---------|
| `PaymentColleague` | Interface: `onPaymentSuccess()`, `onPaymentFailure()` |
| `PostPaymentMediator` | `@Service` — gọi tất cả colleagues theo thứ tự |
| `BookingStatusUpdater` | Cập nhật Booking → `CONFIRMED` |
| `UserSpendingUpdater` | Cộng `totalSpending` cho Customer |
| `TicketIssuer` | Tạo Ticket row cho từng ghế |
| `PaymentStatusUpdater` | Cập nhật Payment → `COMPLETED` |
| `TicketEmailNotifier` | Gửi email vé qua EmailService |
| `MomoCallbackContext` | DTO truyền qua Mediator |

**File:** `patterns/mediator/`

### SOLID

| | Áp dụng |
|-|---------|
| S | Mỗi colleague 1 tác vụ duy nhất |
| O | Thêm tác vụ mới = thêm colleague mới, không sửa Mediator |
| I | Interface hẹp (2 method) |
| D | `CheckoutServiceImpl` phụ thuộc `PostPaymentMediator` (không gọi trực tiếp colleague) |

### Test (manual)

Trigger MoMo callback thành công → kiểm tra:
- Booking status = `CONFIRMED`
- Ticket rows tồn tại trong DB
- Email gửi (log hoặc Maildev)
- Payment status = `COMPLETED`

---

## Pattern 03 — Proxy (Uỷ Quyền Cache)

### Bài toán

`GET /api/movies` được gọi rất thường xuyên (trang chủ, tìm kiếm). Mỗi lần đều query DB → chậm, tốn tài nguyên. Cần cache kết quả mà **không sửa** `MovieServiceImpl`.

### Giải pháp (Design Pattern)

`CachingMovieServiceProxy` implements `MovieService`, inject `MovieServiceImpl` qua `@Qualifier`. Đánh dấu `@Primary` để Spring tự động inject proxy thay vì impl.

```
Controller → CachingMovieServiceProxy (@Primary)
                ├── Redis HIT → trả kết quả cached
                └── Redis MISS → MovieServiceImpl → cache → return
```

### Cấu trúc lớp

| Lớp | Vai trò |
|-----|---------|
| `MovieService` | Interface: `getAllMovies()`, `getMoviesByStatus()`, ... |
| `MovieServiceImpl` | Impl thật — query DB |
| `CachingMovieServiceProxy` | `@Primary @Service` — Redis cache; `@Qualifier("movieServiceImpl")` delegate |

**File:** `patterns/proxy/CachingMovieServiceProxy.java`

### Chi tiết cache

- Cache key `"movie:cache:all"` → TTL = `cinema.app.redisTtlSeconds` (default 600s)
- Cache key `"movie:cache:status:{STATUS}"` theo từng MovieStatus
- Invalidate toàn bộ khi create/update/delete movie

### SOLID

| | Áp dụng |
|-|---------|
| S | Proxy chỉ cache, không có business logic |
| L | `CachingMovieServiceProxy` thay thế `MovieServiceImpl` hoàn toàn (LSP) |
| D | Controller phụ thuộc `MovieService` (interface) |

### Test (manual)

```bash
# Gọi lần 1 — cache miss, query DB
GET /api/movies

# Gọi lần 2 — cache hit, Redis trả về (nhanh hơn rõ rệt)
GET /api/movies

# Kiểm tra Redis key
redis-cli GET "movie:cache:all"
```

---

## Pattern 04 — Specification (Điều Kiện Lọc)

### Bài toán

`GET /api/admin/showtimes?cinemaId=1&movieId=2&date=2026-04-30` cần lọc linh hoạt theo nhiều điều kiện có thể null. Nếu dùng nhiều `if` trong repository, code khó đọc và không mở rộng được.

### Giải pháp (Design Pattern)

`ShowtimeSpecifications` — final class với các static factory method, mỗi method trả về `Specification<Showtime>` (Spring Data JPA). Kết hợp bằng `.and()`.

```java
Specification.where(hasCinemaId(cinemaId))
             .and(hasMovieId(movieId))
             .and(onDate(date))
```

### Cấu trúc lớp

| Lớp | Vai trò |
|-----|---------|
| `ShowtimeSpecifications` | `final` class, private constructor, static factories |
| `hasCinemaId(Integer)` | Join Showtime → room → cinema |
| `hasMovieId(Integer)` | Join Showtime → movie |
| `onDate(LocalDate)` | startTime trong [date 00:00, date+1 00:00) |
| `ShowtimeRepository` | Extends `JpaSpecificationExecutor<Showtime>` |

**File:** `patterns/specification/ShowtimeSpecifications.java`

### SOLID

| | Áp dụng |
|-|---------|
| S | Mỗi static method 1 điều kiện |
| O | Thêm điều kiện mới = thêm static method, không sửa method cũ |
| I | `Specification<T>` là interface 1 method hẹp của Spring Data |

### Test (manual)

```bash
# Lọc theo cinema + ngày
GET /api/admin/showtimes?cinemaId=1&date=2026-04-30

# Lọc theo phim
GET /api/admin/showtimes?movieId=2

# Không có điều kiện → trả tất cả
GET /api/admin/showtimes
```

---

## Pattern 05 — Composite (Cây Tổng Hợp)

### Bài toán

Dashboard admin cần hiển thị số liệu từ nhiều nguồn: movie, user, showtime, F&B, voucher, doanh thu. Nếu gọi tuần tự trong controller, mỗi lần thêm metric phải sửa nhiều chỗ.

### Giải pháp (Design Pattern)

`DashboardStatsComposite` chứa danh sách `StatsComponent` (leaves). Mỗi leaf phụ trách 1 domain. Composite gọi `collect()` trên tất cả → gom kết quả vào 1 `Map`.

```
DashboardStatsComposite
├── MovieStatsLeaf       → movieCount, nowShowingCount
├── UserStatsLeaf        → totalUsers, newUsersThisMonth
├── ShowtimeStatsLeaf    → upcomingShowtimes
├── FnbStatsLeaf         → totalFnbRevenue
├── VoucherStatsLeaf     → activeVouchers
└── RevenueStatsLeaf     → totalRevenue, revenueThisMonth
```

### Cấu trúc lớp

| Lớp | Vai trò |
|-----|---------|
| `StatsComponent` | Interface: `void collect(Map<String, Object> target)` |
| `DashboardStatsComposite` | Composite — iterate leaves |
| `MovieStatsLeaf`, `UserStatsLeaf`, ... | Leaf — query 1 domain, put vào map |

**File:** `patterns/composite/`

### SOLID

| | Áp dụng |
|-|---------|
| S | Mỗi leaf 1 domain metric |
| O | Thêm metric mới = thêm leaf class mới + đăng ký vào composite |
| L | Leaf thay thế Composite đều qua `StatsComponent` |

### Test (manual)

```bash
GET /api/admin/dashboard
# Expected: JSON chứa tất cả key từ 6 leaves
```

---

## Pattern 06 — Singleton (Đơn Thể qua Spring)

### Bài toán

`MomoServiceImpl` cần `RestTemplate` để gọi MoMo API. Nếu mỗi service `new RestTemplate()` riêng → lãng phí, connection pool không được quản lý.

### Giải pháp (Design Pattern)

Spring IoC tự nhiên quản lý singleton — một `@Bean` = một instance duy nhất, inject vào mọi nơi cần.

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Ý nghĩa

- Đây là cách triển khai Singleton **đúng chuẩn** trong Spring — không dùng `private static instance`, không dùng `getInstance()` cổ điển
- Spring đảm bảo thread-safe và lifecycle management
- Tất cả service inject `RestTemplate` qua constructor — Dependency Inversion

**File:** `config/RestTemplateConfig.java`

### SOLID

| | Áp dụng |
|-|---------|
| S | Config class chỉ khai báo RestTemplate bean |
| D | Service inject `RestTemplate` (Spring inject) — không `new` trực tiếp |

---

## Pattern 07 — Prototype (Mẫu Sao Chép)

### Bài toán

Hệ thống gửi 3 loại email: xác nhận vé, chào mừng đăng ký, hoàn tiền. Mỗi loại có subject/body template khác nhau. Nếu tạo `SimpleMailMessage` từ đầu mỗi lần, code lặp và dễ nhầm template.

### Giải pháp (Design Pattern)

Mỗi prototype là `@Component` Spring bean — được Spring tạo một lần. Mỗi lần cần dùng: gọi `.copy()` để clone prototype, sau đó fill thông tin cụ thể (email, tên...) rồi gửi.

```java
// EmailServiceImpl
SimpleMailMessage msg = ticketEmailPrototype.copy()
    .setTo(email)
    .setBody("...${bookingCode}...")
    .toMessage();
mailSender.send(msg);
```

### Cấu trúc lớp

| Lớp | Vai trò |
|-----|---------|
| `EmailTemplate` | Interface: `copy()`, `toMessage()` |
| `TicketEmailPrototype` | Template xác nhận vé, `@Component` |
| `WelcomeEmailPrototype` | Template chào mừng, `@Component` |
| `RefundEmailPrototype` | Template hoàn tiền, `@Component` |

**File:** `patterns/prototype/`

### SOLID

| | Áp dụng |
|-|---------|
| S | Mỗi prototype 1 loại email |
| O | Thêm loại email mới = thêm class prototype, không sửa class cũ |
| L | Mọi prototype thay thế nhau qua `EmailTemplate` |
| I | Interface chỉ 2 method hẹp |
| D | `EmailServiceImpl` phụ thuộc `EmailTemplate` interface |

### Test (manual)

Sau khi checkout thành công → kiểm tra email nhận được (cần SMTP hoặc Maildev):
- Subject đúng template
- Thông tin booking (bookingCode, ghế) được điền đúng
- Gửi đến đúng email user

---

## Tổng Kết 7 Pattern

| # | Pattern | GoF Type | Vấn đề giải quyết | Lớp trung tâm |
|---|---------|----------|-------------------|---------------|
| 01 | Chain of Responsibility | Behavioral | Validate checkout | `CheckoutValidationConfig` |
| 02 | Mediator | Behavioral | Điều phối post-payment | `PostPaymentMediator` |
| 03 | Proxy | Structural | Cache movie list | `CachingMovieServiceProxy` |
| 04 | Specification | Behavioral | Lọc showtime | `ShowtimeSpecifications` |
| 05 | Composite | Structural | Dashboard stats | `DashboardStatsComposite` |
| 06 | Singleton | Creational | Shared RestTemplate | `RestTemplateConfig` |
| 07 | Prototype | Creational | Email templates | `EmailTemplate` + 3 impls |

> **Ghi chú test pattern 01-07:** Các pattern này gắn sâu với Spring context và database nên được test thủ công qua Postman/Swagger. Pattern 08 (Dynamic Pricing) có unit test tự động đầy đủ — xem [`docs/bao-cao-dynamic-pricing.md`](bao-cao-dynamic-pricing.md).
