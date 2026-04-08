# Dự án: Xây dựng Website Đặt Vé Xem Phim Hoàn Chỉnh

## 1. Giới thiệu
Dự án là một hệ thống website bán vé xem phim trực tuyến (Tương tự CGV, Lotte Cinema, Galaxy) với các chức năng hoàn thiện và trải nghiệm người dùng tối ưu. Hệ thống phục vụ cho ba đối tượng chính: Khách hàng (Customer), Nhân viên tại rạp (Staff), và Quản trị viên (Admin).

## 2. Tính năng chính (Features)

### 2.1. Phía Khách hàng (User/Customer)
* **Quản lý tài khoản (Authentication & Profile):**
  * Đăng ký/ Đăng nhập (hỗ trợ Email, Google).
  * Quản lý thông tin cá nhân.
  * Tích điểm thành viên (Membership Points) để nâng hạng (Standard, VIP, VVIP). 
  * Xem lịch sử giao dịch và vé điện tử đã đặt.
  * Xem thông báo
  * Xem hạng và tổng chi tiêu trong năm hiện tại


* **Các tính năng hiển thị trên web mà Khách Hàng và Guest Checkout đều có thể xem**
  * Hiển thị danh sách Phim Đang Chiếu, Phim Sắp Chiếu.
  * Tìm kiếm phim theo tên phim, lọc theo Thể loại, Độ tuổi.
  * Xem chi tiết phim: Poster, thể loại, Diễn viên, Đạo diễn, Tóm tắt nội dung phim, Đánh giá trung bình.
  * Xem bài Review đánh giá thực tế của mọt phim từ những khách khác.
  * Xem chi tiết đạo diễn, diễn viên: mô tả, tiểu sử, phim tham gia
  * Blog điện ảnh (Bài viết):  giới thiệu phim , bài viết phân tích hoặc bài viết bình luận phim 

* **Quy trình đặt vé (Booking Flow) 5 bước:**
  1. *Chọn Phim & Chọn Rạp:* Khách hàng chọn Tỉnh/Thành phố, sau đó hệ thống sẽ hiển thị danh sách các Cụm rạp (Cinema) thuộc thành phố đó để chọn.
  2. *Chọn Suất chiếu:* Xem lịch theo ngày, giờ (có thông báo tình trạng ghế trống/sắp đầy).
  3. *Chọn Ghế:* Giao diện chọn ghế ngồi trực quan mô phỏng sơ đồ Rạp (Standard, VIP, Couple - tính phí khác nhau).
  4. *Chọn Dịch vụ kèm (F&B):* Mua bắp, nước, combo, đồ chơi (Merch) đi kèm tiện lợi.
  5. *Thanh toán:* Nhập mã giảm giá -> Bấm thanh toán bằng nhiều hình thức: Cổng thanh toán (VNPay, Momo, ZaloPay), Thẻ tín dụng, Điểm tích lũy.

* **Hệ thống vé điện tử (E-Ticket):**
  * Cấp vé dưới dạng mã QR sau khi thanh toán xuất trên Web/Mobile.
  * Gửi vé qua Email tự động.

* **Đánh giá & Bình luận Phim (Review & Rating):**
  * Tính năng này được đặt ở một Tab/Trang hoàn toàn tách biệt (Không nằm chung trong luồng đặt vé).
  * **Điều kiện bắt buộc:** Khách hàng phải đăng nhập, đã mua vé thành công và hệ thống ghi nhận suất chiếu đó đã kết thúc (Đã xem xong) thì mới được phép mở khóa chức năng chấm điểm (1-5 sao) và viết bình luận cho bộ phim đó (Tránh Review rác/Spam).

