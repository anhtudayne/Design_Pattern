Bổ sung Design Pattern Mới cho Backend Cinema



Các pattern sau đây không trùng với những pattern đã đề xuất trước đó (Template Method, Strategy, Decorator, Factory, State, Adapter, Observer, Facade, Command).



PHẦN 1 — Luồng Checkout & Payment (User flow)

1. Chain of Responsibility — Validation Pipeline cho Checkout

Vấn đề hiện tại: CheckoutServiceImpl.createBooking() nhét toàn bộ validation vào một method body:





Kiểm tra user tồn tại



Kiểm tra showtime tồn tại



Kiểm tra từng ghế đã bán chưa (vòng lặp)



Kiểm tra promo code hợp lệ

Mỗi lần thêm rule mới (giới hạn 8 ghế/booking, kiểm tra giờ chiếu đã qua...) phải sửa thẳng vào method — vi phạm Open/Closed Principle.

Giải pháp: Chuỗi handler độc lập, mỗi handler chỉ làm một việc:

UserExistsHandler → ShowtimeAvailableHandler → SeatAvailableHandler → PromoValidHandler → MaxSeatsHandler

Bổ sung khi triển khai: MaxSeatsHandler bắt buộc enforce giới hạn tối đa 8 ghế / booking (ví dụ seatIds.size() <= 8) và trả lỗi rõ ràng nếu vượt quá.

File liên quan: [CheckoutServiceImpl.java](backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java)

File mới: patterns/chainofresponsibility/CheckoutValidationHandler.java, UserExistsHandler.java, ShowtimeAvailableHandler.java, SeatAvailableHandler.java, MaxSeatsHandler.java



2. Mediator — Điều phối luồng hậu thanh toán

Vấn đề hiện tại: CheckoutServiceImpl.processMomoCallback() là God method — một method trực tiếp điều phối 5 nghiệp vụ khác nhau:





bookingRepository.save() — cập nhật trạng thái Booking



userRepository.save() — cộng total_spending cho User



ticketRepository.save() (vòng lặp) — tạo từng Ticket



paymentRepository.save() — ghi transaction Payment



emailService.sendTicketEmail() — gửi email

Giải pháp: PostPaymentMediator nhận PaymentSuccessEvent và điều phối các component xử lý độc lập. Mỗi component (BookingUpdater, UserSpendingUpdater, TicketCreator, PaymentRecorder, EmailNotifier) chỉ biết Mediator, không biết nhau.

File liên quan: [CheckoutServiceImpl.java](backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java)

File mới: patterns/mediator/PostPaymentMediator.java, PaymentColleague.java, BookingUpdater.java, UserSpendingUpdater.java, TicketCreator.java, PaymentRecorder.java, EmailNotifier.java



PHẦN 2 — Admin: Movie, Showtime, Catalog

3. Proxy — Caching Proxy cho MovieService

Vấn đề hiện tại: MovieServiceImpl query thẳng DB mỗi request getAllMovies() và getMoviesByStatus(). PublicController gọi hai endpoint này rất thường xuyên (mỗi lần user vào trang chủ). Redis đã có sẵn trong project (dùng cho seat lock và voucher) nhưng hoàn toàn không dùng cho catalog data.

Giải pháp: CachingMovieServiceProxy implements MovieService — wrap MovieServiceImpl, cache kết quả vào Redis với TTL. Khi admin gọi createMovie() / updateMovie() / deleteMovie(), proxy tự invalidate cache. Cùng interface → Controller không cần thay đổi gì.

File liên quan: [MovieServiceImpl.java](backend/src/main/java/com/cinema/booking/services/impl/MovieServiceImpl.java), [services/MovieService.java](backend/src/main/java/com/cinema/booking/services/MovieService.java)

File mới: patterns/proxy/CachingMovieServiceProxy.java



4. Specification — Filter suất chiếu công khai

Vấn đề hiện tại: PublicController.getPublicShowtimes() load toàn bộ getAllShowtimes() từ DB rồi filter in-memory bằng Java stream:

List<ShowtimeDTO> all = showtimeService.getAllShowtimes(); // full table scan
if (cinemaId != null) all = all.stream().filter(...).toList();
if (movieId != null)  all = all.stream().filter(...).toList();
if (date != null)     all = all.stream().filter(...).toList();

Càng nhiều dữ liệu, càng tốn memory. Thêm filter mới phải sửa method.

Giải pháp: ShowtimeSpecification implements Specification<Showtime> — đẩy toàn bộ điều kiện filter xuống DB dưới dạng SQL WHERE. ShowtimeRepository extends JpaSpecificationExecutor<Showtime> để hỗ trợ. Controller truyền Specification thay vì load hết rồi filter.

Bổ sung khi triển khai: cập nhật ShowtimeRepository để extends JpaSpecificationExecutor<Showtime> (bên cạnh JpaRepository) nhằm gọi findAll(Specification, ...).

File liên quan: [PublicController.java](backend/src/main/java/com/cinema/booking/controllers/PublicController.java), [ShowtimeRepository.java](backend/src/main/java/com/cinema/booking/repositories/ShowtimeRepository.java)

File mới: patterns/specification/ShowtimeSpecification.java



PHẦN 3 — Admin: Dashboard & Thống kê

5. Composite — Dashboard Stats từ nhiều nguồn

Vấn đề hiện tại: DashboardController.getStats() inject trực tiếp 5 repository + 1 service, tự tổng hợp số liệu:

stats.put("totalMovies", movieRepository.count());
stats.put("totalUsers", userRepository.count());
stats.put("totalShowtimes", showtimeRepository.count());
stats.put("totalFnbItems", fnbItemRepository.count());
stats.put("totalVouchers", voucherService.getAllVouchers().size());
// + logic tính revenue...

Thêm thống kê mới (bookings hôm nay, tỷ lệ lấp đầy rạp...) phải inject thêm dependency và sửa controller — vi phạm Single Responsibility.

Giải pháp: DashboardStatsComposite — mỗi nguồn là một StatsComponent (leaf hoặc composite). Controller chỉ giữ một DashboardStatsComposite và gọi collect() duy nhất một lần.

DashboardStatsComposite.collect()
├── MovieStatsLeaf        → {totalMovies: N}
├── UserStatsLeaf         → {totalUsers: N}
├── ShowtimeStatsLeaf     → {totalShowtimes: N}
├── FnbStatsLeaf          → {totalFnbItems: N}
├── VoucherStatsLeaf      → {totalVouchers: N}
└── RevenueStatsLeaf      → {totalRevenue: X, totalTickets: Y}

File liên quan: [DashboardController.java](backend/src/main/java/com/cinema/booking/controllers/DashboardController.java)

File mới: patterns/composite/StatsComponent.java, MovieStatsLeaf.java, UserStatsLeaf.java, ShowtimeStatsLeaf.java, FnbStatsLeaf.java, VoucherStatsLeaf.java, RevenueStatsLeaf.java, DashboardStatsComposite.java



PHẦN 4 — Cross-cutting

6. Singleton — Bean RestTemplate trong MomoServiceImpl

Vấn đề hiện tại: MomoServiceImpl khai báo private final RestTemplate restTemplate = new RestTemplate() trực tiếp trong field — Spring không quản lý lifecycle, không thể mock khi viết unit test.

Giải pháp: RestTemplateConfig.java expose RestTemplate như @Bean. Spring container đảm bảo duy nhất một instance trong toàn application context (Singleton scope là default của Spring beans).

File liên quan: [MomoServiceImpl.java](backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java), [config/](backend/src/main/java/com/cinema/booking/config/)

File mới: config/RestTemplateConfig.java



7. Prototype — Template Email nhiều loại

Vấn đề hiện tại: EmailServiceImpl có hai method (sendTicketEmail, sendWelcomeEmail) đều build nội dung email bằng string concatenation thẳng vào method. Cả hai method đều có cấu trúc giống nhau: setTo → setSubject → setText → send. Khi thêm email hoàn vé, email nhắc suất chiếu... mỗi loại lại copy-paste toàn bộ cấu trúc đó.

