# Báo Cáo Tổng Hợp — Dự Án Áp Dụng Design Pattern

> **Tên dự án:** StarCine Backend — Hệ thống đặt vé xem phim  
> **Sinh viên:** [Tên bạn]  
> **Môn học:** Thiết kế hướng đối tượng / Mô hình Thiết kế  
> **Ngày báo cáo:** Tháng 4/2026

---

## 1. Tổng Quan Dự Án

### 1.1 Giới thiệu

StarCine là hệ thống đặt vé xem phim trực tuyến được xây dựng bằng **Spring Boot (Java)**. Dự án áp dụng **8 Design Pattern** từ cuốn *Design Patterns: Elements of Reusable Object-Oriented Software* (Gang of Four) trong môi trường thực tế, giải quyết các bài toán nghiệp vụ cụ thể.

### 1.2 Kiến trúc tổng thể

```
backend/src/main/java/com/cinema/booking/
├── controllers/        REST API endpoints
├── services/           Business logic layer
│   ├── impl/
│   ├── factory/                 Pattern: Factory Method (BookingFactory)
│   ├── seatlock/                Pattern: Adapter (SeatLockProvider ← RedisSeatLockAdapter)
│   ├── strategy_decorator/
│   │   └── pricing/             Pattern: Strategy + Decorator (PricingEngine)
│   ├── template_method/
│   │   └── checkout/            Pattern: Template Method (AbstractCheckoutTemplate)
│   └── payment/                 Pattern: Strategy + Factory (PaymentStrategy)
├── domain/
│   └── seat/                    Pattern: State (SeatState, VacantSeatState, ...)
├── repositories/       Data access (Spring Data JPA)
├── entities/           Domain model (JPA entities)
├── dtos/               Data Transfer Objects
├── config/             Spring configuration
└── patterns/           Remaining Design Pattern implementations
    ├── composite/               Pattern 05
    └── state/                   Pattern: State (BookingContext, BookingState, ...)
```

### 1.3 Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Backend Framework | Spring Boot 3.x |
| Ngôn ngữ | Java 17+ |
| Database | MySQL (JPA/Hibernate) |
| Cache | Redis |
| Build tool | Maven |
| Test framework | JUnit 5 + AssertJ |

---

## 2. Danh Sách Design Pattern Đã Triển Khai

| # | Pattern | Loại (GoF) | Bài toán giải quyết | Package |
|---|---------|-----------|---------------------|---------|
| 05 | **Composite** | Structural | Tổng hợp số liệu dashboard từ nhiều nguồn | `patterns.composite` |
| 06 | **Singleton** | Creational | Chia sẻ `RestTemplate` bean qua Spring IoC | `config` |
| 08 | **Dynamic Pricing Engine** | Tổng hợp nhiều pattern | Tính giá vé: Strategy + Decorator + Specification | `services.strategy_decorator.pricing` |

---

## 3. Kết Quả Test

### 3.1 Tổng hợp kết quả

```
Tổng số test (pattern unit test): 9 tests (PricingConditionsTest)
✅ PASS: 9 / 9
❌ FAIL: 0
⚠️  SKIP: 0
Thời gian chạy: ~1 giây (không cần DB/Redis)
```

> Ghi chú: `FilmBookingBackendApplicationTests` (Spring full context) không được tính vào đây vì yêu cầu kết nối DB/Redis thật. Các test pattern đều chạy hoàn toàn độc lập, không cần infrastructure.

### 3.2 Kết quả theo từng Pattern (08)

| Pattern | Test Class | Số test | Kết quả |
|---------|-----------|---------|---------|
| Specification (PricingConditions) | `PricingConditionsTest` | 9 | ✅ ALL PASS |
| **TỔNG** | | **9** | **✅ 100%** |

---

## 4. SOLID Principles — Kiểm Tra Toàn Bộ

| Nguyên tắc | Áp dụng trong dự án |
|------------|---------------------|
| **S** — Single Responsibility | Mỗi Handler (CoR) 1 rule; mỗi Decorator 1 khoản tiền; mỗi Strategy 1 công thức |
| **O** — Open/Closed | Thêm Decorator/Handler/Strategy mới = thêm class mới, không sửa class cũ |
| **L** — Liskov Substitution | Proxy thay thế MovieServiceImpl hoàn toàn; DiscountComponent decorator thay thế nhau linh hoạt |
| **I** — Interface Segregation | `DiscountComponent` (1 method), `PricingStrategy` (1 method), `StatsComponent` (1 method) |
| **D** — Dependency Inversion | Tất cả service inject abstraction; Spring IoC quản lý concrete impl |

---

## 5. Hướng Dẫn Tra Cứu Báo Cáo Chi Tiết

Để xem chi tiết từng phần, cô có thể mở các file sau:

| File | Nội dung |
|------|---------|
| [`docs/bao-cao-dynamic-pricing.md`](bao-cao-dynamic-pricing.md) | Dynamic Pricing Engine (08): nhiều pattern kết hợp |
| [`UML/08-dynamic-pricing-engine.md`](../UML/08-dynamic-pricing-engine.md) | UML Class Diagram đầy đủ (Mermaid) |
| [`docs/patterns/08-dynamic-pricing-engine.md`](patterns/08-dynamic-pricing-engine.md) | Tài liệu kỹ thuật chi tiết |
| [`docs/patterns/composite-dashboard-stats-report-bo-cuc-mau.md`](patterns/composite-dashboard-stats-report-bo-cuc-mau.md) | Báo cáo ngắn Composite (bố cục mẫu 08) |
| [`docs/patterns/composite-dashboard-stats-package-vi.md`](patterns/composite-dashboard-stats-package-vi.md) | Composite: giải thích package + từng file |
| [`docs/patterns/05-composite.md`](patterns/05-composite.md) | Lý thuyết Composite trong dự án |
| [`docs/patterns/singleton-resttemplate-report-bo-cuc-mau.md`](patterns/singleton-resttemplate-report-bo-cuc-mau.md) | Báo cáo ngắn Singleton / `RestTemplate` (bố cục mẫu 08) |
| [`docs/patterns/singleton-resttemplate-package-vi.md`](patterns/singleton-resttemplate-package-vi.md) | Singleton: Spring bean `RestTemplate` + `MomoServiceImpl` |
| [`docs/patterns/06-singleton.md`](patterns/06-singleton.md) | Lý thuyết Singleton (Spring IoC) |
| [`docs/patterns/README-bao-cao-word-composite-singleton.md`](patterns/README-bao-cao-word-composite-singleton.md) | Mục lục báo cáo ngắn + tùy chọn xuất Word |

**Lưu ý:** Các pattern 01-04 và 07 đã được xóa khỏi dự án. Chỉ còn lại pattern 05 (Composite), 06 (Singleton), và 08 (Dynamic Pricing Engine).

---

## 6. Điểm Nổi Bật Kỹ Thuật

### 6.1 Dynamic Pricing Engine (tính năng nâng cao)

Kết hợp **nhiều GoF design pattern** hoạt động thực sự trong production path:

- **Specification** (`PricingConditions`): 4 predicate điều kiện — tích hợp thực sự qua `TimeBasedPricingStrategy`
- **Strategy** (`TicketPricingStrategy`, `FnbPricingStrategy`, `TimeBasedPricingStrategy`): tách biệt từng loại tính giá, không gọi DB trong engine
- **Decorator** (`DiscountComponent` chain): `NoDiscount` → `PromotionDiscountDecorator` → `MemberDiscountDecorator` — validation ở đúng tầng CoR

**Orchestrator thực tế:** `BookingServiceImpl → PricingEngine`.

**`PriceBreakdownDTO` 7 trường:** `ticketTotal`, `timeBasedSurcharge`, `fnbTotal`, `membershipDiscount`, `discountAmount`, `appliedStrategy`, `finalTotal`.

### 6.2 Không cần Spring context để test

Test unit `PricingConditionsTest` (9 test) chạy trong **dưới 1 giây** mà không cần kết nối DB hay Redis. Logic nghiệp vụ hoàn toàn tách biệt khỏi infrastructure.

### 6.3 Backward Compatible API

`PriceBreakdownDTO` trả về 4 field cốt lõi (`ticketTotal`, `fnbTotal`, `discountAmount`, `finalTotal`) — Frontend/Mobile không bị lỗi khi Backend cập nhật.

---

## 7. Cấu Trúc Thư Mục Báo Cáo

```
Design_Pattern/
├── docs/
│   ├── bao-cao-tong-hop.md          ← File này (tổng quan)
│   ├── bao-cao-dynamic-pricing.md   ← Chi tiết Dynamic Pricing Engine
│   └── patterns/
│       ├── 05-composite.md, 06-singleton.md, 08-dynamic-pricing-engine.md
│       ├── composite-dashboard-stats-report-bo-cuc-mau.md
│       ├── composite-dashboard-stats-package-vi.md
│       ├── singleton-resttemplate-report-bo-cuc-mau.md
│       ├── singleton-resttemplate-package-vi.md
│       └── README-bao-cao-word-composite-singleton.md
├── UML/
│   └── 08-dynamic-pricing-engine.md
├── plans/
│   ├── 00-patterns-conventions.md   ← Quy ước chung
│   └── 08 → 13 ...                 ← Plan cho Dynamic Pricing (6 phase)
└── backend/
    └── src/
        ├── main/java/               ← Source code
        └── test/java/               ← Test code
```
