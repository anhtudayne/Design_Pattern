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
| `MomoServiceImpl.java` | `services/impl/` | Inject và dùng `RestTemplate` |
| `ExternalApiService.java` | `services/` | Inject `RestTemplate` (nếu có HTTP call khác) |

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

```bash
# Kiểm tra app khởi động có bean không
curl http://localhost:8080/actuator/beans | grep restTemplate

# Kiểm tra thanh toán MoMo hoạt động (RestTemplate gọi được)
POST /api/payment/checkout
Body: { "showtimeId": 1, "seatIds": [1, 2] }
→ Response có orderId MoMo → RestTemplate Singleton hoạt động
```
