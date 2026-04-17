# Báo Cáo Chi Tiết — Dynamic Pricing Engine (Pattern 08)

> Tham chiếu tổng quan: [`docs/bao-cao-tong-hop.md`](bao-cao-tong-hop.md)  
> UML đầy đủ: [`UML/08-dynamic-pricing-engine.md`](../UML/08-dynamic-pricing-engine.md)

---

## 1. Tổng Quan Tính Năng

### Bài toán

Hệ thống đặt vé cũ tính giá đơn giản và không có validation tách biệt. Vấn đề:
- Logic validate và tính giá lẫn lộn trong cùng 1 method
- Không cache — tính lại mỗi lần user preview giá
- Không có surcharge theo ngày / hạng thành viên
- Không có promotion validation đúng tầng

### Giải pháp: 5 GoF Pattern Production-Grade

```
POST /api/booking/calculate
        │
        ▼ BookingServiceImpl.calculatePrice()
        │
        ├─[1. Chain of Responsibility]─►
        │    ShowtimeFutureHandler → SeatsAvailableHandler → PromoValidHandler
        │    (validate + populate showtime, promotion; builder nối thêm ghế/F&B/occupancy)
        │
        ├─[1b. PricingContextBuilder]─► build(validationCtx, request) → PricingContext
        │
        ├─[2. Proxy]─► CachingPricingEngineProxy
        │    └─ cache hit → return từ Redis (key: showtime+seats+F&B+promo+customer)
        │    └─ cache miss → delegate xuống PricingEngine
        │
        └─[3-4-5. Engine: Strategy + Decorator + Specification]─►
             ├─ TicketPricingStrategy     → ticketTotal
             ├─ FnbPricingStrategy        → fnbTotal
             ├─ TimeBasedPricingStrategy  → timeBasedSurcharge (via PricingConditions)
             └─ Decorator chain           → discountAmount + membershipDiscount
                  NoDiscount → PromotionDiscountDecorator → MemberDiscountDecorator
```

---

## 2. Năm Pattern Trong Engine

### 2.1 Chain of Responsibility — Pricing Validation

**File:** `services/strategy_decorator/pricing/validation/`

**Chain:** `ShowtimeFutureHandler → SeatsAvailableHandler → PromoValidHandler`

| Handler | Validate | Error message |
|---------|----------|---------------|
| `ShowtimeFutureHandler` | Showtime tồn tại + chưa chiếu | "Suất chiếu đã kết thúc..." |
| `SeatsAvailableHandler` | Ghế chưa bán | "Ghế ID X đã được bán..." |
| `PromoValidHandler` | Promo tồn tại + còn hạn + còn quantity | "Mã... đã hết hạn/lượt" |

**Lợi ích kép:** Validate fail sớm (fail-fast) + populate `showtime` và `promotion` vào context để `BookingServiceImpl` tái dùng, không load lại DB.

---

### 2.2 Proxy — CachingPricingEngineProxy

**File:** `services/strategy_decorator/pricing/CachingPricingEngineProxy.java`

**Cache key:** `pricing:{showtimeId}:seats:{sortedIds}:fnb:{sorted itemId:qty}|fnb:none:promo:{code}:cust:{userId}`

**TTL:** 600s (configurable qua `cinema.app.redisTtlSeconds`)

**Cơ chế:** `@Primary` + `IPricingEngine` interface — Spring inject proxy vào tất cả caller tự động.

```java
Object cached = redisTemplate.opsForValue().get(cacheKey);
if (cached instanceof PriceBreakdownDTO dto) return dto;   // cache hit

PriceBreakdownDTO result = delegate.calculateTotalPrice(context);
redisTemplate.opsForValue().set(cacheKey, result, ttlSeconds, SECONDS);
return result;  // cache miss → delegate
```

---

### 2.3 Specification — PricingConditions

**File:** `patterns/specification/PricingConditions.java`

Predicate factory tái sử dụng — tích hợp thực sự qua `TimeBasedPricingStrategy`:

```java
boolean isHolidayDay = PricingConditions.isHoliday().test(specCtx);
boolean isWeekendDay = PricingConditions.isWeekend().test(specCtx);
BigDecimal rate = isHolidayDay ? holidaySurchargePct : weekendSurchargePct;
```

---

