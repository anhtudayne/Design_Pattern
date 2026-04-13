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

Admin Dashboard cần hiển thị thống kê tổng hợp: số phim, số user, số suất chiếu, doanh thu F&B, số voucher, tổng doanh thu. Controller gọi 6 service khác nhau:

```java
// ❌ Trước — Controller làm quá nhiều việc
@GetMapping("/stats")
public ResponseEntity<?> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();

    stats.put("totalMovies", movieService.count());
    stats.put("totalUsers", userService.count());
    stats.put("totalShowtimes", showtimeService.count());
    stats.put("totalFnbRevenue", fnbService.getTotalRevenue());
    stats.put("totalVouchers", voucherService.count());
    stats.put("totalRevenue", bookingService.getTotalRevenue());

    return ResponseEntity.ok(stats);
}
```

**Hậu quả:**
- Controller phụ thuộc 6 service — coupling cao, vi phạm SRP
- Thêm loại thống kê mới → phải mở Controller ra sửa — vi phạm OCP
- Khó test Controller đơn lẻ
- Không tái sử dụng được từng nhóm stats

---

## 3. Giải pháp — Composite Pattern

Mỗi loại thống kê là một `Leaf`. `DashboardStatsComposite` chứa tất cả Leaf và điều phối. Controller chỉ gọi 1 dòng: `composite.collect(map)`.

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `StatsComponent.java` | `patterns/composite/` | **Interface** — contract cho Leaf và Composite |
| `DashboardStatsComposite.java` | `patterns/composite/` | **Composite** — chứa và điều phối tất cả Leaf |
| `MovieStatsLeaf.java` | `patterns/composite/` | Leaf: đếm số phim |
| `UserStatsLeaf.java` | `patterns/composite/` | Leaf: đếm số user |
| `ShowtimeStatsLeaf.java` | `patterns/composite/` | Leaf: đếm suất chiếu |
| `FnbStatsLeaf.java` | `patterns/composite/` | Leaf: doanh thu F&B |
| `VoucherStatsLeaf.java` | `patterns/composite/` | Leaf: đếm voucher |
| `RevenueStatsLeaf.java` | `patterns/composite/` | Leaf: tổng doanh thu booking |

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
@Component
public class DashboardStatsComposite implements StatsComponent {

    private final List<StatsComponent> children;

    @Autowired
    public DashboardStatsComposite(
            MovieStatsLeaf movieStatsLeaf,
            UserStatsLeaf userStatsLeaf,
            ShowtimeStatsLeaf showtimeStatsLeaf,
            FnbStatsLeaf fnbStatsLeaf,
            VoucherStatsLeaf voucherStatsLeaf,
            RevenueStatsLeaf revenueStatsLeaf) {
        this.children = Arrays.asList(
                movieStatsLeaf, userStatsLeaf, showtimeStatsLeaf,
                fnbStatsLeaf, voucherStatsLeaf, revenueStatsLeaf
        );
    }

    @Override
    public void collect(Map<String, Object> target) {
        for (StatsComponent child : children) {
            child.collect(target);  // mỗi Leaf tự điền phần của mình
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
// ✅ Sau — 1 dòng duy nhất
@Autowired
private DashboardStatsComposite dashboardStatsComposite;

@GetMapping("/stats")
public ResponseEntity<?> getDashboardStats() {
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
AdminController.getDashboardStats()
        │── new HashMap<>()
        │── dashboardStatsComposite.collect(stats)
        │       │
        │       ├── MovieStatsLeaf.collect()    → stats["totalMovies"] = 42
        │       ├── UserStatsLeaf.collect()     → stats["totalUsers"] = 1500
        │       ├── ShowtimeStatsLeaf.collect() → stats["totalShowtimes"] = 88
        │       ├── FnbStatsLeaf.collect()      → stats["totalFnbRevenue"] = 15000000
        │       ├── VoucherStatsLeaf.collect()  → stats["totalVouchers"] = 200
        │       └── RevenueStatsLeaf.collect()  → stats["totalRevenue"] = 85000000
        │
        └── return stats (map đầy đủ 6 keys)
```

---

## 7. SOLID

| | Chi tiết |
|-|---------|
| **S** | `MovieStatsLeaf` chỉ đếm phim; `RevenueStatsLeaf` chỉ tính doanh thu |
| **O** | Thêm thống kê quảng cáo → tạo `AdsStatsLeaf` mới + thêm vào Composite constructor |
| **I** | `StatsComponent` chỉ 1 method `collect()` — gọn nhất có thể |
| **D** | Controller inject `DashboardStatsComposite` (implements `StatsComponent`) |

---

## 8. Thành quả

| Trước | Sau |
|-------|-----|
| Controller phụ thuộc 6 service | Controller chỉ phụ thuộc 1 Composite |
| Thêm stats → sửa Controller | Tạo Leaf mới + thêm vào Composite |
| Khó test từng loại stats riêng | Test từng Leaf riêng với 1 Repository mock |
| Controller 30+ dòng | Controller 3 dòng |

---

## 9. Test thủ công

```bash
GET /api/admin/dashboard/stats
Authorization: Bearer {admin_token}

# Expected response:
{
  "totalMovies": 42,
  "totalUsers": 1500,
  "totalShowtimes": 88,
  "totalFnbRevenue": 15000000,
  "totalVouchers": 200,
  "totalRevenue": 85000000
}
```
