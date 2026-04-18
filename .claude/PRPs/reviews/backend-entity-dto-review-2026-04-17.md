# Backend review — Entity refactor & DTO alignment

**Reviewed**: 2026-04-17  
**Phạm vi**: Nhánh `thang` (HEAD), không có thay đổi chưa commit (`git diff HEAD` rỗng). Đánh giá tập trung: sau khi chỉnh **entity**, các lớp còn lại (DTO, service, controller, seed) có khớp hay không.  
**Quyết định**: **BLOCK** — project **không compile** cho đến khi đồng bộ DTO ↔ service/controller hoặc chỉnh lại mapping.

## Summary

Refactor entity/DTO đã làm lệch hợp đồng API nội bộ: nhiều controller/service vẫn gọi setter/builder theo **tên field cũ** (`setCasts`, `setItemId`, `setMovieTitle`, `customerId`, `seatCode`, `movieTitle`, `UserDTO.fromEntity`, …) trong khi DTO hiện tại dùng **tên khác** (`movieCastList`, `fnbItemId`, thiếu field aggregate trên `ShowtimeDTO`, v.v.). Ngoài ra `MovieCast` dùng tên field Java `role_name` / `role_type` nên Lombok không sinh `getRoleName()` / `roleName()` như code đang mong đợi.

File `VoucherDTO.java` đã **bị xóa khỏi HEAD** (vẫn còn tham chiếu trong `VoucherController` / `VoucherService*`). Trong phiên review này file đã được **khôi phục** tại `backend/src/main/java/com/cinema/booking/dtos/VoucherDTO.java` để gỡ một phần lỗi biên dịch; các lỗi còn lại vẫn chặn build.

## Findings

### CRITICAL

| # | File / vị trí | Mô tả | Gợi ý sửa |
|---|----------------|-------|-----------|
| C1 | Toàn project compile | `mvn compile` thất bại (~50+ lỗi symbol) do mismatch DTO ↔ lớp gọi | Chọn một hướng: (A) cập nhật DTO + Lombok getters/setters cho khớp code hiện tại, hoặc (B) cập nhật toàn bộ controller/service theo DTO/entity mới. Không nên để hai “API” chồng lấn. |
| C2 | `com.cinema.booking.entities.MovieCast` | Field `role_name`, `role_type` — service gọi `getRoleName()`, `getRoleType()`, builder `roleName(...)` | Đổi thành `roleName`, `roleType` với `@Column(name = "role_name")` / `role_type`, hoặc sửa toàn bộ call site thành `getRole_name()` / `setRole_name()` (không khuyến nghị). |
| C3 | `MovieServiceImpl.java` (khoảng dòng 97–100) | Builder `MovieCast.builder().roleName(...)` không khớp entity; `List<Object>` vs `List<MovieCast>` | Sau khi thống nhất tên field + builder, sửa generic của stream/collect. |

### HIGH