* **Chính sách Nghiệp vụ Cốt lõi (Core Business Rules):**
  * **Bắt buộc Đăng nhập (Mua vé):** Hệ thống **BẮT BUỘC** người dùng phải Đăng nhập/Đăng ký (Hỗ trợ làm nhanh qua Google Login) thì mới được phép bắt đầu tiến hành quy trình Đặt vé phần Chọn Ghế. Các quyền lợi xem giờ chiếu, xem thông tin phim thì Khách vãng lai (Guest) vẫn có thể dạo coi xem tự do. Quyết định ép luồng (Force-Login) này giúp Rạp phim giữ chân khách hàng (Retention) qua cơ chế tích điểm từ đầu, đồng thời giảm triệt để khiếu nại thất lạc mã vé.
  * **Quy định Check-in & Hủy vé (Refund Policy):** Khách hàng được trao quyền tự hủy Vé trực tiếp trên Web. Cổng Hủy Vé sẽ tự đóng lại (Freeze) trước thời điểm suất chiếu bắt đầu (Start Time) **ít nhất 1 Tiếng đồng hồ**. Qua hạn chót này, hệ thống sẽ từ chối nghiệp vụ Hủy hoàn tiền.

### 2.2. Phía Quản trị (Admin) & Nhân viên rạp (Staff)
* **Dashboard / Báo cáo Thống kê:** 
  * Biểu đồ doanh thu vé bán, doanh thu bắp nước trong ngày/tuần/tháng.
  * Top phim doanh thu cao nhất, tỉ lệ lấp đầy phòng chiếu.

* **Quản lý danh mục cốt lõi:**
  * **Hệ thống Rạp:** Thêm/sửa cụm rạp, quản lý các phòng chiếu trực thuộc, kích thước/số ghế mỗi phòng.
  * **Hệ thống Phim:** Thêm phim mới, chỉnh sửa thông tin, ngày ra mắt.
  * **Lên lịch Suất chiếu:** Xếp lịch chiếu phim cho từng phòng/ngày, thiết lập giá vé cơ sở, cài đặt phụ thu (cuối tuần, Lễ Tết).

* **Quản lý Đơn hàng & Vé:**
  * Hỗ trợ đổi/trả vé (tùy chính sách), Check-in vé tại quầy bằng cách quét mã QR barcode.
  * Tra cứu mã đơn hàng.

* **Quản lý Sản phẩm Kèm (F&B):** Thêm, Sửa combo bắp nước, đồ lưu niệm và điều chỉnh giá linh hoạt.
* **Quản lý Khuyến mãi / Marketing:** Tạo Voucher giảm giá theo % hoặc hạn mức tiền mặt, thời gian áp dụng, định mức điểm đổi Coupon.
* **Quản lý Người dùng:** Thiết lập phân quyền (Roles: Admin, Cinema Manager, Staff, User), Quá khóa/kích hoạt tài khoản.

---

## 3. Thiết kế Cơ sở dữ liệu (Database & ERD)

Dưới đây là mô hình các thực thể (Entities) cốt lõi của bài toán Đặt vé:

1. **User (Người dùng)**
  * `user_id` (PK), `tier_id` (FK - Hạng KH), `fullname`, `email`, `password_hash`, `phone`, `role` (Admin/Customer), `total_spending` (Tổng chi tiêu), `loyalty_points`, `created_at`.
2. **Movie (Phim)**
  * `movie_id` (PK), `title`, `description`, `duration_minutes`, `release_date`, `language`, `age_rating` (C13, C18...), `poster_url`, `trailer_url`, `status`.
3. **Genre (Thể loại)**
  * `genre_id` (PK), `name` (VD: Hành động, Hài hước...).
  * _Quan hệ nhiều-nhiều với Movie thông qua bảng `Movie_Genre`._
4. **Cinema (Cụm rạp)**
  * `cinema_id` (PK), `location_id` (FK), `name` (VD: CGV Sư Vạn Hạnh), `address`, `hotline`.
5. **Room/Hall (Phòng chiếu)**
  * `room_id` (PK), `cinema_id` (FK), `name` (VD: Room 1, IMAX-Room), `screen_type` (2D, 3D, IMAX).
6. **Seat (Ghế ngồi)**
  * `seat_id` (PK), `room_id` (FK), `seat_row` (A, B, C..), `seat_number` (1, 2, 3..), `seat_type` (Standard, VIP, Couple), `price_surcharge` (Phụ thu: Ví dụ VIP +15k), `is_active`.
7. **Showtime (Suất chiếu)**
  * `showtime_id` (PK), `movie_id` (FK), `room_id` (FK), `start_time`, `end_time`, `base_price`.
8. **FNB_Item (Sản phẩm F&B: Bắp/Nước/Combo)**
  * `item_id` (PK), `name`, `description`, `price`, `image_url`, `is_active`.
