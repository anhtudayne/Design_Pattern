# Kịch bản báo cáo — Dynamic Pricing Engine (UML pattern only)

> **Sơ đồ tham chiếu:** [UML/pattern-only/08-dynamic-pricing-engine.md](../../UML/pattern-only/08-dynamic-pricing-engine.md)  
> **Tài liệu chi tiết:** [08-dynamic-pricing-engine.md](08-dynamic-pricing-engine.md) · [08-dynamic-pricing-engine-report-bo-cuc-mau.md](08-dynamic-pricing-engine-report-bo-cuc-mau.md)

**Cách dùng:** Bài trình bày bám **luồng tính giá trong code** — khớp `BookingServiceImpl.calculatePrice`: validate → dựng context → gọi engine (qua proxy). Các lớp nội bộ engine (strategy, specification, decorator) là **bước con** sau khi đã vào `PricingEngine`. Mỗi bước có **Lời thoại** và **Ghi chú**.

---

## Giới thiệu ngắn

**Lời thoại:** Dynamic Pricing Engine phục vụ **xem giá trước thanh toán**: client gửi DTO mô tả suất chiếu, ghế, F&B, mã khuyến mãi; hệ thống trả DTO breakdown. Trên sơ đồ pattern only, các pattern xếp thành pipeline; em trình theo **thứ tự thực thi** trong service, rồi liên hệ lại từng khối trên UML.

**Ghi chú:** Hành động: mở [UML/pattern-only/08-dynamic-pricing-engine.md](../../UML/pattern-only/08-dynamic-pricing-engine.md), nhìn khối DTO và `BookingServiceImpl` trước. Nói ngoài: API HTTP có thể hỏi thêm — không bắt buộc trên class diagram.

---

## Luồng chính — `BookingServiceImpl.calculatePrice`

### Bước 1 — Nhận `BookingCalculationDTO` và tạo `PricingValidationContext`

**Lời thoại:** Điểm vào là `BookingServiceImpl.calculatePrice`: tham số là `BookingCalculationDTO` chứa thông tin người dùng chọn. Service tạo `PricingValidationContext` ban đầu, gắn request vào — đây là “bao tải” sẽ được các bước validate bổ sung dần suất chiếu, promotion, v.v.

**Ghi chú:** Hành động: trên UML, chỉ `BookingCalculationDTO`, `BookingServiceImpl`, `PricingValidationContext`. Nói ngoài: [`BookingServiceImpl.java`](../../backend/src/main/java/com/cinema/booking/service/impl/BookingServiceImpl.java) dòng ~139–147.

### Bước 2 — Chuỗi `PricingValidationHandler` (Chain of Responsibility)

**Lời thoại:** Service gọi `pricingValidationChain.validate(validationCtx)`. Chuỗi handler lần lượt kiểm tra nghiệp vụ — ví dụ suất chiếu còn hợp lệ, ghế còn chỗ, mã khuyến mãi hợp lệ. Mỗi handler chỉ lo một phần; sai thì dừng với lỗi. Context sau bước này đủ để bước sau **không truy vấn lặp** không cần thiết.

**Ghi chú:** Hành động: chỉ cụm CoR — `PricingValidationHandler`, `AbstractPricingValidationHandler`, context, các handler cụ thể, mũi tên `next`. Nói ngoài: bean `pricingValidationChain` trong Spring; package `com.cinema.booking.pattern.chain`.

### Bước 3 — `PricingContextBuilder.build` → `PricingContext`

**Lời thoại:** Sau khi validate thành công, `PricingContextBuilder` đọc `validationCtx` cùng request gốc và **dựng** `PricingContext` — gói dữ liệu đã chuẩn cho tầng tính tiền: showtime, ghế, F&B, promotion, khách, occupancy, thời điểm đặt. Đây là ranh giới rõ giữa “đã kiểm tra và enrich” và “công thức tính”.

**Ghi chú:** Hành động: chỉ `PricingContextBuilder`, `PricingContext`, mũi tên từ service/builder như trên UML. Nói ngoài: class builder trong package strategy pricing (theo import trong `BookingServiceImpl`).

### Bước 4 — `IPricingEngine.calculateTotalPrice` — entry qua `CachingPricingEngineProxy`

**Lời thoại:** Service gọi `pricingEngine.calculateTotalPrice(pricingContext)`. Kiểu field là `IPricingEngine` nhưng bean inject là **proxy** `CachingPricingEngineProxy`. Proxy có thể trả kết quả từ **Redis** nếu trùng khóa; nếu không có cache thì chuyển tiếp cho engine thật. Caller không phân nhánh cache thủ công.