| # | File / vị trí | Mô tả | Gợi ý sửa |
|---|----------------|-------|-----------|
| H1 | `FnbItemDTO` vs `FnbController`, `PublicController` | Code gọi `setItemId`, `setStockQuantity`, `setIsActive`; DTO chỉ có `fnbItemId`, không có stock/active | Thêm field vào DTO **nếu** entity có (hiện `FnbItem` entity không có stock/active — cần thêm cột + entity hoặc bỏ logic UI/controller). |
| H2 | `MovieDTO` vs `MovieController`, `MovieServiceImpl` | Code gọi `setCasts` / `getCasts`; DTO dùng `movieCastList` | Đổi call site → `setMovieCastList` / `getMovieCastList` **hoặc** thêm `@JsonAlias` + alias method (ít sạch hơn). |
| H3 | `MovieDTO.MovieCastDTO` vs `MovieServiceImpl` | Code set `castMemberName`, `castMemberBio`, `castMemberImageUrl` — DTO chỉ có `id`, `castMemberId`, `roleName`, `roleType` | Bổ sung field từ `CastMember` vào nested DTO hoặc map sang cấu trúc mới trong service. |
| H4 | `ShowtimeDTO` vs `ShowtimeServiceImpl`, `ShowtimeQueryService` | Thiếu toàn bộ field “flatten” cho API: `surcharge`, `movieTitle`, `moviePosterUrl`, `movieDurationMinutes`, `roomName`, `screenType`, `cinemaId`, `cinemaName`, … | Thêm các field vào `ShowtimeDTO` (read model) hoặc tách `ShowtimePublicDTO` / projection query. |
| H5 | `BookingDTO` vs `BookingServiceImpl` | Builder `customerId` — DTO có `userId`; `TicketLineDTO.seatCode` thiếu; `FnBLineDTO.itemId` vs `fnbItemId` | Align tên field với entity: `userId`, thêm `seatCode` nếu cần hiển thị, đổi `itemId` → `fnbItemId` hoặc ngược lại. |
| H6 | `CinemaDTO`, `LocationDTO`, `RoomDTO` | Thiếu `setLocationName`, `setLocationId`, `setCinemaName` so với service | Thêm field denormalized (tên địa điểm, rạp) hoặc chỉ trả id và để FE join. |
| H7 | `SeatDTO` vs `SeatServiceImpl` | Code dùng `seatRow`/`seatNumber` và type name/surcharge; entity `Seat` chỉ có `seatCode` + `seatType` | Cập nhật DTO mapping từ `seatCode` (parse row/number) hoặc thêm field vào entity nếu schema DB có. |
| H8 | `TicketDTO` vs `TicketServiceImpl` | Builder `movieTitle(...)` — `TicketDTO` không có field title | Thêm `movieTitle` (và các field hiển thị) hoặc bỏ khỏi builder. |
| H9 | `UserDTO` vs `UserServiceImpl` | Gọi `UserDTO.fromEntity(User)` — class không có factory method | Thêm `static UserDTO fromEntity(User u)` lấy email từ `user.getUserAccount()` hoặc map thủ công trong service. |

### MEDIUM

| # | File / vị trí | Mô tả | Gợi ý sửa |
|---|----------------|-------|-----------|
| M1 | `EmailServiceImpl`, `CheckoutServiceImpl`, `MomoServiceImpl`, `ConfirmedState` | `System.out.println` cho log | Dùng SLF4J `Logger` với level phù hợp; tránh in PII trong production. |
| M2 | Nhiều file `*Observer`, `ShowtimeServiceImpl`, `PricingConditions` | TODO chưa làm | Theo backlog; không chặn merge nếu compile xanh. |
| M3 | `data.sql` + entity | Sau khi ổn định entity, rà lại seed vs cột bảng | Chạy app + integration test; tránh lệch ENUM/cột. |

### LOW

| # | Mô tả |
|---|--------|
| L1 | Javadoc public API: nhiều DTO/service không có — ưu tiên thấp sau khi compile ổn. |
| L2 | `redisTemplate.keys` trong `VoucherServiceImpl` — với production Redis lớn có thể chậm; dùng scan hoặc index theo set (tùy yêu cầu). |

## Validation results

| Check | Result |
|--------|--------|
| `mvn compile` | **Fail** — mismatch DTO/entity/service như trên |
| `mvn test` | **Skipped** (không chạy được do compile fail) |
| Lint | Không chạy riêng; lỗi hiện tại là compiler |

## Mapping nhanh: entity hiện tại ↔ DTO cần thống nhất

- **`FnbItem`**: `fnbItemId` — DTO đặt `fnbItemId` (đúng hướng); controller cần bỏ `itemId` hoặc alias.
- **`Seat`**: `seatCode` — không có row/number tách; service/DTO phải parse từ `seatCode` hoặc đổi schema.
- **`MovieCast`**: quan hệ `CastMember`; DTO nested cần phản ánh field hiển thị (tên diễn viên, ảnh, bio) nếu API cần.
- **`Booking`**: `user`, `promotion` — DTO dùng `userId`, `promotionId` là hợp lý; builder phải khớp tên.

## Files đã đụng trong review (artifact)

- Khôi phục: `backend/src/main/java/com/cinema/booking/dtos/VoucherDTO.java`

## Khuyến nghị quy trình

1. **`mvn compile`** sau mỗi bước refactor entity.  
2. Ưu tiên **một nguồn sự thật** cho API: DTO response cho FE vs entity persistence — tránh xóa field DTO mà chưa sửa controller.  
3. Với `MovieCast`, chuẩn hóa **Java naming** (camelCase) + `@Column` cho DB.  
4. Khi xong compile, chạy `./mvnw test` và smoke test các API quan trọng (booking, showtime, movie).

---

*Report generated for local review mode (no uncommitted diff at review time).*
