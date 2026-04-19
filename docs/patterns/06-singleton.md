# Pattern 06 — Singleton (Đơn Bản)

## 1. Lý thuyết

**Singleton** thuộc nhóm Creational. Đảm bảo **chỉ tồn tại DUY NHẤT MỘT instance** của một class trong suốt vòng đời ứng dụng, và cung cấp điểm truy cập toàn cục đến instance đó.

Trong Spring: mọi `@Component`, `@Service`, `@Bean` đều là **Singleton by default** — IoC Container khởi tạo 1 lần, mọi nơi inject đều dùng chung instance đó.

---

## 2. Vấn đề (Trước khi áp dụng)

Khi thanh toán MoMo, hệ thống phải gọi REST API ra ngoài. Nếu tạo `RestTemplate` mỗi lần gọi:

```java
// ❌ Trước — new RestTemplate() mỗi request
@Service
public class MomoServiceImpl {

    public MomoPaymentResponse createPayment(MomoPaymentRequest request) {
        RestTemplate restTemplate = new RestTemplate();  // tạo mới mỗi lần!
        return restTemplate.postForObject(momoUrl, request, MomoPaymentResponse.class);
    }

    public boolean verifyCallback(MomoCallbackRequest request) {
        RestTemplate restTemplate = new RestTemplate();  // tạo mới lần 2!
        // ...
    }
}
```

**Hậu quả:**
- Mỗi `new RestTemplate()` → tốn bộ nhớ, tạo connection pool mới → **overhead không cần thiết**
- Không thể tập trung cấu hình timeout, TLS, retry ở một chỗ
- Mỗi class tự cấu hình riêng → inconsistent, khó maintain
- Không thread-safe nếu cấu hình sau khi khởi tạo

---

## 3. Giải pháp — Spring Singleton Bean

Khai báo `RestTemplate` là `@Bean` trong `@Configuration`. Spring quản lý 1 instance duy nhất — mọi service inject đều dùng chung.

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `RestTemplateConfig.java` | `config/` | **@Configuration** — khai báo Singleton bean |
| `MomoServiceImpl.java` | `service/impl/` | Inject và dùng `RestTemplate` (gọi API MoMo) |

---

## 5. Code thực tế

### `RestTemplateConfig.java` (toàn bộ)

```java
/**
 * Singleton pattern via Spring IoC — provides a single shared RestTemplate bean.
 * All callers inject this bean; no one calls new RestTemplate() directly.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
        // Có thể mở rộng: timeout, interceptor, TLS config tập trung tại đây
    }
}
```

### Service inject Singleton

```java
// ✅ Sau — inject 1 lần, dùng mãi
@Service
@RequiredArgsConstructor
public class MomoServiceImpl implements MomoService {

    private final RestTemplate restTemplate;  // Spring inject Singleton

    @Value("${momo.endpoint}")
    private String momoUrl;

    @Override
    public MomoPaymentResponse createPayment(MomoPaymentRequest request) {
        return restTemplate.postForObject(momoUrl, request, MomoPaymentResponse.class);
    }

    @Override
    public boolean verifyCallback(MomoCallbackRequest request) {
        // dùng cùng restTemplate instance
        String hmac = computeHmacSHA256(request.getRawSignature());
        return hmac.equals(request.getSignature());
    }
}
```

---

## 6. Cơ chế Spring Singleton

```
Spring IoC Container khởi động
        │
        ▼
Quét @Configuration → RestTemplateConfig
        │
        ├── Gọi restTemplate() 1 LẦN DUY NHẤT
        │── Lưu instance vào ApplicationContext
        │
        ▼
MomoServiceImpl cần RestTemplate
        └── Spring inject CÙNG instance đã tạo (không new lại)

MomoController cần RestTemplate
        └── Spring inject CÙNG instance đó
```

---

## 7. Xác minh Singleton

```java
// Demo: 2 service inject RestTemplate — cùng 1 object
@Autowired MomoServiceImpl momoService;
@Autowired AnotherService anotherService;

System.out.println(momoService.getRestTemplate() == anotherService.getRestTemplate());
// Output: true  ← cùng 1 instance
```

---

## 8. Tại sao không dùng Singleton "cổ điển" (double-checked locking)?