9. **Promotion (Khuyến mãi)**
  * `promo_id` (PK), `code`, `discount_percentage`, `max_discount_amount`, `min_purchase_amount` (Đơn tối thiểu), `valid_from`, `valid_to`, `quantity`.
10. **Booking/Order (Đơn Đặt Vé)**
  * `booking_id` (PK), `user_id` (FK), `showtime_id` (FK), `promo_id` (FK nullable), `total_price`, `status` (Pending, Paid, Cancelled), `qr_code`, `created_at`.
11. **Booking_Seat/Ticket (Vé - Chi tiết ghế trong đơn hàng)**
  * `ticket_id` (PK), `booking_id` (FK), `seat_id` (FK), `price` (giá chốt tại thời điểm mua).
12. **Booking_FNB (Chi tiết hóa đơn mua F&B đi kèm)**
  * `booking_id` (FK), `item_id` (FK), `quantity`, `price` (giá tĩnh lúc mua).
  * _Primary Key hợp từ (`booking_id`, `item_id`)._
13. **Review (Đánh giá)**
  * `review_id` (PK), `user_id` (FK), `movie_id` (FK), `rating_stars` (1-5), `comment`, `created_at`.
14. **Location (Tỉnh / Thành phố)**
  * `location_id` (PK), `name`.
15. **Director (Đạo diễn)**
  * `director_id` (PK), `full_name`, `bio`, `birth_date`, `nationality`, `image_url`.
16. **Actor (Diễn viên)**
  * `actor_id` (PK), `full_name`, `bio`, `birth_date`, `nationality`, `image_url`.
17. **Movie_Director (Phim - Đạo diễn)**
  * `movie_id` (FK), `director_id` (FK).
  * _Primary Key hợp từ (`movie_id`, `director_id`)._
18. **Movie_Actor (Phim - Diễn viên)**
  * `id` (PK - Surrogate Key), `movie_id` (FK), `actor_id` (FK), `role_name`.
19. **Payment (Lịch sử Thanh toán)**
  * `payment_id` (PK), `booking_id` (FK), `payment_method`, `transaction_id`, `amount`, `status` (SUCCESS, FAILED), `paid_at`.
20. **Article (Bài viết Blog Điện ảnh)**
  * `article_id` (PK), `thumbnail_url` (Ảnh bìa), `title`, `content`, `author` (hoặc `user_id` FK), `view_count`, `created_at`.
21. **Membership_Tier (Hạng Thành Viên)**
  * `tier_id` (PK), `name` (VD: Standard, VIP, VVIP), `min_spending` (Hạn mức chi tiêu), `discount_percent` (Phần trăm ưu đãi).