**Ghi chú:** Hành động: chỉ `IPricingEngine`, `CachingPricingEngineProxy`, `delegate`, nhánh tới `PricingEngine`. Nói ngoài: `@Qualifier("cachingPricingEngineProxy")` trong `BookingServiceImpl`; [`CachingPricingEngineProxy.java`](../../backend/src/main/java/com/cinema/booking/pattern/proxy/CachingPricingEngineProxy.java).

---

## Luồng con — bên trong `PricingEngine` (sau cache miss hoặc không dùng cache)

### Bước 5.1 — Strategy theo `PricingLineType` (vé, F&B, phụ phí thời gian)

**Lời thoại:** Engine tra `EnumMap` từng loại dòng tiền — vé, F&B, phụ phí theo thời gian — và gọi đúng `PricingStrategy.calculate` cho từng phần. Constructor của engine **bắt buộc đủ ba strategy**; thiếu cấu hình thì lỗi ngay lúc khởi động, tránh sai tiền lúc chạy.

**Ghi chú:** Hành động: chỉ `PricingEngine`, `PricingLineType`, `PricingStrategy`, ba lớp strategy. Nói ngoài: [`PricingEngine.java`](../../backend/src/main/java/com/cinema/booking/pattern/decorator/PricingEngine.java).

### Bước 5.2 — `TimeBasedPricingStrategy` và `PricingConditions` (Specification)

**Lời thoại:** Phần phụ phí thời gian không viết if-else dài trong một method; strategy chuyển context sang dạng specification và dùng các **predicate** tập trung trong `PricingConditions` — cuối tuần, ngày lễ, early bird, tỷ lệ lấp ghế, v.v. Đúng vai trò Specification: rule đọc được, tách khỏi công thức nhân hệ số.

**Ghi chú:** Hành động: chỉ `TimeBasedPricingStrategy`, `PricingSpecificationContext`, `PricingConditions` và mũi tên liên quan trên UML. Nói ngoài: package `com.cinema.booking.pattern.specification`.

### Bước 5.3 — Chuỗi `DiscountComponent` (Decorator)

**Lời thoại:** Sau khi có các dòng tiền, phần giảm giá được áp qua **chuỗi decorator** quanh `NoDiscount`: có thể thêm lớp khuyến mãi, lớp hạng thành viên, mỗi lớp bọc bên trong và chỉnh subtotal lần lượt. Validate promo đã nằm ở CoR phía trước; bước này **chỉ tính chiết khấu**.

**Ghi chú:** Hành động: chỉ `DiscountComponent`, `NoDiscount`, `BaseDiscountDecorator`, `PromotionDiscountDecorator`, `MemberDiscountDecorator`. Nói ngoài: `PriceBreakdownDTO` không có field `promotionDiscount` riêng — `discountAmount` là tổng giảm; chi tiết trong `DiscountResult` nội bộ (theo mở đầu file UML).

### Bước 6 — Trả `PriceBreakdownDTO` ngược lên caller

**Lời thoại:** Engine (hoặc proxy trả từ cache) trả về `PriceBreakdownDTO` với đủ các cột tiền và tổng cuối. Luồng hoàn tất: **context validate → context tính → engine → DTO**; proxy chỉ là lớp bọc trong bước gọi engine.

**Ghi chú:** Hành động: chỉ `PriceBreakdownDTO` và đường ngược từ engine về service (ý luồng dữ liệu). Nói ngoài: có thể nhắc lại bean `pricingEngine` là interface, implementation thật là `PricingEngine` sau proxy.

---

## Một câu tóm pattern theo thứ tự luồng

**Lời thoại:** Đọc nhanh theo thứ tự vừa trình: **CoR** là cửa ải; **Builder context** chuẩn hóa đầu vào engine; **Proxy** tối ưu lặp lại; bên trong engine có **Strategy**, **Specification** cho phụ phí, **Decorator** cho giảm giá; **DTO** khóa hợp đồng hai đầu.

**Ghi chú:** Hành động: panorama toàn diagram. Nói ngoài: không đọc lại từng tên lớp.

---

## Kết (~30 giây)

**Lời thoại:** Như vậy luồng chức năng tính giá là một **pipeline có thứ tự** trong `BookingServiceImpl`, còn sơ đồ UML pattern only cho thấy **lớp nào đảm nhiệm từng đoạn** của pipeline đó. Mở rộng sau: thêm handler validate, thêm predicate, thêm decorator hoặc điều chỉnh TTL cache mà ít phải sửa một method khổng lồ.

**Ghi chú:** Nói ngoài — kết luận.

---

## Thời lượng (tham khảo)

**Lời thoại:** _(Không đọc.)_

**Ghi chú:** Nói ngoài — ~4–5 phút nếu chỉ Bước 1–4 + tóm tắt Bước 5; ~7–8 phút nếu đi đủ Bước 5.1–5.3 chi tiết.
