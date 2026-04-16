# Kịch bản thuyết trình — Dynamic Pricing Engine

> **Cách dùng**: **Pha A** — lời thoại + mốc chỉ tay trên UML ([08-dynamic-pricing-engine.md](08-dynamic-pricing-engine.md)), đi **theo đúng thứ tự request chạy**; tới bước nào dùng pattern nào thì **nói pattern đó ngay tại bước đó**, không tách “luồng” rồi mới lần lượt pattern. **Pha B** — [bảng chỉ đường file](#phần-b--sau-khi-thuyết-trình-uml-chỉ-đường-file--vị-trí). Luồng kỹ thuật: [../docs/patterns/08-dynamic-pricing-engine.md](../docs/patterns/08-dynamic-pricing-engine.md).

---

## Thông tin buổi trình bày (gợi ý)

| Hạng mục | Gợi ý |
|----------|--------|
| Thời lượng | **Pha A (UML)**: 12–18 phút. **Pha B (code, tuỳ chọn)**: thêm 5–10 phút. Có demo API: ~25–30 phút tổng. |
| Đối tượng | Hội đồng / giảng viên / nhóm lớp — đã quen OOP cơ bản |
| Màn hình **Pha A** | **Chỉ** sơ đồ UML. Khi nói từng bước, **trỏ** vào đúng khối lớp trên sơ đồ vừa được nhắc tới. |
| Màn hình **Pha B** | Laptop / màn phụ: mở file theo bảng cuối **cùng thứ tự với luồng runtime**. |
| Tài liệu tham chiếu | [08-dynamic-pricing-engine.md](08-dynamic-pricing-engine.md), [../docs/patterns/08-dynamic-pricing-engine.md](../docs/patterns/08-dynamic-pricing-engine.md) |

---

## Hai pha trình bày (tóm tắt)

- **Pha A**: Đọc [kịch bản luồng](#kịch-bản-pha-a-theo-luồng-runtime) từ đầu tới cuối một mạch.  
- **Pha B**: [Phần B](#phần-b--sau-khi-thuyết-trình-uml-chỉ-đường-file--vị-trí) — cùng thứ tự với luồng thực thi.  
- Đường dẫn `backend/...` là tương đối gốc repo.

---

## Kịch bản Pha A — theo luồng runtime

### Mở đầu (khoảng 45 giây)

**(Chào hỏi ngắn, mở sơ đồ.)**

Em trình bày **Dynamic Pricing Engine**: từ lúc client gửi yêu cầu tính tiền cho tới lúc trả về chi tiết giá. Em **đi theo đúng thứ tự xử lý trong backend** — `POST /api/booking/calculate` — và **mỗi khi luồng đi qua chỗ áp dụng mẫu thiết kế**, em dừng vài giây để nói **pattern đó** và **chỉ vào nhóm lớp tương ứng trên UML**, không tách sang một mục “liệt kê pattern” riêng.

---

### Bước 1 — Vào service: đầu vào và điều phối

**(Trên UML: `BookingServiceImpl`; phần `DOMAIN ENTITIES` — `BookingCalculationDTO` nếu cần chỉ nhanh payload.)**

Request vào **`BookingServiceImpl.calculatePrice`**. Payload là **`BookingCalculationDTO`**: suất chiếu, danh sách ghế, có thể có mã khuyến mãi — đây là “hợp đồng” client gửi lên. Trên sơ đồ, `BookingServiceImpl` nằm ở nhánh điều phối; các mũi tên orchestrator sau này sẽ lần lượt là validate, dựng context, rồi gọi engine — em sẽ đi đúng thứ tự đó.

Service tạo **`PricingValidationContext`** bọc request; lúc đầu chỉ có request, chưa có đủ thực thể để tính giá.

---

### Bước 2 — Kiểm tra hợp lệ: Chain of Responsibility

**(Trên UML: `CHAIN OF RESPONSIBILITY — pricing validation` — `PricingValidationHandler`, `AbstractPricingValidationHandler`, các handler, `PricingValidationContext`.)**

Bước đầu tiên trong điều phối là **`pricingValidationChain.validate`**. Đây là **Chain of Responsibility**: thay vì một hàm if lồng nhau, ta nối các bước **`ShowtimeFutureHandler` → `SeatsAvailableHandler` → `PromoValidHandler`**. Mỗi handler chỉ làm một việc; lỗi thì dừng. **`AbstractPricingValidationHandler`** giữ `next` và gọi tiếp chuỗi sau `doValidate`.

Sau bước này, context validate đã có **`showtime`** (và thông tin promo cơ bản nếu qua bước promo) — trên UML có ghi: showtime được load sớm để **tái dùng** ở bước sau, tránh đọc DB lặp.

---

### Bước 3 — Làm mới khuyến mãi (nghiệp vụ, không phải GoF riêng)

**(Trên UML: không có lớp riêng; có thể trỏ lại `BookingServiceImpl`.)**

Nếu có `promoCode`, service gọi **`resolvePromotionForPricing`** và ghi lại **`promotion`** vào context **ngay trước** khi tính giá — đảm bảo đúng tồn kho / trạng thái mã tại thời điểm đó. Đây là chi tiết trong `BookingServiceImpl`, không tách thành pattern riêng trên sơ đồ.

---

### Bước 4 — Dựng ngữ cảnh tính giá: entity và `PricingContext`

**(Trên UML: `ENGINE CONTEXT` — `PricingContext`, `ResolvedFnbItem`; nhìn lên `DOMAIN ENTITIES` khi nói từng loại đối tượng.)**

Tiếp theo là **`PricingContextBuilder.build`**: lấy **`showtime`** và **`promotion`** đã có từ context validate, nạp **`Seat`** theo id, resolve F&B thành **`ResolvedFnbItem`**, lấy **`Customer`** / **`MembershipTier`** nếu đăng nhập, tính số ghế đã bán / tổng ghế phòng, gắn **`bookingTime`**. Kết quả là **`PricingContext`** — đủ dữ liệu để engine tính, **không** còn là lớp validate nữa.

Khi nói miền dữ liệu, ta có thể chỉ nhanh trên UML: **`Showtime`**, **`Seat`/`SeatType`**, **`Promotion`**, **`FnbItem`**, **`Customer`/`MembershipTier`** — nhưng **trong kịch bản** chúng xuất hiện **vì builder cần chúng**, không phải mục “liệt kê domain” tách biệt.

---

### Bước 5 — Gọi engine qua interface: Proxy (cache Redis)

**(Trên UML: `PROXY — Redis cache` — `IPricingEngine`, `CachingPricingEngineProxy`; quan hệ delegate tới `PricingEngine`.)**

Service gọi **`IPricingEngine.calculateTotalPrice(context)`**. Spring inject **`CachingPricingEngineProxy`** (`@Primary`): đây là **Proxy** — cùng interface với engine thật, nhưng **chèn thêm** tra Redis: trúng key thì trả **`PriceBreakdownDTO`** luôi; trượt thì **ủy quyền** cho **`PricingEngine`**. Logic giá không nằm trong proxy, chỉ cache và delegate.

---

### Bước 6 — Bên trong engine: Strategy cho từng dòng tiền

**(Trên UML: `STRATEGY — pricing` — `PricingLineType`, `PricingStrategy`, ba strategy, `PricingEngine` với `EnumMap`.)**

`PricingEngine` dùng **Strategy**: mỗi **`PricingLineType`** (TICKET, FNB, TIME_BASED_SURCHARGE) có một **`PricingStrategy`**; engine gọi lần lượt rồi cộng thành phần tiền trước giảm giá. **Không** nhét ba công thức vào một method dài — thêm loại dòng tiền mới thì thêm strategy.

---

### Bước 6b — Cùng lúc strategy “thời điểm”: Specification

**(Trên UML: `SPECIFICATION — PricingConditions`, `PricingSpecificationContext`, liên kết `TimeBasedPricingStrategy`.)**

Khi engine chạy dòng **`TIME_BASED_SURCHARGE`**, **`TimeBasedPricingStrategy`** đưa **`PricingContext`** sang **`PricingSpecificationContext`** và dùng **`PricingConditions`** (predicate: cuối tuần, ngày lễ, early bird, lấp chỗ cao, …). Đây là **Specification** — điều kiện có tên, dễ test và ghép nối. Trên luồng nó **không đứng riêng trước Strategy**: nó là **cách strategy phụ thu thời điểm** quyết định có áp mức phụ thu hay không.

---

### Bước 7 — Sau subtotal: Decorator cho chuỗi giảm giá

**(Trên UML: `DECORATOR — discount chain` — `DiscountComponent`, `NoDiscount`, decorator promotion/member, `DiscountResult`.)**

Sau khi có subtotal (vé + F&B + phụ thu thời điểm), engine xây chuỗi **`DiscountComponent`**: thường từ **`NoDiscount`**, bọc **`PromotionDiscountDecorator`** nếu có promotion, bọc **`MemberDiscountDecorator`** nếu có hạng thành viên — **Decorator** xếp lớp trên cùng một số tiền. **`DiscountResult`** tách rõ từng loại giảm để đổ vào DTO.

---

### Bước 8 — Kết quả trả về

**(Trên UML: `PriceBreakdownDTO`; trỏ lại `BookingServiceImpl` như điểm ra.)**

Engine (sau proxy) trả **`PriceBreakdownDTO`**: các thành phần tiền, giảm, tổng cuối — đó là đầu ra của cả luồng (trên class diagram UML hiện chỉ thể hiện các khoản tiền; trong code có thêm field `appliedStrategy` nếu cần nói khi demo Phần B).

---

### Kết luận (khoảng 30 giây)

Vừa rồi là **một đường đi** từ request tới response: validate bằng **Chain of Responsibility**, dựng context, qua **Proxy** cache, vào **Strategy** (trong đó nhánh thời điểm dùng **Specification**), rồi **Decorator** giảm giá. Năm pattern trên UML **không năm rời rạc** — chúng **nối tiếp nhau đúng thứ tự chạy** trong hệ thống.

---

## Phụ lục: Lộ trình zoom trên UML (theo luồng runtime)

Thứ tự gợi ý khi laser / zoom **trùng với thứ tự kịch bản Pha A**, không nhảy lên domain rồi mới xuống orchestrator:

| Bước | Vùng trên sơ đồ | Ghi chú |
|------|------------------|---------|
| 1 | `ORCHESTRATOR` — `BookingServiceImpl` | Điểm vào; mũi tên tới validate / builder / engine |
| 2 | `CHAIN OF RESPONSIBILITY — pricing validation` | CoR |
| 3 | `ENGINE CONTEXT` + `DOMAIN ENTITIES` (khi cần) | `PricingContext`, entity load trong builder |
| 4 | `PROXY — Redis cache` | `IPricingEngine`, proxy, delegate |
| 5 | `STRATEGY — pricing` | `PricingEngine`, `EnumMap`, ba strategy |
| 6 | `SPECIFICATION — PricingConditions` | Zoom khi nói bước 6b; `TimeBasedPricingStrategy` |
| 7 | `DECORATOR — discount chain` | Chuỗi giảm |
| 8 | `PriceBreakdownDTO` + lại `BookingServiceImpl` | Điểm ra |

Chi tiết mở file IDE: [Phần B](#phần-b--sau-khi-thuyết-trình-uml-chỉ-đường-file--vị-trí).

---

## Phần B — Sau khi thuyết trình UML: chỉ đường file & vị trí

Thứ tự **khớp luồng runtime** (cùng với kịch bản Pha A). **Không** dán code lên slide; trong IDE nhảy tới method hoặc khoảng dòng gợi ý.

| Thứ tự | Bước luồng / pattern | File (từ gốc repo) | Mở tới đâu (gợi ý) |
|--------|------------------------|---------------------|---------------------|
| 1 | Điều phối + re-resolve promo | `backend/src/main/java/com/cinema/booking/services/impl/BookingServiceImpl.java` | `calculatePrice` (~134–148) |
| 2 | Chain of Responsibility | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/validation/AbstractPricingValidationHandler.java` | `validate` (~15–21) |
| 2b | Nối chain | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/validation/PricingValidationConfig.java` | Bean `pricingValidationChain` (~19–28) |
| 2c | (tuỳ chọn) Handler | `.../ShowtimeFutureHandler.java`, `SeatsAvailableHandler.java`, `PromoValidHandler.java` | `doValidate` |
| 3 | Dựng `PricingContext` | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/PricingContextBuilder.java` | `build` (~36–55+) |
| 4 | Proxy Redis | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/CachingPricingEngineProxy.java` | `calculateTotalPrice`; tuỳ chọn `buildCacheKey` |
| 5 | Strategy + orchestrator | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/PricingEngine.java` | constructor `EnumMap` (~30–43); `calculateTotalPrice` ba strategy (~46–49) |
| 6 | Specification (trong nhánh time-based) | `backend/src/main/java/com/cinema/booking/patterns/specification/PricingConditions.java` | `isWeekend`, `isHoliday`, … |
| 6b | TimeBased strategy | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/TimeBasedPricingStrategy.java` | `calculate`, `toSpecContext` |
| 7 | Decorator | `backend/src/main/java/com/cinema/booking/services/strategy_decorator/pricing/PricingEngine.java` | `applyDiscount` (~52–53); `buildDiscountChain` (~77–88) |
| 8 | Domain / DTO (tuỳ chọn) | `backend/src/main/java/com/cinema/booking/entities/`, `.../dtos/BookingCalculationDTO.java`, `.../dtos/PriceBreakdownDTO.java` | Field khớp UML |

**Mẹo Pha B**: Đi từ hàng 1 tới 7 như một stack trace nghiệp vụ — trùng với thứ tự đã nói trên sơ đồ.

Danh sách file đầy đủ: [../docs/patterns/08-dynamic-pricing-engine.md](../docs/patterns/08-dynamic-pricing-engine.md).

---

*Tài liệu này không thay thế giải thích từng field trong [pattern-only/08-dynamic-pricing-engine-giai-thich-class.md](pattern-only/08-dynamic-pricing-engine-giai-thich-class.md); dùng khi cần tra chi tiết lớp.*
