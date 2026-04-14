# Tai lieu ky thuat: Dynamic Pricing Engine

> **Tong quan**: Dynamic Pricing Engine ket hop 5 GoF pattern tren luong `POST /api/booking/calculate`: Chain of Responsibility (validate input), Proxy (Redis cache), Specification (dieu kien ngay gio), Strategy (tinh tung thanh phan gia), Decorator (giam gia).

---

## Muc luc

1. [Tong quan luong backend](#1-tong-quan-luong-backend)
2. [Danh sach file backend lien quan](#2-danh-sach-file-backend-lien-quan)
3. [Luong thuc thi chi tiet](#3-luong-thuc-thi-chi-tiet)
4. [Chain of Responsibility](#4-chain-of-responsibility)
5. [PricingContextBuilder](#5-pricingcontextbuilder)
6. [Proxy Redis](#6-proxy-redis)
7. [PricingEngine + Strategy + Specification + Decorator](#7-pricingengine--strategy--specification--decorator)
8. [Output va cac diem can luu y](#8-output-va-cac-diem-can-luu-y)
9. [Class diagram](#9-class-diagram)

---

## 1. Tong quan luong backend

```
POST /api/booking/calculate
        |
        v BookingController.calculatePrice()
        |
        v BookingServiceImpl.calculatePrice(request)
        |
        +-- [A] pricingValidationChain.validate(ctx)
        |        ShowtimeFuture -> SeatsAvailable -> PromoValid
        |        (populate showtime + promotion vao PricingValidationContext)
        |
        +-- [B] re-resolve promo qua PromotionInventoryService (neu co promoCode)
        |
        +-- [C] PricingContextBuilder.build(validationCtx, request)
        |        load seats + fnb gia DB + customer dang login + occupancy
        |
        +-- [D] IPricingEngine.calculateTotalPrice(context)
                 |
                 +-- @Primary CachingPricingEngineProxy
                 |      cache hit  -> tra PriceBreakdownDTO tu Redis
                 |      cache miss -> delegate PricingEngine
                 |
                 +-- PricingEngine
                        1) Strategy: TICKET + FNB + TIME_BASED_SURCHARGE
                        2) Decorator: NoDiscount -> Promotion -> Member
                        3) Build PriceBreakdownDTO
```

---

## 2. Danh sach file backend lien quan

| File | Vai tro |
|------|---------|
| `controllers/BookingController` | Expose API `/api/booking/calculate` |
| `services/impl/BookingServiceImpl` | Orchestrate full flow tinh gia |
| `services/strategy_decorator/pricing/validation/*` | CoR validation chain |
| `services/strategy_decorator/pricing/PricingContextBuilder` | Build `PricingContext` cho engine |
| `services/strategy_decorator/pricing/IPricingEngine` | Interface de chen Proxy |
| `services/strategy_decorator/pricing/CachingPricingEngineProxy` | Redis cache layer |
| `services/strategy_decorator/pricing/PricingEngine` | Core orchestrator trong engine |
| `services/strategy_decorator/pricing/*PricingStrategy` | Ticket/FnB/TimeBased calculations |
| `patterns/specification/PricingConditions` | Static predicates (holiday/weekend/...) |
| `services/strategy_decorator/pricing/*Discount*` | Decorator chain cho discount |
| `dtos/PriceBreakdownDTO` | Payload tra ve cho frontend |

---

## 3. Luong thuc thi chi tiet

### Buoc 1 - Khoi tao validation context

`BookingServiceImpl.calculatePrice()` tao:

- `PricingValidationContext.request = request`
- `showtime = null`, `promotion = null`

### Buoc 2 - Chay Chain of Responsibility

Thu tu trong `PricingValidationConfig`:

1. `ShowtimeFutureHandler`
2. `SeatsAvailableHandler`
3. `PromoValidHandler`

Neu bat ky handler throw exception -> dung flow, API tra loi loi.

### Buoc 3 - Re-resolve promotion tai service

Sau khi chain xong, `BookingServiceImpl` tiep tuc:

- Neu co `promoCode` thi goi lai `promotionInventoryService.resolvePromotionForPricing(promoCode)`
- Gan lai vao `validationCtx.setPromotion(...)`

Muc dich: dam bao promotion trong context la trang thai hien tai tu inventory service ngay truoc khi build pricing context.

### Buoc 4 - Build PricingContext

`PricingContextBuilder.build(validationCtx, request)`:

- Lay `showtime` va `promotion` tu `validationCtx` (tai su dung data da validate)
- Query seats theo `request.seatIds`
- Resolve F&B items + gia tu DB thanh `ResolvedFnbItem`
- Resolve customer hien tai tu `SecurityContext` (co the null voi guest)
- Tinh occupancy: `bookedSeatsCount`, `totalSeatsCount`
- Set `bookingTime = LocalDateTime.now()`

### Buoc 5 - Qua Proxy va Engine

- `IPricingEngine` duoc inject la `CachingPricingEngineProxy` (`@Primary`)
- Proxy tao cache key tu showtime/seats/fnb/promo/customer
- Cache miss -> delegate vao `PricingEngine`
- `PricingEngine` tinh toan strategy + discount, build `PriceBreakdownDTO`
- Proxy cache ket qua theo TTL va tra ve client

---

## 4. Chain of Responsibility

### 4.1 `ShowtimeFutureHandler`

- Validate showtime ton tai theo `request.showtimeId`
- Validate `startTime` chua o qua khu
- Populate `context.showtime`

### 4.2 `SeatsAvailableHandler`

- Validate `seatIds` khong rong
- Check tung seat chua duoc ban (`ticketRepository.existsByShowtime_ShowtimeIdAndSeat_SeatId`)

### 4.3 `PromoValidHandler`

- Neu khong co `promoCode` -> bo qua
- Neu co `promoCode`:
  - Promotion phai ton tai
  - Chua het han (`validTo`)
  - Con luot su dung (`promotionInventoryService.resolvePromotionForPricing(...) != null`)
- Hop le thi populate `context.promotion`
- **Luu y quan trong**: handler nay dang **throw exception** neu promo khong hop le (khong phai graceful ignore)

---

## 5. PricingContextBuilder

Builder nay la diem ghep du lieu truoc khi vao engine:

- Khong tinh gia tai day; chi map/load du lieu dau vao cho strategies
- F&B gia duoc chot theo DB tai thoi diem calculate
- Customer null la case binh thuong (anonymous)
- Occupancy fields duoc tao de strategies/specification co the dung ve sau

---

## 6. Proxy Redis

`CachingPricingEngineProxy` implement `IPricingEngine`.

**Key format thuc te**:

`pricing:{showtimeId}:seats:{sortedSeatIds|none}:fnb:{sorted itemId:qty|none}:promo:{code|none}:cust:{userId|anon}`

**Quy tac key**:

- Seats sort tang dan theo `seatId`
- F&B sort theo `itemId`, format `itemId:quantity`
- Co customer thi dung `cust:{userId}`, khong thi `cust:anon`
- TTL: `cinema.app.redisTtlSeconds` (default 600s)

---

## 7. PricingEngine + Strategy + Specification + Decorator

### 7.1 PricingEngine

- Khoi tao `EnumMap<PricingLineType, PricingStrategy>`
- Fail-fast neu duplicate/missing strategy (`IllegalStateException`)
- Tinh:
  - `ticketTotal` tu `TicketPricingStrategy`
  - `fnbTotal` tu `FnbPricingStrategy`
  - `timeBasedSurcharge` tu `TimeBasedPricingStrategy`
- `subtotal = ticketTotal + fnbTotal + timeBasedSurcharge`
- Apply decorator chain de lay `discountAmount`
- `finalTotal = max(subtotal - discountAmount, 0)`

### 7.2 Strategy details

| Strategy | Cong thuc |
|----------|-----------|
| Ticket | `sum(basePrice + seatSurcharge)` |
| Fnb | `sum(itemPrice * quantity)` |
| TimeBased | `ticketSubtotal * rate%` neu holiday/weekend, nguoc lai = 0 |

### 7.3 Specification trong TimeBased

`TimeBasedPricingStrategy` convert `PricingContext` -> `PricingSpecificationContext`, roi evaluate:

- `PricingConditions.isHoliday()`
- `PricingConditions.isWeekend()`

Rate:

- Holiday: `cinema.pricing.holiday-surcharge-pct` (default 20)
- Weekend: `cinema.pricing.weekend-surcharge-pct` (default 15)
- Neu vua holiday vua weekend -> uu tien holiday

### 7.4 Decorator chain

Chain duoc build:

- Bat dau `NoDiscount`
- Neu co promotion -> wrap `PromotionDiscountDecorator`
- Neu co member tier hop le -> wrap `MemberDiscountDecorator`

Thu tu ap dung thuc te:

1. Promotion discount ap tren `subtotal`.
2. Membership discount ap tren **so tien con lai sau promo**.
3. `discountAmount = promotionDiscount + membershipDiscount`.

---

## 8. Output va cac diem can luu y

`PriceBreakdownDTO` hien tai gom:

- `ticketTotal`
- `timeBasedSurcharge`
- `fnbTotal`
- `membershipDiscount` (muc giam member thuc te, tinh tren phan con lai sau promo)
- `discountAmount` (tong discount thuc su da tru vao final)
- `appliedStrategy`
- `finalTotal`

**Luu y**:

1. `membershipDiscount` duoc tinh sau promotion, phan anh dung luong giam gia tu member.
2. `appliedStrategy` duoc build dong theo context (`TICKET`, `FNB`, `TIME_BASED`, `MEMBER_DISCOUNT`, `PROMO`).
3. Luong calculate price chi "read/preview" gia; viec reserve promotion thuc su nam o flow checkout (`reservePromotionOrThrow`), khong nam trong dynamic pricing engine.

---

## 9. Mapping theo hoat dong ung dung (App -> Dynamic Pricing Engine)

Section nay tra loi cau hoi: "Nguoi dung bam gi trong app thi Dynamic Pricing Engine lam gi?"

### 9.1 Nguoi dung vao man hinh chon ghe

**App action**

- Frontend goi `GET /api/booking/seats/{showtimeId}` de ve map ghe.

**Engine action**

- Dynamic Pricing Engine **chua chay**.
- Backend chi tra trang thai ghe (TRONG/DA_BAN/DANG_GIU) va gia ghe co ban (`basePrice + seat surcharge`) de UI hien thi nhanh.

### 9.2 Nguoi dung chon ghe / bo chon ghe

**App action**

- Frontend lock/unlock ghe qua `/api/booking/lock` va `/api/booking/unlock`.

**Engine action**

- Dynamic Pricing Engine **van chua chay**.
- Redis locking xu ly tranh tranh chap ghe giua nhieu user.

### 9.3 Nguoi dung chon F&B

**App action**

- UI cap nhat gio hang F&B (itemId, quantity).

**Engine action**

- Dynamic Pricing Engine **chua tinh ngay** neu frontend chua goi `/calculate`.
- Du lieu F&B duoc dung o lan tinh gia tiep theo.

### 9.4 Nguoi dung nhap/doi ma khuyen mai

**App action**

- Frontend gui lai request tinh gia co `promoCode` moi qua `POST /api/booking/calculate`.

**Engine action**

- CoR (`PromoValidHandler`) kiem tra ma ton tai, con han, con luot.
- Neu khong hop le -> throw exception, API tra loi de UI hien thong bao.
- Neu hop le -> promotion duoc dua vao `PricingContext` de decorator ap dung discount.

### 9.5 Nguoi dung bam "Tam tinh"/"Xem tong tien" (hoac moi lan UI auto recalculate)

**App action**

- Goi `POST /api/booking/calculate` voi `showtimeId`, `seatIds`, `fnbs`, `promoCode`.

**Engine action**

1. Validate input qua chain (`ShowtimeFuture -> SeatsAvailable -> PromoValid`).
2. Build `PricingContext` (load seats, load gia F&B, resolve customer dang login, occupancy).
3. Proxy check Redis cache:
   - cache hit -> tra ket qua ngay.
   - cache miss -> vao `PricingEngine`.
4. `PricingEngine`:
   - Strategy tinh `ticketTotal`, `fnbTotal`, `timeBasedSurcharge`.
   - Specification xac dinh holiday/weekend cho time-based surcharge.
   - Decorator tinh discount promo/member.
   - Build `PriceBreakdownDTO` va tra ve UI.

### 9.6 Nguoi dung tiep tuc sang checkout/thanh toan

**App action**

- User xac nhan don va thuc hien thanh toan.

**Engine action**

- Dynamic Pricing Engine khong phai noi giu ton kho promo/fnb.
- Viec **reserve/consume thuc su** (vi du promo inventory) nam o checkout flow khac, khong nam trong `/calculate`.

---

## 10. Class diagram

- UML day du: [UML/08-dynamic-pricing-engine.md](../../UML/08-dynamic-pricing-engine.md)
- Pattern-only: [UML/pattern-only/08-dynamic-pricing-engine.md](../../UML/pattern-only/08-dynamic-pricing-engine.md)
- Domain: [classdiagram.md](../../classdiagram.md)
