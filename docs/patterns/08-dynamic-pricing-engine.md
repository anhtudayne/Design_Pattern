# Tài Liệu Kỹ Thuật: Dynamic Pricing Engine

> **Tổng quan**: Dynamic Pricing Engine là tính năng tính giá vé thông minh tích hợp **5 GoF Design Pattern** (Specification, Strategy, Decorator, Chain of Responsibility, Proxy) theo kiến trúc phân tầng, đảm bảo giá vé được tính chính xác, có thể mở rộng và hiệu quả cao nhờ Redis cache.

---

## Mục lục

1. [Tổng quan & Kiến trúc tổng thể](#1-tổng-quan--kiến-trúc-tổng-thể)
2. [Danh sách file trong dự án](#2-danh-sách-file-trong-dự-án)
3. [Phase 0 — Specification Pattern: PricingConditions](#3-phase-0--specification-pattern-pricingconditions)
4. [Phase 1 — Strategy Pattern](#4-phase-1--strategy-pattern)
5. [Phase 2 — Decorator Pattern](#5-phase-2--decorator-pattern)
6. [Phase 3 — Chain of Responsibility: Price Validation](#6-phase-3--chain-of-responsibility-price-validation)
7. [Phase 4 — Service Orchestration](#7-phase-4--service-orchestration)
8. [Phase 5 — Proxy Cache](#8-phase-5--proxy-cache)
9. [SOLID & Clean Code Summary](#9-solid--clean-code-summary)
10. [Class Diagram Tổng hợp](#10-class-diagram-tổng-hợp)

---

## 1. Tổng quan & Kiến trúc tổng thể

### Bài toán trước khi có Dynamic Pricing Engine

Trước đây, toàn bộ logic tính giá vé nằm chen lẫn trong `BookingServiceImpl.calculateTotalPrice()` — một phương thức "God Method" dài hơn 50 dòng, trực tiếp gọi repository, hard-code logic khuyến mãi, và không có validation. Hậu quả:

- **Không mở rộng được**: Thêm loại giá mới (VD: giá VIP, giá Flash Sale) phải sửa thẳng vào service.
- **Không tái sử dụng được**: Logic tính giá bị gắn chặt với luồng booking, không thể gọi độc lập.
- **Không testable**: Khó unit test vì phụ thuộc trực tiếp nhiều repository.
- **Không cache được**: Mỗi request đều phải tính lại từ đầu, kể cả khi input giống nhau.

### Kiến trúc phân tầng mới

```
  BookingServiceImpl
        │
        ▼ (inject qua @Primary)
  CachingDynamicPricingProxy   ←── Redis Cache (TTL 5 phút)
        │ (cache miss)
        ▼ (delegate)
  DynamicPricingServiceImpl    ←── Orchestrator
        │
        ├── 1. PricingContextFactory  →  PricingContext (load dữ liệu 1 lần)
        │
        ├── 2. PriceCalculatorChain   →  Decorator Chain
        │       BasePriceCalculator         (Strategy: chọn giá cơ sở)
        │       └── OccupancyDecorator      (+ phụ thu lấp đầy)
        │           └── FnbDecorator        (+ F&B)
        │               └── MemberDiscount  (- giảm thành viên)
        │                   └── Voucher     (- voucher / promo)
        │
        └── 3. PriceValidationChain   →  CoR Validation
                MinPriceHandler         (final ≥ 0)
                └── MaxDiscountHandler   (discount ≤ 50% subtotal)
                    └── FraudDetection  (giá/ghế ≥ 10.000 VND)
```

### Luồng xử lý hoàn chỉnh

```
Request (BookingCalculationDTO)
    │
    ▼ [Proxy]        Cache HIT → trả về ngay
    CachingDynamicPricingProxy
    │ Cache MISS
    ▼ [Orchestrator]
    DynamicPricingServiceImpl
    │
    ├─[1]─► PricingContextFactory.build()
    │            Load Showtime, Seats, Customer, Promotion từ DB
    │            Tính bookedSeatsCount, totalSeatsCount
    │            → PricingContext (immutable, không cần gọi DB nữa)
    │
    ├─[2]─► PriceCalculatorChain.calculate(ctx)
    │            PricingStrategySelector.select(ctx)
    │                → WeekendStrategy / HolidayStrategy / EarlyBird / Standard
    │            BasePriceCalculator: ticketTotal = adjustedBasePrice × seatCount
    │            OccupancyDecorator:  + surcharge nếu occupancy > 70%
    │            FnbDecorator:        + ctx.fnbTotal
    │            MemberDiscount:      - discount theo tier thành viên
    │            VoucherDecorator:    - giảm theo promotion (% hoặc fixed)
    │            → PricingAccumulator → PriceBreakdownDTO
    │
    ├─[3]─► PriceValidationChain.validate(validationCtx)
    │            MinPriceHandler:     finalTotal ≥ 0 VND
    │            MaxDiscountHandler:  discount ≤ 50% subtotal
    │            FraudDetection:      giá/ghế ≥ 10.000 VND
    │            → OK hoặc throw RuntimeException
    │
    └─[4]─► Trả PriceBreakdownDTO → Cache → Response
```

---

## 2. Danh sách file trong dự án

### Specification (điều kiện)
| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `PricingConditions.java` | `patterns/specification/` | Predicate factory: isWeekend, isHoliday, isEarlyBird, isHighOccupancy |
| `PricingContext.java` | `patterns/pricing/context/` | Immutable data object — dữ liệu đầu vào cho toàn bộ engine |
| `PricingContextFactory.java` | `patterns/pricing/context/` | Load DB → build PricingContext (SRP: tách load data) |

### Strategy (chọn giá cơ sở)
| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `PricingStrategy.java` | `patterns/pricing/strategy/` | Interface: `adjustBasePrice()`, `isApplicable()`, `priority()`, `name()` |
| `StandardPricingStrategy.java` | `patterns/pricing/strategy/` | Giá chuẩn, priority thấp nhất — fallback |
| `WeekendPricingStrategy.java` | `patterns/pricing/strategy/` | +15% cuối tuần |
| `HolidayPricingStrategy.java` | `patterns/pricing/strategy/` | +20% ngày lễ, priority cao nhất |
| `EarlyBirdPricingStrategy.java` | `patterns/pricing/strategy/` | -10% đặt sớm ≥ 3 ngày |
| `PricingStrategySelector.java` | `patterns/pricing/strategy/` | Sort theo priority, chọn strategy đầu tiên `isApplicable()` |

### Decorator (cộng dồn phụ thu / giảm giá)
| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `PriceCalculator.java` | `patterns/pricing/decorator/` | Interface: `calculate(PricingContext, PricingAccumulator)` |
| `BasePriceCalculator.java` | `patterns/pricing/decorator/` | Concrete component: ticketTotal = adjustedPrice × seatCount |
| `AbstractPriceCalculatorDecorator.java` | `patterns/pricing/decorator/` | Abstract decorator — delegate + doDecorate |
| `OccupancyDecorator.java` | `patterns/pricing/decorator/` | +10% khi occupancy > 70% |
| `FnbDecorator.java` | `patterns/pricing/decorator/` | + F&B total |
| `MemberDiscountDecorator.java` | `patterns/pricing/decorator/` | - giảm theo membership tier |
| `VoucherDecorator.java` | `patterns/pricing/decorator/` | - giảm theo promotion (% hoặc fixed, có clamp ≥ 0) |
| `PricingAccumulator.java` | `patterns/pricing/decorator/` | Mutable DTO truyền qua chain — build PriceBreakdownDTO cuối |
| `PriceCalculatorChainFactory.java` | `patterns/pricing/decorator/` | Nối decorator chain theo thứ tự |

### Chain of Responsibility (validation)
| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `PriceValidationHandler.java` | `patterns/pricing/validation/` | Interface: `validate(PriceValidationContext)` |
| `AbstractPriceValidationHandler.java` | `patterns/pricing/validation/` | Abstract base — delegate → next |
| `PriceValidationContext.java` | `patterns/pricing/validation/` | DTO: finalTotal, subtotal, seatCount, originalPrice |
| `MinPriceHandler.java` | `patterns/pricing/validation/` | Rule: finalTotal ≥ 0 VND |
| `MaxDiscountHandler.java` | `patterns/pricing/validation/` | Rule: discount ≤ 50% subtotal |
| `FraudDetectionHandler.java` | `patterns/pricing/validation/` | Rule: giá/ghế ≥ 10.000 VND |
| `PriceValidationChainConfig.java` | `patterns/pricing/validation/` | @Configuration — nối validation chain |

### Proxy (cache) + Orchestrator
| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `DynamicPricingService.java` | `services/` | Interface: `calculatePrice(BookingCalculationDTO)` |
| `DynamicPricingServiceImpl.java` | `services/impl/` | Orchestrator 4 bước: Context → Decorator → Validation → DTO |
| `CachingDynamicPricingProxy.java` | `patterns/proxy/` | @Primary proxy: Redis cache, key = sorted seat IDs |
| `PriceBreakdownDTO.java` | `dtos/` | Response: ticketTotal, fnbTotal, discountAmount, finalTotal + nullable fields mới |

---

## 3. Phase 0 — Specification Pattern: PricingConditions

### 2.1 Lý thuyết Design Pattern

**Specification** (thuộc nhóm Behavioral, thường được coi là hybrid Behavioral/Structural) là pattern định nghĩa các **quy tắc nghiệp vụ dưới dạng đối tượng có thể kết hợp** (composable predicates). Mỗi "specification" biểu diễn một điều kiện cụ thể và có thể được kết hợp với các specification khác bằng `and()`, `or()`, `not()`.

Trong Java hiện đại, `Predicate<T>` chính là biểu hiện tự nhiên nhất của Specification — nó cung cấp sẵn các phương thức `.and()`, `.or()`, `.negate()` để compose logic phức tạp.

**Cấu trúc GoF cổ điển:**
```
«interface» Specification<T>
    +isSatisfiedBy(T candidate) boolean
    +and(Specification<T>) Specification<T>
    +or(Specification<T>) Specification<T>
    +not() Specification<T>
```

### 2.2 Vấn đề trong dự án

Logic kiểm tra điều kiện áp giá (cuối tuần? ngày lễ? đặt sớm? phòng gần đầy?) bị lặp lại ở nhiều nơi — trong các `if-else` rải rác trong `BookingServiceImpl`, và có nguy cơ bị lặp lại trong các strategy khác nhau khi mở rộng. Không có đơn vị test độc lập cho từng điều kiện.

### 2.3 Giải pháp áp dụng

`PricingConditions` — một **utility class `final`** với các static factory method trả về `Predicate<PricingContext>`. Mỗi method đóng gói đúng một điều kiện nghiệp vụ:

| Predicate | Điều kiện nghiệp vụ |
|-----------|---------------------|
| `isWeekend()` | `bookingTime` rơi vào Thứ 7 hoặc Chủ Nhật |
| `isHoliday()` | Ngày đặt vé là ngày lễ quốc gia Việt Nam |
| `isEarlyBird()` | Đặt vé trước ≥ 3 ngày so với giờ chiếu |
| `isHighOccupancy()` | Tỷ lệ lấp đầy phòng > 70% |

```java
// Cách sử dụng — Strategy kiểm tra điều kiện:
PricingConditions.isWeekend().test(ctx)         // → true/false
PricingConditions.isEarlyBird().test(ctx)       // → true/false

// Compose predicates:
PricingConditions.isWeekend()
    .and(PricingConditions.isHighOccupancy())
    .test(ctx)  // cuối tuần VÀ gần đầy phòng
```

### 2.4 Vai trò trong hệ thống

`PricingConditions` là **tầng điều kiện dùng chung** — nó không quyết định giá, chỉ trả lời "có/không" cho từng câu hỏi nghiệp vụ. Các Strategy và Decorator bên trên dùng nó như một thư viện điều kiện.

### 2.5 SOLID

- **S**: Mỗi predicate chỉ kiểm tra đúng 1 điều kiện, không tính toán giá.
- **O**: Thêm điều kiện mới = thêm static method mới, không sửa code cũ.
- **D**: Strategy/Decorator phụ thuộc `PricingConditions` (abstraction), không hard-code logic điều kiện.

---

## 3. Phase 1 — Strategy Pattern

### 3.1 Lý thuyết Design Pattern

**Strategy** (Behavioral) định nghĩa một **họ các thuật toán có thể hoán đổi cho nhau** — encapsulate mỗi thuật toán vào một lớp riêng, cho phép client thay đổi thuật toán tại runtime mà không cần biết chi tiết cài đặt.

**Cấu trúc GoF:**
```
«interface» Strategy
    +execute(Context) Result

ConcreteStrategyA implements Strategy
ConcreteStrategyB implements Strategy
ConcreteStrategyC implements Strategy

Context
    -strategy: Strategy
    +setStrategy(Strategy)
    +executeStrategy() Result
```

Điểm mạnh: Client (Context) không phụ thuộc vào implementation cụ thể, chỉ biết interface. OCP: thêm strategy mới không cần sửa Context.

### 3.2 Vấn đề trong dự án

Logic điều chỉnh giá cơ sở (base price) theo ngày, giờ, mùa có nhiều quy tắc **loại trừ lẫn nhau** — nếu là ngày lễ thì áp mức lễ, nếu là cuối tuần áp mức cuối tuần, nếu đặt sớm áp giảm giá EarlyBird. Trước đây nó là một chuỗi `if-else if-else if` khổng lồ không có thứ tự ưu tiên rõ ràng, không testable riêng lẻ.

### 3.3 Giải pháp áp dụng

**Interface `PricingStrategy`** với 4 phương thức:

```java
public interface PricingStrategy {
    BigDecimal adjustBasePrice(BigDecimal basePrice, PricingContext ctx);
    boolean isApplicable(PricingContext ctx);
    int priority();   // số nhỏ = ưu tiên cao
    String name();    // ghi vào PriceBreakdownDTO.appliedStrategy
}
```

**4 concrete strategies** (tất cả là `@Component`, Spring quản lý vòng đời):

| Class | Điều kiện | Priority | Surcharge/Discount |
|-------|-----------|----------|-------------------|
| `EarlyBirdPricingStrategy` | Đặt trước ≥ 3 ngày | 10 (cao nhất) | -15% giảm giá |
| `HolidayPricingStrategy` | Ngày lễ | 20 | +20% phụ thu |
| `WeekendPricingStrategy` | Cuối tuần | 30 | +15% phụ thu |
| `StandardPricingStrategy` | Luôn đúng (fallback) | 999 (thấp nhất) | Không thay đổi |

**`PricingStrategySelector`** — inject tất cả `PricingStrategy` bean qua constructor, sort theo `priority()`, và chọn strategy đầu tiên có `isApplicable(ctx) == true`:

```java
// Trong PricingStrategySelector:
public PricingStrategy select(PricingContext ctx) {
    return strategies.stream()              // đã sort theo priority ASC
        .filter(s -> s.isApplicable(ctx))
        .findFirst()
        .orElse(standardStrategy);         // StandardPricingStrategy luôn đúng
}
```

**Chỉ đúng 1 strategy được chọn** — đây là điểm khác biệt với Decorator (nhiều modifier cùng lúc). Strategy giải quyết bài toán "chọn một trong nhiều công thức tính giá cơ sở".

### 3.4 Cấu hình externalized (application.properties)

```properties
cinema.pricing.weekend-surcharge-pct=15
cinema.pricing.holiday-surcharge-pct=20
cinema.pricing.early-bird-discount-pct=15
cinema.pricing.occupancy-surcharge-pct=10
cinema.pricing.member-silver-discount-pct=5
cinema.pricing.member-gold-discount-pct=10
cinema.pricing.member-platinum-discount-pct=15
```

Tỉ lệ phần trăm được inject qua `@Value` — thay đổi config không cần recompile.

### 3.5 SOLID

- **S**: Mỗi strategy chỉ chứa 1 công thức giá, không làm gì khác.
- **O**: Thêm strategy mới = tạo class `@Component` mới, không sửa `PricingStrategySelector`.
- **L**: Mọi strategy implement interface, có thể hoán đổi cho nhau.
- **D**: `PricingStrategySelector` phụ thuộc `List<PricingStrategy>` (abstraction), không biết concrete classes.

---

## 4. Phase 2 — Decorator Pattern

### 4.1 Lý thuyết Design Pattern

**Decorator** (Structural) cho phép **thêm trách nhiệm vào đối tượng một cách động** bằng cách bọc (wrap) đối tượng gốc trong các "lớp trang trí". Không cần kế thừa, không cần sửa lớp gốc — mỗi Decorator thêm đúng một trách nhiệm, các Decorator có thể xếp chồng theo thứ tự bất kỳ.

**Cấu trúc GoF:**
```
«interface» Component
    +operation() Result

ConcreteComponent implements Component
    +operation() Result              // logic gốc

AbstractDecorator implements Component
    -inner: Component                // wrap một Component khác
    +operation() Result              // gọi inner.operation() rồi thêm logic

ConcreteDecoratorA extends AbstractDecorator
ConcreteDecoratorB extends AbstractDecorator
```

**So sánh với Strategy**: Strategy chọn MỘT thuật toán, Decorator áp dụng NHIỀU modifier xếp chồng đồng thời.

### 4.2 Vấn đề trong dự án

Sau khi có giá cơ sở (từ Strategy), cần áp dụng **nhiều khoản tiền độc lập** theo thứ tự: phụ thu lấp đầy → F&B → giảm thành viên → giảm voucher. Các khoản này:
- **Độc lập nhau**: OccupancyDecorator không cần biết FnbDecorator
- **Có thể bật/tắt**: Nếu không có voucher, VoucherDecorator không làm gì
- **Thứ tự quan trọng**: Discount tính trên subtotal (sau phụ thu + F&B), không phải trên base price

### 4.3 Giải pháp áp dụng

**`PriceCalculator`** — Component interface:
```java
public interface PriceCalculator {
    PricingAccumulator calculate(PricingContext ctx);
}
```

**`PricingAccumulator`** — Mutable object tích lũy kết quả qua chuỗi Decorator:
```java
// Các field:
BigDecimal ticketTotal        // Base × seatCount (đã điều chỉnh Strategy)
BigDecimal occupancySurcharge // OccupancyDecorator đặt
BigDecimal fnbTotal           // FnbDecorator đặt
BigDecimal membershipDiscount // MemberDiscountDecorator đặt
BigDecimal voucherDiscount    // VoucherDecorator đặt
String appliedStrategy        // Tên Strategy được chọn

// Computed:
subtotal()      = ticketTotal + occupancySurcharge + fnbTotal
totalDiscount() = membershipDiscount + voucherDiscount
finalTotal()    = subtotal() - totalDiscount() (clamp ≥ 0)
```

**`AbstractPriceCalculatorDecorator`** — Template Method cho tất cả Decorator:
```java
public abstract class AbstractPriceCalculatorDecorator implements PriceCalculator {
    private final PriceCalculator inner;

    @Override
    public final PricingAccumulator calculate(PricingContext ctx) {
        PricingAccumulator acc = inner.calculate(ctx);  // gọi inner trước
        decorate(acc, ctx);                              // rồi thêm khoản của mình
        return acc;
    }

    protected abstract void decorate(PricingAccumulator acc, PricingContext ctx);
}
```

**Chuỗi Decorator cố định** (build bởi `PriceCalculatorChainFactory`):
```
BasePriceCalculator
  └─ OccupancyDecorator
       └─ FnbDecorator
            └─ MemberDiscountDecorator
                 └─ VoucherDecorator   ← điểm gọi vào chuỗi
```

Gọi `chain.calculate(ctx)` → VoucherDecorator gọi MemberDiscount → FnbDecorator → Occupancy → Base → tích lũy ngược lên → trả `PricingAccumulator` đầy đủ.

**4 Concrete Decorators:**

| Decorator | Trách nhiệm | Điều kiện áp dụng |
|-----------|-------------|-------------------|
| `OccupancyDecorator` | `occupancySurcharge += ticketTotal × pct` | `PricingConditions.isHighOccupancy()` |
| `FnbDecorator` | `fnbTotal = ctx.getFnbTotal()` | Luôn (0 nếu không có F&B) |
| `MemberDiscountDecorator` | `membershipDiscount = subtotal × discountPct` | Có customer và tier |
| `VoucherDecorator` | `voucherDiscount = fixed hoặc % subtotal` | Có promotion hợp lệ |

### 4.4 Tại sao dùng Accumulator thay vì immutable DTO?

Nếu mỗi Decorator tạo một DTO mới → N lần tạo object garbage. Dùng `PricingAccumulator` (mutable, chỉ tồn tại trong 1 request) → hiệu quả hơn, `toDTO()` chỉ được gọi **1 lần duy nhất** ở cuối chuỗi.

### 4.5 SOLID

- **S**: Mỗi Decorator chỉ tính đúng 1 khoản tiền.
- **O**: Thêm Decorator mới (VD: `LoyaltyPointsDecorator`) = tạo class mới, không sửa chain cũ.
- **L**: Mọi Decorator là `PriceCalculator`, có thể thay thế nhau.
- **D**: `AbstractPriceCalculatorDecorator` phụ thuộc `PriceCalculator` (interface), không biết impl cụ thể.

---

## 5. Phase 3 — Chain of Responsibility: Price Validation

### 5.1 Lý thuyết Design Pattern

**Chain of Responsibility** (Behavioral) tổ chức một chuỗi các "handler" để xử lý request tuần tự. Mỗi handler quyết định:
1. Tự xử lý (và dừng chuỗi) hoặc ném exception, hoặc
2. Chuyển tiếp cho handler kế tiếp

**Ứng dụng trong dự án này**: Không cần dừng sớm khi pass (ngược lại, luôn truyền về cuối) — mỗi handler **validate một invariant độc lập**, ném exception nếu vi phạm, không ném thì chuyển tiếp. Pattern giúp tách validation logic thành các class độc lập, testable riêng.

> *Lưu ý*: Hệ thống đã có một CoR chain khác là `CheckoutValidationChain` (validate trước khi tạo booking). Chain này là chain thứ hai, hoàn toàn độc lập về mục đích — validate kết quả **tính giá**, không phải validate input booking.

### 5.2 Vấn đề trong dự án

Sau khi Decorator chain tính xong giá, kết quả có thể bất hợp lệ do:
- Voucher lớn quá khiến `finalTotal` âm
- Discount quá cao (> 50% subtotal) — dấu hiệu config lỗi
- Giá trên mỗi ghế quá thấp (< 10.000 VND) — dấu hiệu gian lận hoặc config sai

Các check này không thuộc về bất kỳ Decorator nào (mỗi Decorator chỉ biết khoản của nó), cần một tầng validation tập trung phía sau.

### 5.3 Giải pháp áp dụng

**`PriceValidationContext`** — Immutable context gửi vào chain:
```java
@Value
public class PriceValidationContext {
    BigDecimal subtotal;       // ticketTotal + occupancy + fnb
    BigDecimal totalDiscount;  // membership + voucher
    BigDecimal finalTotal;
    int seatCount;
}
```

**3 Concrete Handlers** — mỗi handler kiểm tra 1 invariant:

| Handler | Rule | Exception message |
|---------|------|-------------------|
| `MinPriceHandler` | `finalTotal >= 0` | "Final price cannot be negative" |
| `MaxDiscountHandler` | `totalDiscount <= subtotal × 50%` | "Total discount exceeds 50% of subtotal" |
| `FraudDetectionHandler` | `finalTotal / seatCount >= 10.000` | "Price per seat is suspiciously low" |

**`AbstractPriceValidationHandler`** — Template:
```java
@Override
public void validate(PriceValidationContext ctx) {
    doValidate(ctx);    // kiểm tra, throw nếu vi phạm
    if (next != null) next.validate(ctx);   // truyền tiếp nếu pass
}
```

**`PriceValidationChainConfig`** — `@Configuration` lắp ráp chain:
```java
@Bean
public PriceValidationHandler priceValidationChain(
        MinPriceHandler min, MaxDiscountHandler max, FraudDetectionHandler fraud) {
    min.setNext(max);
    max.setNext(fraud);
    return min;   // trả về đầu chuỗi
}
```

### 5.4 SOLID

- **S**: Mỗi handler chứa đúng 1 rule validation.
- **O**: Thêm validation mới = thêm handler class + cắm vào config.
- **L**: Mọi handler implement `PriceValidationHandler`, có thể swap vị trí trong chain.
- **D**: `DynamicPricingServiceImpl` inject `PriceValidationHandler` (interface đầu chain), không biết tên từng handler.

---

## 6. Phase 4 — Service Orchestration

### 6.1 Thiết kế

`DynamicPricingServiceImpl` là **orchestrator thuần túy** — nó không chứa bất kỳ pricing logic nào, chỉ điều phối 3 tầng:

```java
@Service("dynamicPricingServiceImpl")
public class DynamicPricingServiceImpl implements DynamicPricingService {

    public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
        // 1. Build context (load dữ liệu)
        PricingContext ctx = contextFactory.build(request);

        // 2. Tính giá qua Decorator chain
        PriceCalculator chain = chainFactory.build();
        PricingAccumulator acc = chain.calculate(ctx);

        // 3. Validate kết quả qua CoR chain
        PriceValidationContext validCtx = new PriceValidationContext(
            acc.subtotal(), acc.totalDiscount(), acc.finalTotal(), ctx.getSeats().size()
        );
        validationChain.validate(validCtx);  // throw nếu vi phạm

        // 4. Trả DTO
        return acc.toDTO();
    }
}
```

### 6.2 Tại sao cần qualifier `"dynamicPricingServiceImpl"`?

Spring có 2 bean implement `DynamicPricingService`: `DynamicPricingServiceImpl` và `CachingDynamicPricingProxy`. `CachingDynamicPricingProxy` được đánh dấu `@Primary` — nên khi ai inject `DynamicPricingService`, họ nhận Proxy. Proxy cần inject impl thực → dùng `@Qualifier("dynamicPricingServiceImpl")` để tránh vòng lặp circular.

### 6.3 SOLID

- **S**: Chỉ orchestrate, không chứa pricing logic.
- **D**: Phụ thuộc `PricingContextFactory`, `PriceCalculatorChainFactory`, `PriceValidationHandler` — tất cả là abstraction.

---

## 7. Phase 5 — Proxy Cache

### 7.1 Lý thuyết Design Pattern

**Proxy** (Structural) cung cấp một đối tượng thay thế (surrogate) để kiểm soát việc truy cập vào đối tượng thật. Client không biết đang nói chuyện với Proxy hay real object vì cả hai implement cùng interface.

Các loại Proxy phổ biến:
- **Virtual Proxy**: Lazy initialization (tạo object nặng khi cần)
- **Remote Proxy**: Đại diện cho object ở máy khác (gRPC stub)
- **Caching Proxy**: Cache kết quả để không phải tính lại ← **loại áp dụng ở đây**
- **Protection Proxy**: Kiểm tra quyền truy cập

**Cấu trúc GoF:**
```
«interface» Subject
    +request() Result

RealSubject implements Subject
    +request() Result    // thực hiện công việc nặng

Proxy implements Subject
    -realSubject: Subject
    +request() Result    // kiểm tra cache → nếu miss → gọi realSubject
```

### 7.2 Vấn đề trong dự án

Tính giá vé là thao tác **tốn kém**: load nhiều entity từ DB, thực hiện nhiều phép tính. Nhưng với cùng một suất chiếu, cùng ghế, cùng user, cùng promo code → kết quả **giống hệt nhau** trong vòng vài phút. Không cần tính lại mỗi request.

### 7.3 Giải pháp áp dụng

**`CachingDynamicPricingProxy`** — `@Primary @Service` implement `DynamicPricingService`:

```java
@Primary
@Service
public class CachingDynamicPricingProxy implements DynamicPricingService {

    @Qualifier("dynamicPricingServiceImpl")
    private DynamicPricingService delegate;

    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
        String key = buildCacheKey(request);

        Object cached = cacheGet(key);
        if (cached instanceof PriceBreakdownDTO dto) {
            return dto;   // HIT: trả ngay, không gọi delegate
        }

        // MISS: gọi impl thật → validate → lưu cache
        PriceBreakdownDTO result = delegate.calculatePrice(request);
        cachePut(key, result);
        return result;
    }
}
```

**Cache Key Strategy** — chứa đủ thông tin để đảm bảo không có false hit:
```
pricing:{showtimeId}:[{seatIds sorted}]:{userId}:{promoCode}

Ví dụ:
pricing:42:[1,3,5]:101:SUMMER20
pricing:42:[1,3,5]:anonymous:none
```

**Tại sao cần userId trong cache key?** VIP member và anonymous user có giá khác nhau (membership discount) → phải cache riêng. `resolveUserId()` lấy từ `SecurityContextHolder`.

**Tại sao sort seatIds?** `[1,2,3]` và `[3,1,2]` là cùng yêu cầu → sort để đảm bảo cùng key.

**TTL = 5 phút**: Occupancy có thể thay đổi (ghế được đặt liên tục), nên không cache quá lâu.

**Không lưu cache nếu exception**: `cachePut` chỉ được gọi sau khi `delegate.calculatePrice()` thành công — nếu validation fail thì exception propagate, cache sạch.

**`protected cacheGet/cachePut`** — design có chủ ý: test có thể subclass và override 2 method này bằng HashMap in-memory, không cần mock `RedisTemplate`.

### 7.4 Tích hợp vào BookingServiceImpl

```java
// Trước (cũ — 50+ dòng hard-code):
private BigDecimal calculateTotalPrice(BookingCalculationDTO dto) {
    // ... gọi fnbItemRepository, promotionRepository, nhiều logic ...
}

// Sau (mới — 1 dòng delegate):
@Autowired
private DynamicPricingService dynamicPricingService; // Spring inject CachingDynamicPricingProxy

public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
    return dynamicPricingService.calculatePrice(request);
}
```

`BookingServiceImpl` không biết về cache, không biết về Strategy hay Decorator — hoàn toàn decoupled.

### 7.5 SOLID

- **S**: Proxy chỉ làm cache, không làm validation hay business logic.
- **O**: Thêm cache layer mới (VD: L1 in-memory + L2 Redis) = tạo Proxy mới bọc Proxy cũ.
- **L**: Proxy implement `DynamicPricingService`, hoàn toàn thay thế được impl thật.
- **D**: `BookingServiceImpl` inject `DynamicPricingService` (interface) — không biết là Proxy hay Impl.

---

## 8. SOLID & Clean Code Summary

### Bảng tổng hợp SOLID theo Pattern

| Phase | Pattern | S | O | L | I | D |
|-------|---------|---|---|---|---|---|
| 0 | Specification | Mỗi predicate 1 điều kiện | Thêm predicate mới | — | Predicate hẹp | Strategies dùng PricingConditions |
| 1 | Strategy | Mỗi strategy 1 công thức | Thêm @Component mới | Hoán đổi được | Interface hẹp | Selector inject List<PricingStrategy> |
| 2 | Decorator | Mỗi decorator 1 khoản | Thêm decorator class | Mọi decorator thay thế được | PriceCalculator hẹp | AbstractDecorator dùng interface |
| 3 | CoR Validation | Mỗi handler 1 rule | Thêm handler + config | Hoán đổi vị trí chain | PriceValidationHandler hẹp | ServiceImpl inject interface |
| 4 | Orchestration | ServiceImpl chỉ điều phối | — | — | DynamicPricingService hẹp | Phụ thuộc factory + handler interfaces |
| 5 | Proxy Cache | Chỉ làm cache | Wrap bằng Proxy khác | Thay thế impl | — | BookingService inject interface |

### Kiến trúc phân tầng rõ ràng

```
Tầng                     Trách nhiệm
─────────────────────────────────────────────────────────────
API / Controller         Nhận request HTTP, trả response
BookingServiceImpl       Orchestrate booking flow
DynamicPricingService    Interface tính giá (DIP)
CachingDynamicPricingProxy  Cache layer (Proxy)
DynamicPricingServiceImpl   Orchestrate pricing flow
PricingContextFactory    Load dữ liệu từ DB → immutable context
PricingConditions        Library điều kiện nghiệp vụ (Specification)
PricingStrategy          Chọn 1 công thức giá cơ sở (Strategy)
PriceCalculator chain    Tích lũy nhiều khoản (Decorator)
PriceValidationHandler   Validate invariants (CoR)
```

### Test Coverage

| Test class | Pattern | Tests |
|-----------|---------|-------|
| `PricingConditionsTest` | Specification | 9 |
| `PricingStrategyTest` | Strategy | 21 |
| `DecoratorTest` | Decorator | 9 |
| `PriceValidationChainTest` | CoR Validation | 10 |
| `DynamicPricingServiceImplTest` | Orchestration | 9 |
| `CachingDynamicPricingProxyTest` | Proxy | 9 |
| **Tổng** | | **67/67 ✅** |

---

## 9. Class Diagram Tổng hợp

> **UML tách riêng** (Mermaid `classDiagram` đầy đủ): [UML/08-dynamic-pricing-engine.md](../../UML/08-dynamic-pricing-engine.md).
>
> Tham chiếu domain gốc (không gộp UML): [classdiagram.md](../../classdiagram.md).

---

*Tài liệu này được tạo đồng thời với implementation Phase 0–5 của Dynamic Pricing Engine.*
*Tests: 67/67 passed — Specification (9), Strategy (21), Decorator (9), CoR Validation (10), Service (9), Proxy (9).*
