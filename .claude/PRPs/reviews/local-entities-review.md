# Báo cáo Review Mapping giữa Entity và Class Diagram

**Các tệp đã kiểm tra**: `backend/src/main/java/com/cinema/booking/entities/*.java`
**Tài liệu tham chiếu**: `classdiagram.md`
**Quyết định**: CHẤP NHẬN CÓ Ý KIẾN (APPROVE WITH COMMENTS)

## Tổng quan
Các lớp entity trong Java về cơ bản đã ánh xạ đúng với cấu trúc UML được định nghĩa trong `classdiagram.md`. Hầu hết các quan hệ (OneToMany, ManyToOne, Inheritance) đều đại diện tốt cho mô hình cơ sở dữ liệu vật lý. Tuy nhiên, sau khi kiểm tra toàn bộ các thực thể, có một số điểm thiếu sót về các collection hai chiều hoặc các thuộc tính cụ thể đã định nghĩa trong UML nhưng chưa có trong code, đồng thời code có mở rộng thêm nhiều thực thể chưa được cập nhật trong diagram.

## Các phát hiện

### MỨC ĐỘ CAO (HIGH)
- **Thiếu trường `hold_expires_at` trong Ticket**
  - *Vị trí*: `Ticket.java`
  - *Mô tả*: Biểu đồ định nghĩa trường `hold_expires_at` (kiểu Date) trong lớp Ticket. Đây là thông tin quan trọng để khóa/giữ ghế tạm thời nhưng đang bị thiếu hoàn toàn trong thực thể Java.
  - *Gợi ý*: Thêm `private LocalDateTime holdExpiresAt;` vào `Ticket.java`.

### MỨC ĐỘ TRUNG BÌNH (MEDIUM)
- **Thiếu Collection `seatList` trong Room**
  - *Vị trí*: `Room.java`
  - *Mô tả*: Biểu đồ chỉ định `Room` có `-List<Seat> seatList` và quan hệ Composition với `Seat`. Trong khi `Seat.java` đã ánh xạ ngược lại `Room`, thì `Room.java` lại thiếu collection `@OneToMany` hai chiều, gây khó khăn khi muốn truy xuất danh sách ghế trực tiếp từ phòng.
  - *Gợi ý*: Thêm `@OneToMany(mappedBy = "room", cascade = CascadeType.ALL) private List<Seat> seats;` vào `Room.java`.
- **Thiếu Collection `notifications` trong User**
  - *Vị trí*: `User.java`
  - *Mô tả*: Biểu đồ ghi nhận quan hệ `User "1" -- "n" Notification`. Tuy `Notification.java` đã map về User, nhưng lớp `User.java` đang thiếu danh sách `@OneToMany` tương ứng.
  - *Gợi ý*: Thêm `@OneToMany(mappedBy = "user", cascade = CascadeType.ALL) private List<Notification> notifications;`
- **Thiếu tham chiếu `payment` trong Booking**
  - *Vị trí*: `Booking.java`
  - *Mô tả*: Biểu đồ định nghĩa `Booking "1" -- "1" Payment`. Trong khi lớp `Payment.java` có `@OneToOne` liên kết đến Booking, bản thân lớp `Booking.java` lại không có tham chiếu ngược lại đến `Payment`.
  - *Gợi ý*: Thêm `@OneToOne(mappedBy = "booking", cascade = CascadeType.ALL) private Payment payment;` vào `Booking.java`.

### MỨC ĐỘ THẤP (LOW)
- **Trường `movie_ID` dư thừa trên Ticket**
  - *Mô tả*: UML có `movie_ID` trên Ticket. Code thực tế dựa vào `Showtime.movie` (đây là cách chuẩn hóa dữ liệu vật lý đúng để tránh dư thừa dữ liệu). Đây là một điểm cải tiến so với UML.
- **Thêm trường `unit_price` trong `FnBLine`**
  - *Mô tả*: Biểu đồ thiếu `unit_price` trong FnbLine. Code đã thêm vào để lưu lại giá tại thời điểm mua, giúp quản lý lịch sử giá tốt hơn.
- **Quan hệ `customer` trong Booking thay vì `userID`**
  - *Mô tả*: UML trỏ đến `userID`, trong khi entity map tới `Customer`. Điều này phù hợp với logic nghiệp vụ vì chỉ khách hàng mới đặt vé.

### CÁC THỰC THỂ MỞ RỘNG (KHÔNG CÓ TRONG DIAGRAM)
Thực tế triển khai đã mở rộng đáng kể so với `classdiagram.md` bằng cách giới thiệu các thực thể sau (chưa có trong UML):
- `Article.java` (Bài viết/Tin tức)
- `Artist.java` (Nghệ sĩ)
- `FnbCategory.java` (Danh mục đồ ăn)
- `FnbItemInventory.java` (Kho đồ ăn)
- `MembershipTier.java` (Hạng thành viên)
- `MovieGenre.java` / `MovieGenreId.java` (Bảng trung gian cho quan hệ N-N giữa Phim và Thể loại)
- `PromotionInventory.java` (Kho khuyến mãi)
- `Review.java` (Đánh giá)

Đây là các phần mở rộng hợp lý cho hệ thống nhưng đang làm sai lệch tài liệu thiết kế ban đầu.

## Kết quả kiểm tra

| Tiêu chí | Kết quả |
|---|---|
| Sự hiện diện của các lớp | Đạt (Tất cả các lớp trong diagram đều có code) |
| Tính nhất quán của các trường dữ liệu | Đạt có lưu ý (thiếu `hold_expires_at`) |
| Thiết lập quan hệ (Relationship) | Đạt có lưu ý (thiếu các collection hai chiều ở Room, User, Booking) |
| Độ khớp với Diagram | Có sự sai lệch (Nhiều thực thể mở rộng không có trong UML) |

## Danh sách các tệp đã review
- Cụm User: `User.java`, `UserAccount.java`, `Admin.java`, `Staff.java`, `Customer.java`
- Cụm Rạp/Phòng: `Room.java`, `Seat.java`, `SeatType.java`, `Location.java`, `Cinema.java`
- Cụm Phim: `Movie.java`, `Genre.java`, `MovieCast.java`, `CastMember.java`, `Showtime.java`
- Cụm Đặt vé & Thanh toán: `Booking.java`, `Ticket.java`, `Payment.java`, `Promotion.java`
- Khác: `FnbItem.java`, `FnBLine.java`, `Notification.java`
