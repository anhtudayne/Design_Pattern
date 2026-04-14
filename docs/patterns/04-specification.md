# Pattern 04 — Specification (Đặc Tả)

## 1. Lý thuyết

**Specification** thuộc nhóm Behavioral. Ý tưởng: đóng gói từng điều kiện lọc thành một object riêng biệt. Các điều kiện kết hợp qua `and()`, `or()`, `not()` để tạo query phức tạp **mà không viết SQL thủ công**.

```
Spec A  AND  Spec B  AND  Spec C  →  combined Specification → JPA Query
```

---

## 2. Vấn đề (Trước khi áp dụng)

Tìm suất chiếu theo cinemaId + movieId + date — query cứng trong Repository:

```java
// ❌ Trước
@Query("SELECT s FROM Showtime s WHERE " +
       "(:cinemaId IS NULL OR s.room.cinema.cinemaId = :cinemaId) AND " +
       "(:movieId IS NULL OR s.movie.movieId = :movieId) AND " +
       "(:date IS NULL OR DATE(s.startTime) = :date)")
List<Showtime> findByFilters(Integer cinemaId, Integer movieId, LocalDate date);
```

**Hậu quả:**
- Thêm filter mới → phải sửa `@Query` string — dễ lỗi SQL, vi phạm OCP
- Không tái sử dụng được từng điều kiện riêng lẻ
- Test khó — phụ thuộc DB thật

---

## 3. Giải pháp — Specification + JpaSpecificationExecutor

Mỗi điều kiện là một static method trả `Specification<Showtime>`. Service kết hợp chúng động. Repository chỉ cần extend thêm `JpaSpecificationExecutor`.

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `ShowtimeSpecifications.java` | `patterns/specification/` | **Factory** — chứa tất cả điều kiện |
| `ShowtimeRepository.java` | `repositories/` | Extend `JpaSpecificationExecutor<Showtime>` |
| `ShowtimeServiceImpl.java` | `services/impl/` | Kết hợp các Spec, gọi repository |

---

## 5. Code thực tế

### `ShowtimeSpecifications.java` (toàn bộ)

```java
public final class ShowtimeSpecifications {

    private ShowtimeSpecifications() {}

    /** Join Showtime → room → cinema */
    public static Specification<Showtime> hasCinemaId(Integer cinemaId) {
        return (root, query, cb) -> {
            if (cinemaId == null) return cb.conjunction();  // null = bỏ qua
            return cb.equal(root.get("room").get("cinema").get("cinemaId"), cinemaId);
        };
    }

    /** Join Showtime → movie */
    public static Specification<Showtime> hasMovieId(Integer movieId) {
        return (root, query, cb) -> {
            if (movieId == null) return cb.conjunction();
            return cb.equal(root.get("movie").get("movieId"), movieId);
        };
    }

    /** startTime trong khoảng [date 00:00, date+1 00:00) */
    public static Specification<Showtime> onDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end   = date.plusDays(1).atStartOfDay();
            return cb.and(
                cb.greaterThanOrEqualTo(root.get("startTime"), start),
                cb.lessThan(root.get("startTime"), end)
            );
        };
    }
}
```

### Repository — thêm 1 interface

```java
public interface ShowtimeRepository
        extends JpaRepository<Showtime, Integer>,
                JpaSpecificationExecutor<Showtime> {  // dòng này là đủ
}
```

### Service — kết hợp Specification

```java
// ✅ Sau — đọc như tiếng Anh
public List<ShowtimeDTO> getShowtimes(Integer cinemaId, Integer movieId, LocalDate date) {
    Specification<Showtime> spec = Specification
        .where(ShowtimeSpecifications.hasCinemaId(cinemaId))
        .and(ShowtimeSpecifications.hasMovieId(movieId))
        .and(ShowtimeSpecifications.onDate(date));

    return showtimeRepository.findAll(spec)
        .stream().map(this::toDTO).collect(Collectors.toList());
}
```

---

## 6. Luồng hoạt động thực tế

```
GET /api/showtimes?cinemaId=1&movieId=5&date=2025-01-01
        │
        ▼
ShowtimeServiceImpl.getShowtimes(1, 5, LocalDate.of(2025,1,1))
        │
        ├── hasCinemaId(1)       → WHERE s.room.cinema.id = 1
        ├── hasMovieId(5)        → AND s.movie.id = 5
        └── onDate(2025-01-01)   → AND s.startTime BETWEEN 00:00 AND 23:59
                │
                ▼
        showtimeRepository.findAll(spec)
                │ Hibernate tạo SQL động
                ▼
        List<Showtime> → List<ShowtimeDTO>
```

---

## 7. SOLID

| | Chi tiết |
|-|---------|
| **S** | `hasCinemaId`, `hasMovieId`, `onDate` — mỗi method 1 điều kiện duy nhất |
| **O** | Thêm filter "lọc theo giờ" → thêm method `afterTime()` mới, không sửa gì cũ |
| **D** | Service dùng `Specification<T>` (interface JPA), không viết SQL cứng |

---

## 8. Thành quả

| Trước | Sau |
|-------|-----|
| `@Query` string cứng, dễ lỗi typo SQL | Kết hợp Specification động — null = bỏ qua filter |
| Thêm filter → sửa SQL trong Repository | Thêm static method mới, không sửa gì cũ |
| Test cần DB thật | Test từng Specification bằng CriteriaBuilder mock |
| Logic filter trộn vào SQL string | Service đọc rõ ràng: `hasCinemaId(1).and(onDate(date))` |

---

## 9. Test thủ công

```bash
# Lọc đầy đủ 3 điều kiện
GET /api/showtimes?cinemaId=1&movieId=5&date=2025-01-15

# Chỉ lọc ngày
GET /api/showtimes?date=2025-01-15

# Không filter — trả tất cả
GET /api/showtimes

# Xem SQL sinh ra (bật logging)
# application.properties: spring.jpa.show-sql=true
```
