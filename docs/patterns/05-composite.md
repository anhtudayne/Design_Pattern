# Pattern 05 — Composite (Hợp Thể)

## 1. Lý thuyết

**Composite** thuộc nhóm Structural. Ý tưởng: cho phép xử lý **một đối tượng đơn lẻ** (Leaf) và **một nhóm đối tượng** (Composite) theo cùng một cách — qua interface chung. Client không cần biết đang làm việc với 1 object hay cả cây.

```
Component (interface)
    ├── Leaf (xử lý thực tế)
    └── Composite (chứa nhiều Leaf/Composite, gọi đệ quy)
```

---

## 2. Vấn đề (Trước khi áp dụng)

Admin Dashboard cần hiển thị thống kê tổng hợp: số phim, user, suất chiếu, món F&B, vé, khuyến mãi, doanh thu. Controller gọi nhiều service/repository khác nhau:

```java
// ❌ Trước — Controller làm quá nhiều việc
@GetMapping("/stats")
public ResponseEntity<?> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();

    stats.put("totalMovies", movieService.count());
    stats.put("totalUsers", userService.count());
    stats.put("totalShowtimes", showtimeService.count());
    stats.put("totalFnbItems", fnbItemRepository.count());
    stats.put("totalTickets", ticketRepository.count());
    stats.put("totalPromotions", promotionRepository.count());
    stats.put("totalRevenue", paymentService.sumSuccessfulPayments());

    return ResponseEntity.ok(stats);
}
```

**Hậu quả:**
- Controller phụ thuộc nhiều service/repository — coupling cao, vi phạm SRP
- Thêm loại thống kê mới → phải mở Controller ra sửa — vi phạm OCP
- Khó test Controller đơn lẻ
- Không tái sử dụng được từng nhóm stats

---

## 3. Giải pháp — Composite Pattern

Mỗi loại thống kê là một `Leaf`. `DashboardStatsComposite` chứa tất cả Leaf và điều phối. Controller chỉ gọi 1 dòng: `composite.collect(map)`.

---

## 4. Các file trong dự án

Package: `com.cinema.booking.pattern.composite`

| File | Vai trò |
|------|---------|
| `StatsComponent.java` | **Interface** — contract cho Leaf và Composite |
| `DashboardStatsComposite.java` | **Composite** — nhận `List<StatsComponent>` từ Spring, lọc bỏ chính nó, gọi `collect` lần lượt |
| `MovieStatsLeaf.java` | Leaf: đếm phim |
| `UserStatsLeaf.java` | Leaf: đếm user |
| `ShowtimeStatsLeaf.java` | Leaf: đếm suất chiếu |
| `FnbStatsLeaf.java` | Leaf: đếm món F&B (`totalFnbItems`) |
| `TicketStatsLeaf.java` | Leaf: đếm vé |
| `PromotionStatsLeaf.java` | Leaf: đếm khuyến mãi |
| `RevenueStatsLeaf.java` | Leaf: tổng doanh thu từ thanh toán thành công |

---

## 5. Code thực tế

### Interface — `StatsComponent.java`

```java
public interface StatsComponent {
    void collect(Map<String, Object> target);
}
```

### Composite — `DashboardStatsComposite.java`

```java
// import java.util.stream.Collectors;

@Component
public class DashboardStatsComposite implements StatsComponent {

    private final List<StatsComponent> children;

    @Autowired
    public DashboardStatsComposite(List<StatsComponent> allComponents) {
        this.children = allComponents.stream()
                .filter(c -> !(c instanceof DashboardStatsComposite))
                .collect(Collectors.toList());
    }

    @Override
    public void collect(Map<String, Object> target) {
        for (StatsComponent child : children) {
            child.collect(target);
        }
    }
}
```

### Leaf ví dụ — `MovieStatsLeaf.java`

```java
@Component
@RequiredArgsConstructor
public class MovieStatsLeaf implements StatsComponent {
    private final MovieRepository movieRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalMovies", movieRepository.count());
    }
}
```

### Kết quả — Controller sau refactor

```java
// ✅ Sau — gom thống kê qua composite
@Autowired
private DashboardStatsComposite dashboardStatsComposite;

@GetMapping("/stats")
public ResponseEntity<Map<String, Object>> getStats() {
    Map<String, Object> stats = new HashMap<>();
    dashboardStatsComposite.collect(stats);
    return ResponseEntity.ok(stats);
}
```

---

## 6. Luồng hoạt động thực tế

```
GET /api/admin/dashboard/stats
        │
        ▼
DashboardController.getStats()
        │── new HashMap<>()
        │── dashboardStatsComposite.collect(stats)
        │       │
        │       ├── MovieStatsLeaf.collect()      → stats["totalMovies"]
        │       ├── UserStatsLeaf.collect()       → stats["totalUsers"]
        │       ├── ShowtimeStatsLeaf.collect()   → stats["totalShowtimes"]
        │       ├── FnbStatsLeaf.collect()        → stats["totalFnbItems"]
        │       ├── TicketStatsLeaf.collect()     → stats["totalTickets"]
        │       ├── PromotionStatsLeaf.collect()  → stats["totalPromotions"]
        │       └── RevenueStatsLeaf.collect()    → stats["totalRevenue"]
        │
        └── return stats (7 keys)
```

---

## 7. SOLID

| | Chi tiết |
|-|---------|
| **S** | `MovieStatsLeaf` chỉ đếm phim; `RevenueStatsLeaf` chỉ tính doanh thu |
| **O** | Thêm thống kê → tạo `AdsStatsLeaf` mới (`@Component` implements `StatsComponent`); Spring inject vào `List` — **không** cần sửa `DashboardStatsComposite` |
| **I** | `StatsComponent` chỉ 1 method `collect()` — gọn nhất có thể |
| **D** | Controller inject `DashboardStatsComposite` (implements `StatsComponent`) |

---

## 8. Thành quả

| Trước | Sau |
|-------|-----|
| Controller phụ thuộc nhiều dependency | Controller chỉ phụ thuộc 1 Composite |
| Thêm stats → sửa Controller | Tạo Leaf `@Component` mới — DI tự gom |
| Khó test từng loại stats riêng | Test từng Leaf riêng với 1 Repository mock |
| Controller 30+ dòng | Controller 3 dòng |

---

## 9. Test thủ công

Đoạn dưới đây là **mô tả request/response (HTTP)** — **không** phải lệnh terminal. Nếu bạn copy toàn bộ vào zsh/bash, shell sẽ báo `command not found` vì nó cố chạy `GET`, `Authorization:`... như lệnh.

**Cách dùng:** Swagger UI (`/swagger-ui.html`), Postman, Insomnia, hoặc `curl` như sau (thay token thật):

```bash
curl -sS -H "Authorization: Bearer YOUR_ADMIN_JWT" \
  http://localhost:8080/api/admin/dashboard/stats
```

**Ví dụ response (JSON):**

```json
{
  "totalMovies": 42,
  "totalUsers": 1500,
  "totalShowtimes": 88,
  "totalFnbItems": 120,
  "totalTickets": 5000,
  "totalPromotions": 15,
  "totalRevenue": 85000000
}
```
