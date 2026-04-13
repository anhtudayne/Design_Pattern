# Báo Cáo Chi Tiết — Dynamic Pricing Engine (Pattern 08)

> Tham chiếu tổng quan: [`docs/bao-cao-tong-hop.md`](bao-cao-tong-hop.md)  
> UML đầy đủ: [`UML/08-dynamic-pricing-engine.md`](../UML/08-dynamic-pricing-engine.md)

---

## 1. Tổng Quan Tính Năng

### Bài toán

Hệ thống đặt vé cũ tính giá đơn giản: `basePrice + seatSurcharge`. Không có điều chỉnh theo:
- Ngày lễ / cuối tuần
- Đặt sớm (Early Bird)
- Tỷ lệ lấp đầy phòng
- Hạng thành viên (VIP/VVIP)
- Voucher kết hợp membership

### Giải pháp

Xây dựng **Dynamic Pricing Engine** — kết hợp **5 Design Pattern** để tính giá linh hoạt, dễ mở rộng, an toàn.

### Luồng xử lý

```
POST /api/booking/calculate
        │
        ▼
CachingDynamicPricingProxy (Proxy — check Redis cache)
        │ cache MISS
        ▼
DynamicPricingServiceImpl
        │
        ├─① PricingContextFactory (load DB → build PricingContext)
        │
        ├─② PricingStrategySelector (Specification → chọn Strategy)
        │       PricingConditions.isEarlyBird? → EarlyBirdStrategy (-10%)
        │       PricingConditions.isHoliday?   → HolidayStrategy (+20%)
        │       PricingConditions.isWeekend?   → WeekendStrategy (+15%)
        │       else                           → StandardStrategy
        │
        ├─③ PriceCalculatorChainFactory (Decorator chain)
        │       BasePriceCalculator      → ticketTotal = Σ(strategy(basePrice) + surcharge)
        │       OccupancyDecorator       → +10% nếu > 80% lấp đầy
        │       FnbDecorator             → +fnbTotal
        │       MemberDiscountDecorator  → -tier.discountPercent%
        │       VoucherDecorator         → -promotion (FIXED/PERCENT)
        │
        ├─④ PriceValidationChain (CoR — validate kết quả)
        │       MinPriceHandler          → finalTotal ≥ 0
        │       MaxDiscountHandler       → discount ≤ 50% subtotal
        │       FraudDetectionHandler    → giá/ghế ≥ 10,000 VND
        │
        └─⑤ Cache kết quả → Redis (TTL 300s)
                │
                ▼
        PriceBreakdownDTO (trả về client)
```

---

## 2. Năm Pattern Trong Engine

### 2.1 Specification Pattern — `PricingConditions`

**Mục đích:** Đóng gói điều kiện kinh doanh thành predicate thuần, tái sử dụng được.

**File:** `patterns/specification/PricingConditions.java`

```java
public final class PricingConditions {
    public static Predicate<PricingContext> isWeekend()             { ... }
    public static Predicate<PricingContext> isHoliday()             { ... }
    public static Predicate<PricingContext> isEarlyBird()           { ... }
    public static Predicate<PricingContext> isHighOccupancy(int %)  { ... }
}
```

**Ngày lễ Việt Nam được định nghĩa sẵn:**
- 1/1 (Tết Dương lịch), 30/4 (Giải phóng miền Nam), 1/5 (Quốc tế Lao động), 2/9 (Quốc khánh)

---

### 2.2 Strategy Pattern — 4 chiến lược giá

**Mục đích:** Mỗi chiến lược đóng gói một công thức tính giá cơ sở, thay thế nhau linh hoạt.

**File:** `patterns/pricing/strategy/`

| Strategy | `priority()` | `isApplicable()` | Công thức |
|----------|-------------|------------------|-----------|
| `EarlyBirdPricingStrategy` | 10 (cao nhất) | Đặt trước ≥ 3 ngày | `basePrice × 0.90` |
| `HolidayPricingStrategy` | 20 | Ngày lễ | `basePrice × 1.20` |
| `WeekendPricingStrategy` | 30 | Thứ 7 / Chủ nhật | `basePrice × 1.15` |
| `StandardPricingStrategy` | 999 (fallback) | Luôn true | `basePrice` |

**`PricingStrategySelector`** — inject `List<PricingStrategy>` từ Spring, sort theo `priority()`, chọn strategy đầu tiên `isApplicable()`. **Không có if-else** — thêm strategy mới không cần sửa Selector (OCP).