### 2.4 Strategy — Ticket / Fnb / TimeBased

| Strategy | Công thức | Rate (application.properties) |
|----------|-----------|-------------------------------|
| `TicketPricingStrategy` | `Σ(basePrice + seatSurcharge)` | — |
| `FnbPricingStrategy` | `Σ(resolvedItem.price × qty)` | — |
| `TimeBasedPricingStrategy` | `ticketSubtotal × rate%` | weekend: 15%, holiday: 20% |

---

### 2.5 Decorator — DiscountComponent Chain

Chain build tại runtime trong `PricingEngine.buildDiscountChain()`:

```
NoDiscount (Spring @Component base)
  └── PromotionDiscountDecorator (if promotion != null)
      └── MemberDiscountDecorator (if customer.tier.discountPercent > 0)
```

**Validation tách biệt:** `PromoValidHandler` (CoR) validate trước. Decorator chỉ tính discount thuần.

**Thứ tự áp dụng (code hiện tại):** giảm theo promo tính trên `subtotal`; giảm theo hạng thành viên tính trên phần còn lại sau promo. `discountAmount` là tổng hai khoản; `membershipDiscount` trong response phản ánh mức giảm từ tier.

---

## 3. Kết Quả Unit Test — 9 Test PASS

```bash
cd backend && ./mvnw test -Dtest="PricingConditionsTest"
# Tests run: 9, Failures: 0, Errors: 0 — BUILD SUCCESS
```

| # | Test | Kết quả |
|---|------|---------|
| 1-2 | isWeekend: Thứ 7 → true, Thứ 2 → false | ✅ PASS |
| 3-4 | isHoliday: 30/4 → true, 15/6 → false | ✅ PASS |
| 5-6 | isEarlyBird: 4 ngày → true, 1 ngày → false | ✅ PASS |
| 7-8 | isHighOccupancy(80): 85% → true, 70% → false | ✅ PASS |
| 9 | isHighOccupancy: total=0 → false (guard) | ✅ PASS |

---

## 4. So Sánh Trước / Sau (Full Refactor)

| | Trước (MVP) | Sau (5-pattern production) |
|-|------------|---------------------------|
| Validation | Lẫn lộn trong calculatePrice() | CoR chain tách biệt hoàn toàn |
| Cache | Không có | CachingPricingEngineProxy (Redis) |
| Promo validation | Trong Decorator (sai tầng) | PromoValidHandler (CoR) |
| Membership discount | Không có | MemberDiscountDecorator |
| Time-based surcharge | Không có | TimeBasedPricingStrategy (15%/20%) |
| PriceBreakdownDTO | 4 trường | 7 trường đầy đủ |
| FnbPricingStrategy | Gọi DB trực tiếp | Đọc ResolvedFnbItem (no DB) |
| Proxy interface | Không có | IPricingEngine — OCP compliant |

---

## 5. API Response

```json
POST /api/booking/calculate
{ "showtimeId": 5, "seatIds": [12, 13], "fnbs": [{"itemId": 1, "quantity": 2}], "promoCode": "SALE20" }

Response 200 OK:
{
  "ticketTotal": 200000,
  "fnbTotal": 40000,
  "timeBasedSurcharge": 30000,
  "membershipDiscount": 13500,
  "discountAmount": 53500,
  "appliedStrategy": "TICKET+FNB+TIME_BASED+MEMBER_DISCOUNT+PROMO",
  "finalTotal": 216500
}
```

---

## 6. Điểm Nổi Bật Kỹ Thuật

### Fail-fast validation + data reuse

CoR chain vừa validate (ném exception sớm) vừa populate `showtime` và `promotion` vào `PricingValidationContext` — `BookingServiceImpl` tái dùng thay vì gọi DB lần thứ 2.

### Transparent proxy qua @Primary + interface

`@Primary` trên `CachingPricingEngineProxy` + `IPricingEngine` interface: mọi caller (BookingServiceImpl) nhận proxy tự động. Không cần thay đổi caller code khi thêm/bỏ proxy.

### Config-driven surcharge rates

Tất cả rate đều đọc từ `application.properties`:
```properties
cinema.pricing.weekend-surcharge-pct=15
cinema.pricing.holiday-surcharge-pct=20
```
Thay đổi rate không cần recompile.