```java
// ❌ Cách cũ — boilerplate, nguy cơ race condition
public class RestTemplateHolder {
    private static volatile RestTemplate INSTANCE;
    public static RestTemplate getInstance() {
        if (INSTANCE == null) {
            synchronized (RestTemplateHolder.class) {
                if (INSTANCE == null) INSTANCE = new RestTemplate();
            }
        }
        return INSTANCE;
    }
}
```

Spring `@Bean` đơn giản hơn, thread-safe hơn, dễ test hơn (có thể override bean trong test context).

---

## 9. SOLID

| | Chi tiết |
|-|---------|
| **S** | `RestTemplateConfig` chỉ khai báo HTTP client bean |
| **O** | Muốn thêm retry/timeout → tạo `@Bean` mới với `@Qualifier`, không sửa class cũ |
| **D** | `MomoServiceImpl` inject `RestTemplate` (class Spring quản lý), không `new` trực tiếp |

---

## 10. Thành quả

| Trước | Sau |
|-------|-----|
| `new RestTemplate()` mỗi request | 1 instance dùng chung toàn app |
| Cấu hình timeout rải rác mỗi nơi | Tập trung 1 chỗ trong `RestTemplateConfig` |
| Khó test (mock RestTemplate) | Override bean trong test context dễ dàng |
| Tốn bộ nhớ, GC pressure | Tái sử dụng connection pool hiệu quả |

---

## 11. Test thủ công

**Không** dán cả khối “mô tả HTTP” (`POST ...`, `Body: { ... }`, mũi tên `→`) vào terminal. Đó không phải lệnh shell; zsh sẽ báo lỗi (thường `parse error near '}'` vì ký tự `{`).

**Actuator:** dự án backend **chưa** khai báo `spring-boot-starter-actuator`, nên **`/actuator/beans` không có**. Muốn xem bean trên HTTP thì thêm dependency + bật endpoint trong cấu hình; còn không thì xác minh Singleton `RestTemplate` qua IDE (Spring Boot dashboard) hoặc breakpoint trong `MomoServiceImpl`.

**Checkout MoMo** (`MomoServiceImpl` + bean `RestTemplate`): `POST /api/payment/checkout` cần **JWT**, body đúng `CheckoutRequestDTO`. Mặc định **`momo.dev-skip-external=false`** — gọi sandbox MoMo (trừ khi trong `.env` có `MOMO_DEV_SKIP_EXTERNAL=true`). Khi skip bật: **không** POST ra ngoài, trả `payUrl` dạng `about:blank#momo-dev-skip-...` (chỉ để test luồng đặt vé không cần cổng). Gặp **403** từ MoMo: tạm đặt `MOMO_DEV_SKIP_EXTERNAL=true` hoặc whitelist IP / kiểm tra key trên portal sandbox. Cấu hình: **`.env`** ở root repo (khuyến nghị) hoặc `backend/.env` — Spring import cả hai đường. `DEV_*` MoMo tham chiếu [momo-wallet/java `environment.properties`](https://github.com/momo-wallet/java/blob/master/src/main/resources/environment.properties); `DEV_MOMO_ENDPOINT` = **URL đầy đủ** `.../api/create`. Thiếu biến: `application.properties` có default local cho DB/Redis/MoMo sandbox.

Ví dụ **copy được** (seed `data.sql`: khách `user1@starcine.local` / `123456` → `userId` = **3**):

```bash
# 1) Lấy JWT
TOKEN=$(curl -sS -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@starcine.local","password":"123456"}' | jq -r .token)

# 2) Checkout MoMo (ghế 1,2 — suất 1; chỉnh nếu DB của bạn khác)
curl -sS -X POST http://localhost:8080/api/payment/checkout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"userId":3,"showtimeId":1,"seatIds":[1,2],"paymentMethod":"MOMO"}'
```

Không có `jq` thì làm hai bước tay: gọi login, copy giá trị `token` (chỉ chuỗi JWT, không kèm `","type"`…), gán `TOKEN='...'` rồi chạy lệnh `curl` ở bước 2.

**Nếu 400 *Suất chiếu đã kết thúc*:** `ShowtimeFutureHandler` chỉ cho đặt khi `start_time` của suất chưa qua so với giờ máy chủ. Cập nhật `showtimes` trong DB hoặc chạy lại seed (`data.sql` dùng suất năm 2028).

**Lưu ý:** Nếu **502** *Cổng MoMo trả về 403* — IP/key/endpoint sandbox chưa đúng; có thể đặt `MOMO_DEV_SKIP_EXTERNAL=true` trong `.env` để dùng payUrl giả tạm thời.