**Config trong `application.yml`** (không hardcode):
```yaml
cinema:
  pricing:
    weekend-surcharge-pct: 15
    holiday-surcharge-pct: 20
    early-bird-discount-pct: 10
    early-bird-days: 3
```

---

### 2.3 Decorator Pattern — 4 lớp modifier

**Mục đích:** Xếp chồng các khoản tiền độc lập lên kết quả của Strategy.

**File:** `patterns/pricing/decorator/`

**GoF roles:**

| Role | Class |
|------|-------|
| Component interface | `PriceCalculator` — `PricingAccumulator calculate(ctx)` |
| **Concrete Component** | `BasePriceCalculator` — tạo Accumulator, tính ticketTotal |
| Abstract Decorator | `AbstractPriceCalculatorDecorator` — giữ `inner`, template `decorate()` |
| Concrete Decorators | 4 POJO bên dưới |

**Thứ tự chain:**
```
BasePriceCalculator
  └── OccupancyDecorator      (+10% nếu lấp đầy > 80%)
        └── FnbDecorator      (+fnbTotal từ PricingContext)
              └── MemberDiscountDecorator  (-tier.discountPercent% trên ticketTotal)
                    └── VoucherDecorator  (-promotion FIXED/PERCENT, clamp ≥ 0)
```

**`PricingAccumulator`** — mutable object truyền qua chain, tránh rebuild DTO nhiều lần:
```
ticketTotal → occupancySurcharge → fnbTotal → membershipDiscount → voucherDiscount
                                                           → finalTotal() → toDTO()
```

---

### 2.4 Chain of Responsibility — Price Validation

**Mục đích:** Validate kết quả giá sau khi tính xong, trước khi cache và trả về.

**File:** `patterns/pricing/validation/`

| Handler | Rule | Throw khi |
|---------|------|-----------|
| `MinPriceHandler` | Giá không âm | `finalTotal < 0` |
| `MaxDiscountHandler` | Giảm tối đa 50% | `discount/subtotal > 0.50` |
| `FraudDetectionHandler` | Giá/ghế tối thiểu | `finalTotal/seatCount < 10,000 VND` |

Chuỗi: `MinPrice → MaxDiscount → FraudDetection`

---

### 2.5 Proxy Pattern — Redis Cache

**Mục đích:** Cache kết quả tính giá 5 phút, tránh tính lại cho cùng một yêu cầu.

**File:** `patterns/proxy/CachingDynamicPricingProxy.java`

```java
@Primary @Service
public class CachingDynamicPricingProxy implements DynamicPricingService {
    // Cache key: pricing:{showtimeId}:[sortedSeatIds]:{userId}:{promoCode}
    // TTL: 300 giây
}
```

**Tại sao có userId trong key:** User VIP và anonymous có discount khác nhau → phải cache riêng.

---

## 3. Kết Quả Unit Test — 67 Test PASS

### 3.1 Chạy test

```bash
cd backend
./mvnw test -Dtest="PricingConditionsTest,PricingStrategyTest,DecoratorTest,PriceValidationChainTest,CachingDynamicPricingProxyTest,DynamicPricingServiceImplTest"
```

**Output thực tế:**
```
Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS — Total time: 5.960 s
```

### 3.2 Chi tiết từng test class

#### `PricingConditionsTest` — 9 test

| # | Test | Kết quả |
|---|------|---------|
| 1 | isWeekend: Thứ 7 (2026-04-11) → true | ✅ PASS |
| 2 | isWeekend: Thứ 2 (2026-04-13) → false | ✅ PASS |
| 3 | isHoliday: 30/4 → true | ✅ PASS |
| 4 | isHoliday: 15/6 → false | ✅ PASS |
| 5 | isEarlyBird: đặt trước 4 ngày → true | ✅ PASS |
| 6 | isEarlyBird: đặt trước 1 ngày → false | ✅ PASS |
| 7 | isHighOccupancy(80): 85/100 → true | ✅ PASS |
| 8 | isHighOccupancy(80): 70/100 → false | ✅ PASS |
| 9 | isHighOccupancy: total=0 → false (guard chia 0) | ✅ PASS |

#### `PricingStrategyTest` — 21 test (4 Nested class)

**EarlyBirdPricingStrategy (4 test):**

| # | Test | Kết quả |
|---|------|---------|
| 1 | isApplicable: đặt 10 ngày trước → true | ✅ PASS |
| 2 | isApplicable: đặt 1 ngày trước → false | ✅ PASS |
| 3 | adjustBasePrice: 100,000 × 0.90 = 90,000 | ✅ PASS |
| 4 | priority() = 10 | ✅ PASS |

