# Báo Cáo Kỹ Thuật: Dynamic Pricing Engine

> **Tính năng:** Tính giá vé động  
> **Endpoint:** `POST /api/booking/calculate`  
> **Design Pattern áp dụng:** Chain of Responsibility · Proxy · Specification · Strategy · Decorator

---

## Mục Lục

1. [Mô tả bài toán](#1-mô-tả-bài-toán)
2. [Vấn đề của cách làm ban đầu](#2-vấn-đề-của-cách-làm-ban-đầu)
3. [Giải pháp tổng thể — Tại sao cần nhiều pattern](#3-giải-pháp-tổng-thể--tại-sao-cần-nhiều-pattern)
4. [Chain of Responsibility — Tầng Validation](#4-chain-of-responsibility--tầng-validation)
5. [Proxy — Tầng Cache](#5-proxy--tầng-cache)
6. [Specification — Tầng Điều Kiện Nghiệp Vụ](#6-specification--tầng-điều-kiện-nghiệp-vụ)
7. [Strategy — Tầng Tính Giá](#7-strategy--tầng-tính-giá)
8. [Decorator — Tầng Giảm Giá](#8-decorator--tầng-giảm-giá)
9. [Ưu điểm tổng hợp sau khi áp dụng](#9-ưu-điểm-tổng-hợp-sau-khi-áp-dụng)
10. [So sánh trước và sau](#10-so-sánh-trước-và-sau)

---

## 1. Mô Tả Bài Toán

### Bối cảnh

StarCine là hệ thống đặt vé xem phim trực tuyến. Trong luồng đặt vé, trước khi xác nhận thanh toán, khách hàng cần xem được **bảng giá chi tiết**: tiền vé, tiền đồ ăn, phụ phí nếu là ngày đặc biệt, số tiền được giảm, và tổng cuối cùng phải trả. Tính năng này được gọi qua endpoint `POST /api/booking/calculate`.

### Yêu cầu nghiệp vụ

Hệ thống cần xử lý đồng thời nhiều quy tắc tính giá phức tạp:

**Về tính tiền:** Giá vé được tính dựa trên giá cơ bản của suất chiếu cộng với phụ phí theo loại ghế — ghế VIP và Couple có giá cao hơn Standard. Tiền đồ ăn được tính theo đơn giá từng món nhân với số lượng đặt. Ngoài ra, nếu suất chiếu rơi vào cuối tuần hoặc ngày lễ quốc gia, hệ thống áp thêm phụ phí thời gian lên phần tiền vé.

**Về giảm giá:** Khách hàng có thể được hưởng hai loại ưu đãi cùng lúc — giảm giá theo hạng thành viên (Bronze, Silver, Gold, Platinum) và giảm giá theo mã khuyến mãi. Mã khuyến mãi có thể là giảm theo phần trăm hoặc giảm số tiền cố định, đồng thời có ngày hết hạn và giới hạn số lượt dùng.

**Về validate:** Trước khi tính giá, hệ thống phải kiểm tra xem suất chiếu còn chưa chiếu không, các ghế được chọn còn trống không, và mã khuyến mãi (nếu có) còn hợp lệ không.

**Về hiệu năng:** Khách hàng thường xem giá nhiều lần trong cùng một phiên đặt vé — đổi ghế, thêm bớt đồ ăn, thử các mã promo khác nhau. Hệ thống cần tránh tính toán lại từ đầu mỗi lần cho cùng một bộ dữ liệu.

---

## 2. Vấn Đề Của Cách Làm Ban Đầu

Phiên bản đầu tiên (MVP) xử lý toàn bộ yêu cầu trên trong **một method duy nhất** của `BookingServiceImpl`. Method này lần lượt kiểm tra showtime, kiểm tra từng ghế, kiểm tra mã promo, rồi tính tiền vé, tính tiền F&B, tính phụ phí, và cuối cùng tính giảm giá — tất cả nối tiếp nhau trong khoảng 80–100 dòng code.

Cách làm này dẫn đến một loạt vấn đề nghiêm trọng:

**Quá nhiều trách nhiệm trong một chỗ.** Method vừa phải validate, vừa phải truy vấn cơ sở dữ liệu, vừa phải tính toán — vi phạm nguyên tắc Single Responsibility. Hậu quả là không thể kiểm thử từng phần riêng lẻ mà phải mock toàn bộ service.

**Khó mở rộng.** Mỗi lần thêm một quy tắc mới — ví dụ giới hạn số ghế tối đa, hoặc thêm loại phụ phí mới — đều phải mở method ra sửa trực tiếp. Điều này vi phạm nguyên tắc Open/Closed và tăng nguy cơ phá vỡ các tính năng đang hoạt động tốt.

**Không có cache.** Mỗi lần khách hàng nhấn "xem giá" đều kéo theo một loạt truy vấn cơ sở dữ liệu từ đầu, kể cả khi dữ liệu đầu vào không thay đổi gì so với lần trước.

**Điều kiện nghiệp vụ bị phân tán và dễ sót.** Điều kiện "cuối tuần" được hardcode trực tiếp vào method tính giá, nhưng điều kiện "ngày lễ" lại bị bỏ quên. Không có nơi tập trung để quản lý các quy tắc kinh doanh này.

**Logic giảm giá sai tầng.** Phần tính promo discount nằm ngay cạnh phần validate promo — hai việc khác nhau về mặt trách nhiệm nhưng bị đặt chung chỗ. Và khoản giảm giá theo hạng thành viên hoàn toàn bị bỏ sót, tạo ra lỗi nghiệp vụ.

**N+1 query problem.** Để lấy giá từng món đồ ăn, code cũ gọi vào cơ sở dữ liệu trong một vòng lặp — nghĩa là 5 món F&B sẽ tạo ra 5 truy vấn riêng biệt thay vì 1 truy vấn duy nhất.

---

## 3. Giải Pháp Tổng Thể — Tại Sao Cần Nhiều Pattern

Thay vì chỉ tái cấu trúc method cũ cho gọn hơn, nhóm quyết định phân tách hệ thống thành các **tầng trách nhiệm rõ ràng**, mỗi tầng giải quyết đúng một vấn đề cụ thể:

```
Yêu cầu từ client (BookingCalculationDTO)
        │
        ▼
[Tầng 1] Chain of Responsibility  →  Validate tất cả điều kiện, thu thập dữ liệu dùng lại
        │                               ⚠️ Thứ tự cố định: Showtime FIRST → Seats → Promo
        ▼
[Tầng 1b] PricingContextBuilder   →  Map BookingCalculationDTO + CoR result → PricingContext
        │                               (bước mapping tường minh, không bị ẩn trong BookingServiceImpl)
        ▼
[Tầng 2] Proxy (Redis cache)      →  Trả về từ cache nếu đã tính rồi
        │ (cache miss)                   📌 seatIds sort tăng dần → tránh cache key collision
        ▼
[Tầng 3] Strategy                 →  Tính tiền vé / F&B / timeBasedSurcharge
        │ (trong Strategy dùng)          💡 PricingEngine inject List<PricingStrategy> — OCP compliant
        ├── Specification          →  Đánh giá điều kiện cuối tuần / ngày lễ
        │
        ▼
[Tầng 4] Decorator                →  Áp dụng các khoản giảm giá tích lũy
        │
        ▼
PriceBreakdownDTO (7 trường chi tiết)
```

Mỗi pattern được chọn vì nó giải quyết **đúng loại vấn đề** mà tầng đó gặp phải — không áp dụng pattern cho có, mà áp dụng vì bài toán đòi hỏi.

---

## 4. Chain of Responsibility — Tầng Validation

### Vấn đề cụ thể

Trong MVP, toàn bộ logic kiểm tra điều kiện đầu vào được viết tuần tự trong cùng một method. Điều này có nghĩa là mỗi lần thêm một quy tắc kiểm tra mới — chẳng hạn giới hạn không được đặt quá 8 ghế, hoặc kiểm tra người dùng có bị khóa tài khoản không — đều phải mở method ra sửa, làm tăng rủi ro cho các đoạn code đã ổn định. Ngoài ra, sau khi validate xong, dữ liệu showtime và promotion phải được tải lại từ cơ sở dữ liệu một lần nữa để tính giá — hoàn toàn lãng phí.

### Tại sao chọn Chain of Responsibility

Chain of Responsibility là pattern phù hợp khi có một chuỗi các bước xử lý tuần tự, mỗi bước độc lập với nhau và có thể được thêm vào hoặc bỏ ra mà không ảnh hưởng đến các bước còn lại. Trong trường hợp này, mỗi quy tắc validate là một mắt xích trong chuỗi — khi một mắt xích phát hiện vi phạm, nó ném exception ngay lập tức (fail-fast) và các mắt xích phía sau không cần chạy nữa.

### Cách hoạt động sau khi áp dụng

Chuỗi validation bao gồm ba handler được nối tiếp nhau: `ShowtimeFutureHandler` → `SeatsAvailableHandler` → `PromoValidHandler`.

Handler đầu tiên kiểm tra suất chiếu có tồn tại không và thời gian bắt đầu có còn trong tương lai không. Nếu hợp lệ, nó lưu đối tượng Showtime vào context — `PricingContextBuilder` tái sử dụng entity này khi dựng `PricingContext` cho engine. Handler thứ hai kiểm tra từng ghế được chọn xem đã có vé bán chưa (dùng `showtimeId` từ request). Handler cuối cùng kiểm tra mã khuyến mãi — còn hạn không, còn số lượt dùng không — và nếu hợp lệ thì lưu đối tượng Promotion vào context.

Sau khi chain chạy xong, `PricingContextBuilder.build(...)` lấy `showtime` và `promotion` từ validation context, rồi bổ sung entity ghế, F&B đã resolve đơn giá, occupancy và customer — không cần truy vấn lại showtime/promotion cho mục đích validate.

> **⚠️ Ordering dependency — không được đảo thứ tự:** `ShowtimeFutureHandler` **phải chạy đầu tiên** để fail-fast khi suất chiếu không hợp lệ và để populate `ctx.showtime` cho builder. `PromoValidHandler` trong code hiện tại chỉ đọc `promoCode` từ request và tra `PromotionRepository`, **không** đọc `ctx.showtime`. Thứ tự vẫn được cố định trong `PricingValidationConfig` như một contract rõ ràng của chuỗi.

### Ưu điểm đạt được

Mỗi handler chỉ làm một việc duy nhất, có thể được kiểm thử riêng lẻ mà không cần khởi động toàn bộ Spring context. Khi cần thêm quy tắc mới, chỉ cần tạo thêm một class handler và đăng ký vào chuỗi trong file cấu hình — không cần chạm vào bất kỳ handler nào đang hoạt động. Thứ tự xử lý được kiểm soát tường minh trong một class `@Configuration` duy nhất, dễ đọc và dễ điều chỉnh.

---

## 5. Proxy — Tầng Cache

### Vấn đề cụ thể

Trong thực tế, người dùng thường xem giá nhiều lần trong cùng một phiên đặt vé trước khi quyết định thanh toán — đổi ghế từ Standard sang VIP, thêm bớt món đồ ăn, thử các mã promo khác nhau. Mỗi lần như vậy, hệ thống phải thực hiện toàn bộ quá trình tính giá từ đầu, kể cả khi input không thay đổi gì so với lần trước. Điều này gây lãng phí tài nguyên và tăng độ trễ không cần thiết.

### Tại sao chọn Proxy

Proxy Pattern giải quyết đúng vấn đề này. Một đối tượng Proxy đứng trước đối tượng thật, chặn mọi lời gọi và quyết định xem có cần chuyển tiếp xuống đối tượng thật hay không. Điểm mấu chốt là Proxy implement cùng interface với đối tượng thật — vì vậy caller không cần biết mình đang nói chuyện với proxy hay engine thật, mọi thứ hoàn toàn trong suốt.

### Cách hoạt động sau khi áp dụng

`CachingPricingEngineProxy` và `PricingEngine` cùng implement interface `IPricingEngine`. Khi `BookingServiceImpl` gọi `calculateTotalPrice()`, nó luôn nói chuyện qua interface này. Spring tự động inject proxy vào caller nhờ annotation `@Primary` — không cần thay đổi gì ở phía caller.

Proxy xây dựng một cache key duy nhất từ: mã suất chiếu, danh sách ghế (sort theo `seatId`, hoặc `seats:none`), giỏ F&B (sort theo `itemId`, các cặp `itemId:qty` nối bằng dấu phẩy, hoặc `fnb:none`), mã promo, và khách hàng (`cust:{userId}` hoặc `cust:anon`). Nếu Redis đã có kết quả cho combination này, proxy trả về ngay lập tức mà không cần tính gì thêm. Nếu chưa có (cache miss), proxy gọi xuống `PricingEngine` thật, nhận kết quả, lưu vào Redis với thời gian sống 600 giây, rồi trả về kết quả cho caller.

> **📌 Cache key collision prevention:** `seatIds` và các cặp F&B được **sort trước khi nối**, đảm bảo cùng một lựa chọn (khác thứ tự nhập) vẫn trùng key. Nếu không sort, sẽ có cache miss giả và entry trùng nội dung.

**Format:** `pricing:{showtimeId}:seats:{id1,id2,...}|seats:none:fnb:{itemId:qty,...}|fnb:none:promo:{code}|promo:none:cust:{userId}|cust:anon`

Giá trị tính giá là một snapshot tại thời điểm tính — nó không thay đổi trừ khi dữ liệu đầu vào thay đổi. Vì vậy không cần cơ chế invalidate thủ công; cache tự hết hạn sau 600 giây là đủ an toàn.

### Ưu điểm đạt được

Độ trễ giảm từ hàng trăm millisecond xuống dưới 5ms khi cache hit. `PricingEngine` không bị ảnh hưởng gì — nó không biết mình đang được bao bởi proxy. Nếu sau này cần tắt cache hoặc thay bằng cơ chế cache khác, chỉ cần thay đổi `@Primary` mà không cần sửa engine hay caller.

---

## 6. Specification — Tầng Điều Kiện Nghiệp Vụ

### Vấn đề cụ thể

Các điều kiện kinh doanh như "cuối tuần", "ngày lễ quốc gia", "đặt sớm hơn 3 ngày", "tỷ lệ lấp đầy trên 80%" là các quy tắc nghiệp vụ thuần túy — chúng không phụ thuộc vào database hay bất kỳ infrastructure nào. Tuy nhiên trong MVP, những điều kiện này bị hardcode trực tiếp vào phần tính giá, gây ra hai hậu quả: điều kiện "ngày lễ" bị bỏ sót, và không có cách nào kiểm thử những quy tắc này một cách độc lập.

### Tại sao chọn Specification

Specification Pattern đóng gói mỗi điều kiện nghiệp vụ thành một đơn vị độc lập, có thể kiểm thử riêng và kết hợp tự do với nhau. Trong Java hiện đại, điều này được thể hiện qua `Predicate` — một hàm nhận vào context và trả về true/false. Điều quan trọng là các predicate này có thể được ghép lại bằng `.and()`, `.or()`, `.negate()` để tạo ra các điều kiện phức hợp mà không cần viết thêm code mới.

### Cách hoạt động sau khi áp dụng

Toàn bộ điều kiện nghiệp vụ liên quan đến giá vé được tập trung trong class `PricingConditions`. Class này cung cấp bốn predicate: kiểm tra cuối tuần, kiểm tra ngày lễ (với danh sách ngày lễ Việt Nam được khai báo tập trung), kiểm tra đặt vé sớm ít nhất 3 ngày, và kiểm tra tỷ lệ lấp đầy vượt ngưỡng.

`TimeBasedPricingStrategy` sử dụng hai predicate `isWeekend()` và `isHoliday()` để quyết định có áp phụ phí hay không, và nếu có thì chọn rate nào — holiday rate 20% được ưu tiên hơn weekend rate 15% khi cả hai điều kiện cùng thỏa mãn. Dữ liệu đầu vào cho các predicate được chuyển đổi từ `PricingContext` của tầng engine sang `PricingSpecificationContext` của tầng specification qua private method `toSpecContext()` — hai class khác nhau nhưng chứa cùng loại thông tin, giúp tách biệt hai tầng mà vẫn chia sẻ được dữ liệu.

> **📌 Hai context, hai tầng:** Chỉ `TimeBasedPricingStrategy` thực hiện bước convert này. `TicketPricingStrategy` và `FnbPricingStrategy` đọc thẳng từ `PricingContext` (engine layer) mà không qua Specification — vì chúng không cần đánh giá điều kiện nghiệp vụ theo kiểu predicate.

> **📌 `PricingConditions` static vs injectable:** Hiện tại class này dùng `static` method để tiện dùng như utility. Nếu sau này cần inject `HolidayCalendarService` (ví dụ lấy ngày lễ từ DB hoặc external API), class này sẽ cần refactor thành `@Component`. Nên cân nhắc kiến trúc này trước khi mở rộng.

### Ưu điểm đạt được

Toàn bộ 9 test case cho tầng Specification (`PricingConditionsTest`) chạy trong chưa đầy 1 giây mà không cần kết nối database hay Redis. Danh sách ngày lễ được quản lý tập trung, không còn nguy cơ bỏ sót. Khi cần thêm điều kiện mới (ví dụ: giờ cao điểm, mùa lễ hội), chỉ cần thêm một method vào `PricingConditions` mà không cần sửa bất kỳ strategy nào.

---

## 7. Strategy — Tầng Tính Giá

### Vấn đề cụ thể

Có ba loại tính giá hoàn toàn khác nhau về công thức và dữ liệu cần dùng: tính tiền vé theo ghế và suất chiếu, tính tiền F&B theo đơn giá và số lượng, tính phụ phí thời gian theo điều kiện ngày. Trong MVP, cả ba công thức này được nhồi chung vào một method. Ngoài ra, phần tính F&B còn truy vấn cơ sở dữ liệu trong một vòng lặp, tạo ra vấn đề N+1 query.

### Tại sao chọn Strategy

Strategy Pattern phù hợp khi có nhiều thuật toán khác nhau cho cùng một tác vụ và cần có khả năng hoán đổi hoặc mở rộng mà không ảnh hưởng đến phần điều phối. Thay vì để `PricingEngine` biết chi tiết từng công thức tính giá, nó chỉ cần gọi `strategy.calculate(context)` và nhận về kết quả — không quan tâm cách tính.

### Cách hoạt động sau khi áp dụng

Ba strategy được tạo ra, mỗi cái implement interface `PricingStrategy` với một method duy nhất `calculate(PricingContext)`.

`TicketPricingStrategy` tính tổng tiền vé bằng cách lấy giá cơ bản của suất chiếu cộng với phụ phí loại ghế, nhân với số lượng ghế được chọn. Công thức này thuần túy về mặt tính toán, không cần gọi database.

`FnbPricingStrategy` tính tổng tiền F&B từ danh sách `ResolvedFnbItem` — đây là các món đồ ăn đã được `PricingContextBuilder` load giá từ database **một lần duy nhất** trước khi vào engine. Nhờ vậy, strategy này hoàn toàn không cần truy cập database, giải quyết dứt điểm vấn đề N+1 query.

`TimeBasedPricingStrategy` tính **`timeBasedSurcharge`** (phụ phí thời gian) bằng cách chuyển context sang định dạng Specification rồi đánh giá điều kiện qua `PricingConditions` (xem phần 6). Nếu là ngày lễ hoặc cuối tuần, phụ phí được tính trên phần tiền vé với rate được cấu hình trong `application.properties` — thay đổi rate không cần biên dịch lại. **Đây là strategy duy nhất sản xuất ra field `timeBasedSurcharge` trong `PriceBreakdownDTO`.**

> **💡 OCP — `List<PricingStrategy>` + `PricingLineType`:** `PricingEngine` nhận `List<PricingStrategy>`, gom vào `EnumMap` theo `lineType()`. Mỗi `PricingLineType` (`TICKET`, `FNB`, `TIME_BASED_SURCHARGE`) map đúng một strategy; trùng `lineType` hoặc thiếu line → `IllegalStateException` khi khởi tạo bean. Khi cần thêm dòng giá mới, mở rộng enum và thêm một `@Component` implement `PricingStrategy`.

`PricingEngine` gọi lần lượt ba strategy theo map, cộng kết quả thành subtotal, rồi chuyển sang tầng Decorator để tính giảm giá.

### Ưu điểm đạt được

Mỗi strategy có thể được kiểm thử hoàn toàn độc lập với input giả định, không cần khởi động Spring. Khi cần thêm loại tính giá mới — ví dụ phụ phí theo tỷ lệ lấp đầy — chỉ cần tạo thêm một class strategy và đăng ký vào engine, không sửa code hiện tại. Rate phụ phí cuối tuần và ngày lễ có thể điều chỉnh qua cấu hình mà không cần triển khai lại.

---

## 8. Decorator — Tầng Giảm Giá

### Vấn đề cụ thể

Hệ thống có hai loại giảm giá có thể áp dụng đồng thời và độc lập: giảm theo mã khuyến mãi (FIXED hoặc PERCENT) và giảm theo hạng thành viên. Số lượng và thứ tự áp dụng phụ thuộc hoàn toàn vào dữ liệu runtime — khách có tier hay không, có nhập promo hay không. Trong MVP, phần giảm theo hạng thành viên hoàn toàn bị bỏ quên, và logic giảm giá bị viết lẫn lộn với logic validate.

### Tại sao chọn Decorator

Decorator Pattern giải quyết bài toán "thêm hành vi tại runtime mà không thay đổi đối tượng gốc". Thay vì tạo các class kết hợp kiểu `PromotionAndMemberDiscount`, `OnlyMemberDiscount`, `OnlyPromotionDiscount`... (bùng nổ số lượng class với inheritance), Decorator cho phép wrap các đối tượng lại với nhau theo bất kỳ thứ tự và tổ hợp nào. Mỗi decorator chỉ đảm nhiệm đúng một loại giảm giá và tích lũy kết quả từ tầng bên trong.

### Cách hoạt động sau khi áp dụng

Chain giảm giá được xây dựng tại runtime trong `PricingEngine` dựa trên nội dung của `PricingContext`. Điểm khởi đầu luôn là `NoDiscount` — một đối tượng đặc biệt trả về kết quả giảm giá bằng 0, đóng vai trò nền tảng của chuỗi.

Nếu context chứa một đối tượng Promotion hợp lệ (đã được validate bởi `PromoValidHandler` ở tầng CoR từ trước), `PromotionDiscountDecorator` được wrap bên ngoài `NoDiscount`. Decorator này tính khoản giảm theo quy tắc của promo rồi cộng vào kết quả từ tầng bên trong.

Nếu khách hàng có hạng thành viên với tỷ lệ giảm giá lớn hơn 0, `MemberDiscountDecorator` được wrap thêm bên ngoài. Decorator này tính khoản giảm theo tier của khách trên **phần tiền còn lại sau các discount đã áp trước đó** (điển hình là promo), rồi cộng vào kết quả tích lũy từ các tầng bên trong.

Điểm quan trọng cần nhấn mạnh: tầng Decorator chỉ **tính toán thuần túy** — nó không hề validate promo còn hạn không hay tier có hợp lệ không. Toàn bộ việc validate đó đã được thực hiện ở tầng Chain of Responsibility từ bước đầu. Sự phân tách rõ ràng giữa validate và tính toán là một trong những điểm thiết kế quan trọng nhất của hệ thống này.

Thứ tự runtime hiện tại của chain là: `NoDiscount -> PromotionDiscountDecorator -> MemberDiscountDecorator`. Nghĩa là promo áp trước trên `subtotal`, member áp sau trên phần còn lại. `PricingEngine` dùng kết quả chain để điền `discountAmount` (tổng giảm) và `membershipDiscount` (mức giảm member thực tế) trong `PriceBreakdownDTO`.

### Ưu điểm đạt được

Không bao giờ xảy ra tình trạng bỏ quên một khoản giảm giá vì mỗi loại là một class riêng biệt, được đăng ký rõ ràng trong logic build chain. Khi cần thêm loại giảm giá mới — ví dụ giảm giá ngày sinh nhật, hoặc giảm giá lần đầu đặt vé — chỉ cần tạo thêm một Decorator class và thêm điều kiện wrap vào `buildDiscountChain()`, không sửa bất kỳ Decorator nào đang tồn tại.

---

## 9. Ưu Điểm Tổng Hợp Sau Khi Áp Dụng

### Về chất lượng thiết kế

Kiến trúc được phân tầng rõ ràng với từng tầng đảm nhiệm đúng một loại trách nhiệm. Validate không lẫn với tính giá, tính giá không lẫn với cache, tính giá tiền không lẫn với tính giảm giá. Mỗi class trong hệ thống có thể được đọc và hiểu một cách độc lập mà không cần theo dõi toàn bộ luồng.

### Về khả năng mở rộng

Hệ thống được thiết kế để mở rộng mà không cần sửa code hiện tại. Thêm quy tắc validate mới, thêm loại tính giá mới, thêm loại giảm giá mới, thêm điều kiện nghiệp vụ mới — tất cả đều thực hiện bằng cách tạo thêm class mới và đăng ký vào đúng vị trí trong cấu hình.

### Về kiểm thử

Mỗi tầng và mỗi component trong hệ thống có thể được kiểm thử hoàn toàn độc lập. Toàn bộ 9 test case cho tầng Specification chạy trong chưa đầy 1 giây mà không cần kết nối database hay Redis, chứng minh logic nghiệp vụ được tách biệt hoàn toàn khỏi infrastructure.

### Về hiệu năng

Nhờ Redis cache ở tầng Proxy, với cùng một combination đầu vào, hệ thống chỉ tính giá một lần duy nhất. Các lần preview tiếp theo trả về kết quả ngay lập tức từ bộ nhớ Redis. Ngoài ra, việc load dữ liệu F&B một lần trước khi vào engine đã loại bỏ hoàn toàn vấn đề N+1 query.

### Về tính chính xác nghiệp vụ

Kết quả tính giá giờ bao gồm đầy đủ 7 trường: tiền vé, tiền F&B, phụ phí thời gian, giảm giá theo membership, tổng giảm giá, chiến lược áp dụng, và tổng cuối. Không còn bỏ sót khoản nào như phiên bản MVP.

---

## 10. So Sánh Trước Và Sau

### Cách tổ chức code

Phiên bản MVP tập trung toàn bộ logic vào một method duy nhất với khoảng 80–100 dòng code, làm tất cả mọi việc từ validate đến tính giá đến giảm giá. Phiên bản sau được tách thành các lớp và class riêng biệt, mỗi class từ 20–50 dòng, chỉ làm một việc cụ thể. Tổng số dòng code tăng lên nhưng độ phức tạp của từng phần giảm đi đáng kể.

### Cách xử lý khi có thay đổi yêu cầu

| Thay đổi yêu cầu | Trước — Phải làm gì | Sau — Phải làm gì |
|---|---|---|
| Thêm rule: không đặt quá 8 ghế | Sửa `calculatePrice()` đang hoạt động | Tạo class `MaxSeatsHandler` mới, đăng ký vào chain |
| Thêm phụ phí giờ cao điểm | Sửa engine đang hoạt động | Tạo class `PeakHourPricingStrategy` mới |
| Thêm giảm giá ngày sinh nhật | Sửa engine đang hoạt động | Tạo class `BirthdayDiscountDecorator` mới |
| Điều kiện ngày lễ mới | Sửa và tìm đoạn code giữa method dài | Thêm 1 dòng vào `VIETNAMESE_HOLIDAYS` trong `PricingConditions` |
| Tắt cache để debug | Sửa service để bỏ cache logic | Đổi `@Primary` sang `PricingEngine` trực tiếp |
| Đổi rate cuối tuần từ 15% lên 20% | Sửa code và build lại | Sửa 1 dòng trong `application.properties`, không build lại |

### Kết quả vận hành

| Chỉ số | Trước (MVP) | Sau (5 pattern) |
|---|---|---|
| Số truy vấn DB mỗi lần tính giá | 5–8 query | 3–4 lần đầu; **0 query** nếu cache hit |
| Độ trễ khi user preview lại | ~150ms | **< 5ms** nếu cache hit |
| Khoản giảm theo membership | Bị bỏ quên (lỗi nghiệp vụ) | Tính đầy đủ, hiển thị riêng trong response |
| Điều kiện ngày lễ | Không tồn tại (bị sót) | Đầy đủ 4 ngày lễ quốc gia Việt Nam |
| Số trường trong response | 4 trường | 7 trường đầy đủ breakdown |
| Thêm rule / loại tính giá mới | Sửa code đang hoạt động | Thêm class mới, không sửa code cũ |
| Kiểm thử từng phần riêng lẻ | Không thể | 9/9 test Specification pass, không cần DB/Redis |

---

> **Kết luận:** Năm design pattern được áp dụng trong Dynamic Pricing Engine không phải là sự lựa chọn tùy tiện mà là câu trả lời trực tiếp cho từng nhóm vấn đề cụ thể: Chain of Responsibility giải quyết sự hỗn độn của validate, Proxy giải quyết vấn đề hiệu năng, Specification giải quyết sự phân tán của điều kiện nghiệp vụ, Strategy giải quyết sự lẫn lộn giữa các công thức tính giá, và Decorator giải quyết sự thiếu linh hoạt trong việc kết hợp các khoản giảm giá. Kết quả là một hệ thống có thể đọc, kiểm thử, và mở rộng một cách tự tin mà không lo phá vỡ những gì đang hoạt động tốt.
