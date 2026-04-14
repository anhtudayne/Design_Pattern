# Tai lieu ky thuat: Dynamic Pricing Engine

> **Tong quan**: Dynamic Pricing Engine tich hop **5 GoF Design Pattern** trong production path: Chain of Responsibility (validation), Proxy (Redis cache), Specification (dieu kien), Strategy (tinh gia), Decorator (giam gia). Luong: `BookingServiceImpl` → CoR validation → `PricingContextBuilder` → `CachingPricingEngineProxy` → `PricingEngine`.

---

## Muc luc

1. [Tong quan & Kien truc](#1-tong-quan--kien-truc)
2. [Danh sach file](#2-danh-sach-file)
3. [Chain of Responsibility — validation](#3-chain-of-responsibility--validation)
4. [Proxy — CachingPricingEngineProxy](#4-proxy--cachingpricingengineproxy)
5. [Specification — PricingConditions](#5-specification--pricingconditions)
6. [Strategy — Ticket / Fnb / TimeBased](#6-strategy)
7. [Decorator — DiscountComponent](#7-decorator)
8. [Orchestrator — PricingEngine](#8-orchestrator--pricingengine)
9. [BookingServiceImpl & PricingContextBuilder](#9-service-layer)
10. [SOLID & tom tat](#10-solid--tom-tat)
11. [Class diagram](#11-class-diagram)

---

## 1. Tong quan & Kien truc

```
POST /api/booking/calculate
        |
        v BookingServiceImpl.calculatePrice()
        |
        +--[1. CoR]-->> ShowtimeFutureHandler -> SeatsAvailableHandler -> PromoValidHandler
        |                  (validate + populate showtime, promotion)
        |
        +--[1b]-->> PricingContextBuilder.build(validationCtx, request)
        |            load seats, F&B prices, customer, occupancy -> PricingContext
        |
        +--[2. Proxy]-->> CachingPricingEngineProxy
        |                  cache hit  -> PriceBreakdownDTO tu Redis
        |                  cache miss -> delegate PricingEngine
        |
        +--[3. Engine]-->> PricingEngine
                |
                +-- TicketPricingStrategy  -> ticketTotal
                +-- FnbPricingStrategy      -> fnbTotal
                +-- TimeBasedPricingStrategy -> timeBasedSurcharge
                |       +-- PricingConditions (weekend/holiday)
                |
                +-- Decorator: NoDiscount -> Promotion -> Member
```

---

## 2. Danh sach file

### Chain of Responsibility

| File | Package | Vai tro |
|------|---------|---------|
| `PricingValidationHandler` | `.../validation/` | `validate(ctx)` |
| `AbstractPricingValidationHandler` | `.../validation/` | base + next |
| `PricingValidationContext` | `.../validation/` | request, showtime, promotion |
| `ShowtimeFutureHandler` | `.../validation/` | showtime ton tai + chua chieu |
| `SeatsAvailableHandler` | `.../validation/` | ghe chua ban |
| `PromoValidHandler` | `.../validation/` | promo hop le |
| `PricingValidationConfig` | `.../validation/` | noi chain (@Bean) |

### Proxy

| File | Package |
|------|---------|
| `IPricingEngine` | `strategy_decorator/pricing/` |
| `CachingPricingEngineProxy` | `strategy_decorator/pricing/` (@Primary) |

### Specification

| File | Package |
|------|---------|
| `PricingConditions` | `patterns/specification/` |
| `PricingSpecificationContext` | `patterns/specification/` — VO immutable cho predicate (khac `PricingContext` tang engine) |

### Strategy + Decorator

| File | Ghi chu |
|------|---------|
| `PricingEngine` | `@Component("pricingEngine")`, `EnumMap` theo `PricingLineType` |
| `PricingContextBuilder` | `@Component`, `build(validationCtx, request)` |
| `PricingContext` | Lombok builder: seats, ResolvedFnbItem, occupancy, ... |
| `PricingLineType` | TICKET, FNB, TIME_BASED_SURCHARGE |
| `PricingStrategy` | `lineType()`, `calculate(ctx)` |
| `TicketPricingStrategy`, `FnbPricingStrategy`, `TimeBasedPricingStrategy` | @Component |
| `DiscountComponent`, `NoDiscount`, `PromotionDiscountDecorator`, `MemberDiscountDecorator` | Decorator giam gia |
| `PriceBreakdownDTO` | `timeBasedSurcharge` (truoc day ten `occupancySurcharge` — doi cho dung nghia) |

---

## 3. Chain of Responsibility — validation

**Thu tu:** `ShowtimeFutureHandler` -> `SeatsAvailableHandler` -> `PromoValidHandler`.

- **ShowtimeFutureHandler** phai chay **dau tien** de populate `showtime` — `PricingContextBuilder` tai su dung, khong query lai showtime.
- **PromoValidHandler** chi doc `request.getPromoCode()`, **khong** doc `showtime` tu `PricingValidationContext`.

---

## 4. Proxy — CachingPricingEngineProxy

**Cache key** (tu `PricingContext`):

`pricing:{showtimeId}:seats:{sorted seat ids}:fnb:{sorted itemId:qty}|fnb:none:promo:{code}:cust:{userId|anon}`

- Seat ids sort tang dan.
- F&B: sort theo `itemId`, format `id:qty` — tranh hai don F&B khac nhau dung chung cache.
- TTL: `cinema.app.redisTtlSeconds` (mac dinh 600s).

Caller inject `IPricingEngine` (@Primary = proxy); delegate that la `PricingEngine` voi `@Qualifier("pricingEngine")`.

---

## 5. Specification — PricingConditions

Predicate: `Predicate<PricingSpecificationContext>`.

`TimeBasedPricingStrategy` convert `PricingContext` -> `PricingSpecificationContext` (`toSpecContext`) roi goi `isHoliday()` / `isWeekend()`. Cac strategy khac khong qua tang Specification.

---

## 6. Strategy

| Strategy | Cong thuc |
|----------|-----------|
| Ticket | sum(basePrice + seat surcharge) |
| Fnb | sum(resolved price * qty) |
| TimeBased | ticketSubtotal * rate% neu weekend/holiday |

**timeBasedSurcharge** trong `PriceBreakdownDTO` do `TimeBasedPricingStrategy` tinh (khong phai “occupancy” — ten JSON da doi).

Moi `PricingStrategy` tra ve `lineType()` de `PricingEngine` gom vao `EnumMap`.

---

## 7. Decorator

`NoDiscount` -> `PromotionDiscountDecorator` (neu co promotion) -> `MemberDiscountDecorator` (neu co tier).

Promo da duoc CoR validate; decorator chi tinh so tien giam.

---

## 8. Orchestrator — PricingEngine

- Constructor: `List<PricingStrategy>`, `NoDiscount`.
- Dung `EnumMap<PricingLineType, PricingStrategy>`: duplicate `lineType` hoac thieu mot line -> `IllegalStateException` luc khoi tao.
- Lay ticketTotal, fnbTotal, timeBasedSurcharge; build decorator chain; tra `PriceBreakdownDTO`.

---

## 9. Service layer

```java
public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
    PricingValidationContext validationCtx = PricingValidationContext.builder()
            .request(request).build();
    pricingValidationChain.validate(validationCtx);
    return pricingEngine.calculateTotalPrice(
            pricingContextBuilder.build(validationCtx, request));
}
```

`PricingContextBuilder`: load seats, resolve F&B + gia tu DB, customer (Security), dem occupancy -> `PricingContext.builder()`.

---

## 10. SOLID & tom tat

| Tang | Trach nhiem |
|------|-------------|
| BookingServiceImpl | CoR -> builder -> IPricingEngine |
| PricingContextBuilder | DTO + ket qua CoR -> PricingContext |
| CoR | Validate + showtime/promotion |
| CachingPricingEngineProxy | Redis |
| PricingEngine | EnumMap strategy + decorator |
| PricingConditions | Predicate static (co the refactor inject sau) |

**Test:** `PricingConditionsTest` — 9 test cho Specification.

---

## 11. Class diagram

- UML day du: [UML/08-dynamic-pricing-engine.md](../../UML/08-dynamic-pricing-engine.md)
- Pattern-only: [UML/pattern-only/08-dynamic-pricing-engine.md](../../UML/pattern-only/08-dynamic-pricing-engine.md)
- Domain: [classdiagram.md](../../classdiagram.md)