**HolidayPricingStrategy (4 test):**

| # | Test | Kết quả |
|---|------|---------|
| 1 | isApplicable: 30/4 → true | ✅ PASS |
| 2 | isApplicable: 15/6 → false | ✅ PASS |
| 3 | adjustBasePrice: 100,000 × 1.20 = 120,000 | ✅ PASS |
| 4 | priority() = 20 | ✅ PASS |

**WeekendPricingStrategy (4 test):**

| # | Test | Kết quả |
|---|------|---------|
| 1 | isApplicable: Thứ 7 → true | ✅ PASS |
| 2 | isApplicable: Thứ 3 → false | ✅ PASS |
| 3 | adjustBasePrice: 100,000 × 1.15 = 115,000 | ✅ PASS |
| 4 | priority() = 30 | ✅ PASS |

**StandardPricingStrategy (3 test):**

| # | Test | Kết quả |
|---|------|---------|
| 1 | isApplicable: luôn true (fallback) | ✅ PASS |
| 2 | adjustBasePrice: trả nguyên basePrice | ✅ PASS |
| 3 | priority() = 999 | ✅ PASS |

**PricingStrategySelector (6 test):**

| # | Test | Expected | Kết quả |
|---|------|----------|---------|
| 1 | 30/4 + đặt 4 ngày trước | EARLY_BIRD (priority 10 thắng 20) | ✅ PASS |
| 2 | 30/4 + đặt 1 ngày trước | HOLIDAY | ✅ PASS |
| 3 | Thứ 7 + đặt 1 ngày trước | WEEKEND | ✅ PASS |
| 4 | Thứ 3 + đặt 1 ngày trước | STANDARD | ✅ PASS |
| 5 | List chỉ có [Standard] | STANDARD | ✅ PASS |
| 6 | List rỗng | throw IllegalStateException | ✅ PASS |

#### `DecoratorTest` — 9 test

**basePrice = 100,000 VND, 2 ghế standard → ticketTotal = 200,000 VND**

| # | Decorator | Test | Expected | Kết quả |
|---|-----------|------|----------|---------|
| 1 | Occupancy | occupancy 90% | surcharge = 200,000 × 10% = 20,000 | ✅ PASS |
| 2 | Occupancy | occupancy 70% | surcharge = 0 | ✅ PASS |
| 3 | Fnb | fnbTotal = 150,000 | acc.fnbTotal = 150,000 | ✅ PASS |
| 4 | Member | VIP tier 10% | discount = 200,000 × 10% = 20,000 | ✅ PASS |
| 5 | Member | anonymous | discount = 0 | ✅ PASS |
| 6 | Voucher | FIXED -50,000 | voucherDiscount = 50,000 | ✅ PASS |
| 7 | Voucher | PERCENT 20% | voucherDiscount = subtotal × 20% = 40,000 | ✅ PASS |
| 8 | Voucher | hết hạn | voucherDiscount = 0 | ✅ PASS |
| 9 | Full chain | ticket+occupancy+fnb-member-voucher | finalTotal = 230,000 | ✅ PASS |