Giải pháp: EmailTemplate là abstract class với method clone() và build() trừu tượng. Mỗi loại email cụ thể (TicketEmailTemplate, WelcomeEmailTemplate, RefundEmailTemplate) override build() để điền data riêng. EmailServiceImpl chỉ clone prototype từ registry rồi gọi build().

File liên quan: [EmailServiceImpl.java](backend/src/main/java/com/cinema/booking/services/impl/EmailServiceImpl.java)

File mới: patterns/prototype/EmailTemplate.java, TicketEmailTemplate.java, WelcomeEmailTemplate.java, RefundEmailTemplate.java



Tổng hợp — Bản đồ Pattern Mới theo Domain

LUỒNG CHECKOUT (User)
├── Chain of Responsibility  — Validation pipeline trong createBooking()
└── Mediator                 — Điều phối 5 bước hậu thanh toán trong processMomoCallback()

ADMIN: MOVIE & SHOWTIME
├── Proxy                    — Redis cache cho MovieService (getAllMovies, getMoviesByStatus)
└── Specification            — Filter showtime xuống DB thay in-memory stream trong PublicController

ADMIN: DASHBOARD
└── Composite                — Gom 6 nguồn thống kê thành cây DashboardStatsComposite

CROSS-CUTTING
├── Singleton                — Bean RestTemplate cho MomoService (config/RestTemplateConfig)
└── Prototype                — Template email nhiều loại (TicketEmail, WelcomeEmail, RefundEmail)



Những pattern đã xem xét nhưng KHÔNG đưa vào







Pattern



Lý do loại bỏ





Builder (Booking)



Staff flow chưa tồn tại trong codebase — triển khai cho feature chưa có là premature. Khi có staff flow sẽ thêm.





Flyweight (Surcharge)



Vấn đề thực là duplicate code trong 2 method — giải pháp đúng là Extract Method private, không phải Flyweight (Flyweight dùng khi có hàng nghìn object chia sẻ intrinsic state).





Null Object (Metadata)



Actor/Director là JPA @Entity không có interface chung — tạo NullActor extends entity sẽ xung đột JPA mapping. Vấn đề thực chỉ là thiếu message trong .orElseThrow(), dùng custom exception là đủ.



Danh sách file cần tạo





patterns/chainofresponsibility/ — CheckoutValidationHandler.java, UserExistsHandler.java, ShowtimeAvailableHandler.java, SeatAvailableHandler.java, MaxSeatsHandler.java



patterns/mediator/ — PostPaymentMediator.java, PaymentColleague.java, BookingUpdater.java, UserSpendingUpdater.java, TicketCreator.java, PaymentRecorder.java, EmailNotifier.java



patterns/proxy/ — CachingMovieServiceProxy.java



patterns/specification/ — ShowtimeSpecification.java



patterns/composite/ — StatsComponent.java, MovieStatsLeaf.java, UserStatsLeaf.java, ShowtimeStatsLeaf.java, FnbStatsLeaf.java, VoucherStatsLeaf.java, RevenueStatsLeaf.java, DashboardStatsComposite.java



patterns/prototype/ — EmailTemplate.java, TicketEmailTemplate.java, WelcomeEmailTemplate.java, RefundEmailTemplate.java



config/RestTemplateConfig.java



Thứ tự triển khai đề xuất





Singleton — fix RestTemplate config (1 file, độc lập, ít rủi ro nhất)



Chain of Responsibility — refactor validation trong createBooking() (vấn đề rõ ràng nhất)



Mediator — tách processMomoCallback() thành các handler nhỏ



Composite — refactor DashboardController (độc lập, không ảnh hưởng user flow)



Proxy — thêm Redis cache cho MovieService



Specification — thay in-memory filter trong PublicController



Prototype — template email (thấp ưu tiên nhất)