22. **Notification (Thông báo)**
  * `noti_id` (PK), `user_id` (FK), `title`, `message`, `is_read`, `created_at`.


  ### Chi tiết Sơ đồ Quan hệ (ERD Relationships)

  **1. Quan hệ Cơ sở hạ tầng Rạp (Cinema Infrastructure):**
  * **Location (1) ↔ (N) Cinema:** Mỗi tỉnh/thành phố có nhiều cụm rạp.
  * **Cinema (1) ↔ (N) Room:** Mỗi cụm rạp chứa nhiều phòng chiếu. Mỗi phòng chỉ thuộc một rạp.
  * **Room (1) ↔ (N) Seat:** Mỗi phòng có nhiều ghế. Mỗi ghế gắn với một phòng duy nhất.

  **2. Quan hệ Lịch chiếu (Scheduling):**
  * **Movie (1) ↔ (N) Showtime:** Một phim có nhiều suất chiếu theo thời gian.
  * **Room (1) ↔ (N) Showtime:** Một phòng tổ chức nhiều suất, không trùng giờ.
  * *(Showtime là thực thể trung gian giải quyết quan hệ N-N: Phim ↔ Room)*

  **3. Quan hệ Đặt vé (Booking & Ticketing):**
  * **User (1) ↔ (N) Booking:** Khách hàng có nhiều đơn hàng.
  * **Showtime (1) ↔ (N) Booking:** Mỗi suất chiếu có nhiều đơn đặt vé.
  * **Booking (1) ↔ (N) Booking_Seat:** Một đơn hàng chứa nhiều vé.
  * **Seat + Showtime (1) ↔ (1) Booking_Seat:** Mỗi ghế trong suất chiếu chỉ được bán một vé duy nhất (tránh trùng lặp).

  **4. Quan hệ Dịch vụ thêm (Add-on Services):**
  * **Booking (N) ↔ (M) FNB_Item via Booking_FNB:** Đơn hàng mua nhiều sản phẩm F&B; sản phẩm bán trong nhiều đơn.
  * **Promotion (1) ↔ (N) Booking:** Mã khuyến mãi áp dụng cho nhiều đơn; mỗi đơn dùng tối đa một mã.

  **5. Quan hệ Nội dung (Content & Metadata):**
  * **Movie (N) ↔ (M) Genre via Movie_Genre:** Phim có nhiều thể loại; thể loại có nhiều phim.
  * **Movie (N) ↔ (M) Director via Movie_Director:** Phim có đạo diễn; đạo diễn làm nhiều phim.
  * **Movie (N) ↔ (M) Actor via Movie_Actor:** Phim có diễn viên; diễn viên đóng nhiều phim.
  * **User (1) ↔ (N) Review ↔ (1) Movie:** Khách hàng đánh giá phim; phim nhận nhiều đánh giá.

  **7. Quan hệ Thẻ Thành Viên & Tương tác (User Engagement):**
  * **Membership_Tier (1) ↔ (N) User:** Tham chiếu Hạng thẻ của User dựa trên mốc chi tiêu (`total_spending`).
  * **User (1) ↔ (N) Notification:** User nhận thông báo từ hệ thống (VD: Remind giờ chiếu phim).

  **6. Quan hệ Thanh toán (Transactions):**
  * **Booking (1) ↔ (N) Payment:** Mỗi đơn vé có nhật ký các giao dịch thanh toán.
  * *(Ghi chú Tối ưu Hóa: Lịch sử xem phim của User không cần thiết kế bảng riêng, mà được truy vấn trực tiếp từ bảng hệ thống `Booking`, lọc các đơn hàng có trạng thái `PAID` và có giờ chiếu `start_time` (trong bảng `Showtime`) nhỏ hơn `< Thời gian hiện tại`).*
---

## 4. Cấu trúc Công nghệ (Tech Stack)
Dự án được định hướng xây dựng dựa trên ngăn xếp công nghệ cốt lõi dành cho hệ thống có tính giao dịch cao (E-commerce / Booking System):

*   **Frontend (Giao diện Khách hàng & CMS Admin):** 
    *   **Core:** `ReactJS` được chọn làm nền tảng chính. Kiến trúc Virtual DOM của React đóng vai trò cực kỳ quan trọng giúp việc render mượt mà khi xử lý thao tác State "Click chọn / Hủy chọn" liên tục trên một Sơ đồ rạp chứa hàng trăm chiếc ghế.
    *   **Styling:** Có thể tùy chọn Tailwind CSS hoặc UI Toolkit như Ant Design/Material-UI để tăng tốc độ phát triển Dashboard.

*   **Backend (Máy chủ Nghiệp vụ & API):** 
    *   **Core:** `Java` kết hợp Framework `Spring Boot`. Đặc thù ngành Đặt chỗ luôn ưu tiên Java vì hệ sinh thái mạnh mẽ đối với tính toàn vẹn giao dịch (Transaction Integrity), giúp các block code Tính tiền, Tích điểm dư nợ, Hủy vé hoạt động cực kỳ ổn định, bảo vệ chống tranh chấp dữ liệu.

*   **Database & Caching (Cơ sở dữ liệu):**
    *   **Primary DB:** `MySQL`. Hệ quản trị CSDL quan hệ (RDBMS) mang cấu trúc Bảng chặt chẽ, đóng vai trò gánh vác việc lưu trữ cứng vĩnh viễn toàn bộ 22 Entity thiết kế phía trên. Áp chuẩn ACID chống trôi dòng tiền.
    *   **In-Memory / Lock Engine:** `Redis`. Mảnh ghép bắt buộc để dự án sống sót. Redis chịu trách nhiệm 100% nhiệm vụ "Giữ Chỗ thần tốc" (Seat Locking) do truy xuất trên RAM siêu nhanh, đồng thời cấu hình hết hạn tự động 10 phút để xả ghế. Redis cũng lưu bộ Cache lịch chiếu để giảm số lần Query cọ xát vào MySQL mỗi ngày.