**Ví dụ Full chain (test #9):**
```
ticketTotal        = 200,000 (2 ghế × 100,000)
occupancySurcharge = 20,000  (90% lấp đầy × 10%)
fnbTotal           = 50,000
─────────────────────────────
subtotal           = 270,000
membershipDiscount = 20,000  (VIP 10% trên ticketTotal)
voucherDiscount    = 20,000  (FIXED)
─────────────────────────────
finalTotal         = 230,000  ✅
```

#### `PriceValidationChainTest` — 10 test

| # | Handler | Test | Expected | Kết quả |
|---|---------|------|----------|---------|
| 1 | MinPrice | finalTotal = -100 | throw | ✅ PASS |
| 2 | MinPrice | finalTotal = 0 (boundary) | pass | ✅ PASS |
| 3 | MaxDiscount | discount/subtotal = 51% | throw | ✅ PASS |
| 4 | MaxDiscount | discount/subtotal = 50% (boundary) | pass | ✅ PASS |
| 5 | MaxDiscount | subtotal = 0 (guard) | pass | ✅ PASS |
| 6 | FraudDetection | 9,999 VND/ghế | throw | ✅ PASS |
| 7 | FraudDetection | 10,000 VND/ghế (boundary) | pass | ✅ PASS |
| 8 | FraudDetection | seatCount = 0 (guard) | pass | ✅ PASS |
| 9 | Full chain | happy path | pass hết | ✅ PASS |
| 10 | Full chain | discount 60% | throw tại MaxDiscount | ✅ PASS |

#### `CachingDynamicPricingProxyTest` — 9 test

| # | Test | Kết quả |
|---|------|---------|
| 1-5 | Cache key variants (showtimeId, seatIds sorted, userId, promoCode) | ✅ ALL PASS |
| 6-7 | Cache HIT — delegate không được gọi | ✅ PASS |
| 8 | Cache MISS — gọi delegate, lưu cache | ✅ PASS |
| 9 | Exception từ delegate — không được cache | ✅ PASS |

#### `DynamicPricingServiceImplTest` — 9 test (Integration, không cần DB)

*Dùng anonymous subclass stub `PricingContextFactory`, real chain + validation*

| # | Kịch bản | appliedStrategy | finalTotal | Kết quả |
|---|----------|-----------------|-----------|---------|
| 1 | Thứ 2, anonymous, không promo | STANDARD | 200,000 | ✅ PASS |
| 2 | Thứ 7, booking cùng ngày | WEEKEND | 230,000 | ✅ PASS |
| 3 | 30/4, booking cùng ngày | HOLIDAY | 240,000 | ✅ PASS |
| 4 | Thứ 3, đặt trước 4 ngày | EARLY_BIRD | 180,000 | ✅ PASS |
| 5 | Thứ 7, đặt trước 4 ngày | EARLY_BIRD (ưu tiên hơn WEEKEND) | 180,000 | ✅ PASS |
| 6 | Occupancy 90% | STANDARD | 220,000 (+20k surcharge) | ✅ PASS |
| 7 | VIP tier 10% | STANDARD | 180,000 (-20k discount) | ✅ PASS |
| 8 | Voucher FIXED -50,000 | STANDARD | 150,000 | ✅ PASS |
| 9 | Voucher PERCENT 80% | — | throw IllegalStateException | ✅ PASS |

---

## 4. So Sánh Trước / Sau

| | Trước (cũ) | Sau (Dynamic Pricing Engine) |
|-|-----------|------------------------------|
| Logic giá | 1 method 50 dòng trong `BookingServiceImpl` | 5 pattern, ~20 class, mỗi class < 30 dòng |
| Thêm rule giá mới | Sửa method cũ (OCP vi phạm) | Thêm class mới, không sửa class cũ |
| Membership discount | Chưa có | `MemberDiscountDecorator` áp dụng |
| Ngày lễ/cuối tuần | Chưa có | `HolidayStrategy`, `WeekendStrategy` |
| Cache | Không có | Redis 5 phút |
| Validation | Chỉ clamp >= 0 | 3 rule: min price, max discount, fraud detection |
| Test tự động | Không có | 67 unit test, 100% pass |

---

## 5. API Response Mới

```json
POST /api/booking/calculate
{
  "showtimeId": 5,
  "seatIds": [12, 13],
  "fnbs": [{"itemId": 1, "quantity": 2}],
  "promoCode": "SALE20"
}

Response 200 OK:
{
  "ticketTotal": 230000,
  "occupancySurcharge": 20000,
  "fnbTotal": 40000,
  "membershipDiscount": 20000,
  "discountAmount": 58000,
  "finalTotal": 212000,
  "appliedStrategy": "WEEKEND"
}
```

> Các field cũ (`ticketTotal`, `fnbTotal`, `discountAmount`, `finalTotal`) được giữ nguyên — **backward compatible** với frontend đang dùng.

---

## 6. Điểm Nổi Bật Kỹ Thuật

### OCP triệt để trong Strategy Selector

```java
// PricingStrategySelector — KHÔNG có if-else
public PricingStrategy select(PricingContext ctx) {
    return sortedStrategies.stream()          // List inject từ Spring
        .filter(s -> s.isApplicable(ctx))
        .findFirst()
        .orElseThrow(...);
}
```

Để thêm `FlashSaleStrategy`: tạo 1 class `@Component`, đặt `priority() = 5` — Selector tự động nhận vì Spring inject `List<PricingStrategy>`.

### PricingAccumulator — Thread-safe qua design

Mỗi request tạo một `PricingAccumulator` mới (không share state giữa các request). Factory `buildChain()` tạo chain mới mỗi lần → hoàn toàn thread-safe dù service là singleton.

### Validation trước cache

```
Exception từ DynamicPricingServiceImpl → KHÔNG được cache bởi Proxy
```

Đây là điểm quan trọng: nếu giá tính ra vi phạm rule (fraud, discount quá lớn), exception propagate ra ngoài và không được cache. Request tiếp theo sẽ tính lại — đúng hành vi.