*   **Tích hợp Bên thứ ba (Third-party Integrations):**
    *   **Payment:** `VNPay Sandbox` hoặc `MoMo Web API`.
    *   **Cloud Storage:** `Cloudinary` / `AWS S3` để chuyên lưu ảnh Poster dung lượng lớn và cấp Webhook sinh QR Code.

---

## 5. Kiến trúc Hệ thống (System Architecture)

Để giải quyết triệt để các bài toán chịu tải cao (High Concurrency) và tắc nghẽn giao dịch khi có sự kiện mở bán rạp siêu bom tấn, hệ thống được thiết kế theo các luồng kiến trúc sau:

### 5.1. Cơ chế Khóa ghế thời gian thực (Real-time Seat Locking bằng Redis)
*   **Vấn đề:** Tránh tình trạng Double-Booking (2 người cùng thao tác mua trùng 1 ghế, cùng bị trừ tiền thẻ) trên CSDL RDBMS. Tránh cho Database phải lưu trữ hàng chục ngàn đơn hàng "Rác" (Click mua nhưng không trả tiền).
*   **Giải pháp:** Áp dụng **Redis In-Memory** để lưu trữ trạng thái Giữ Chỗ (Holding).
    *   **Locking:** Ngay khi Khách A click chọn ghế `VIP F5` ➡️ Backend bắn lệnh `SETNX` sinh ra khóa `lock:showtime_001:seat_F5` gán cho `User_A` kèm thời gian sống (TTL) đếm ngược **10 phút (600s)**.
    *   Nếu Khách B ấn vào ghế đó, Redis tra cứu trong bộ nhớ RAM trả ngay lỗi chỉ trong 1 miligiây. Giao diện báo: *"Ghế vừa có người chọn"*.
    *   **Auto-Release (Xả ghế tự động):** Nếu Khách A không sang trang thanh toán hoặc thoát web, sau đúng 10 phút khóa Redis tự hủy. Ghế F5 lập tức sáng đèn trở lại cho hàng triệu người dùng khác mua. Backend không bao giờ phải viết Code chạy nền đi dọn dẹp Database.
    *   **Chốt đơn an toàn:** Chỉ khi Webhook cổng thanh toán VNPay gửi lệnh báo `SUCCESS`, hệ thống mới chính thức `INSERT` dữ liệu vé vào MySQL (`Booking_Seat`) và xóa hẳn khóa Redis.

### 5.2. Quản trị phân quyền Cơ sở (Role-Based Access Control - RBAC)
*   Để chống thất thoát và sai lệch dữ liệu, Backend CMS Dashboard phân quyền chặt chẽ thông qua Token. 
*   Các tài khoản `Cinema Manager` (Giám đốc cụm rạp) và `Staff` (Nhân viên quầy bán vé) sẽ được cấp quyền (Authorize) thao tác CRUD độc lập và chỉ giới hạn lọc theo `cinema_id` thuộc quyền đồn trú của cụm rạp đó. Chỉ `Super Admin` ở Hội sở mới đọc được doanh số toàn hệ thống.

### 5.3. Xử lý tác vụ nền Bất đồng bộ (Asynchronous Background Jobs)
*   Không bắt người dùng ngồi chờ Website quay "Loading" khi Mua vé xong. 
*   Việc tạo file Vé điện tử (PDF E-Ticket), gửi Email xác nhận Đơn hàng, hoặc bắn App Thông Báo (Push Notification)... sẽ được tống thẳng vào **Message Queue (Ví dụ: RabbitMQ, Kafka hoặc Redis Pub/Sub)**. Các Worker ngầm đằng sau sẽ tự động bốc thư đi gởi từ từ để giảm tải tuyệt đối cho API chính của Web Booking.

---
_Đây là bộ tóm tắt tổng thể hệ thống (Overview), bao quát toàn bộ từ luồng Tính năng trải nghiệm (UX/UI Flow), Sơ đồ Database ERD đến Kiến trúc chịu tải. Kế hoạch này là một bản Blueprint hoàn chỉnh mang tiêu chuẩn Doanh nghiệp để tiến hành giao lập trình viên Code!_
