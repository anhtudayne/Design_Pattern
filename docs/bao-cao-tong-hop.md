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
│   └── impl/
├── repositories/       Data access (Spring Data JPA)
├── entities/           Domain model (JPA entities)
├── dtos/               Data Transfer Objects
├── config/             Spring configuration
└── patterns/           Design Pattern implementations
    ├── chainofresponsibility/   Pattern 01
    ├── mediator/                Pattern 02
    ├── proxy/                   Pattern 03 + 08 (Proxy Cache)
    ├── specification/           Pattern 04 + 08 (PricingConditions)
    ├── composite/               Pattern 05
    ├── prototype/               Pattern 07
    └── pricing/                 Pattern 08 (Dynamic Pricing Engine)
        ├── context/
        ├── strategy/
        ├── decorator/
        └── validation/
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
| 01 | **Chain of Responsibility** | Behavioral | Validate checkout request theo chuỗi rule | `patterns.chainofresponsibility` |
| 02 | **Mediator** | Behavioral | Điều phối các tác vụ sau thanh toán thành công | `patterns.mediator` |
| 03 | **Proxy** | Structural | Cache danh sách phim bằng Redis | `patterns.proxy` |
| 04 | **Specification** | Behavioral | Lọc suất chiếu theo nhiều điều kiện linh hoạt | `patterns.specification` |
| 05 | **Composite** | Structural | Tổng hợp số liệu dashboard từ nhiều nguồn | `patterns.composite` |
| 06 | **Singleton** | Creational | Chia sẻ `RestTemplate` bean qua Spring IoC | `config` |
| 07 | **Prototype** | Creational | Tạo email template theo từng loại giao tiếp | `patterns.prototype` |
| 08 | **Dynamic Pricing Engine** | Tổng hợp 5 pattern | Tính giá vé thông minh đa yếu tố | `patterns.pricing` |

---

## 3. Kết Quả Test

### 3.1 Tổng hợp kết quả

```
Tổng số test (pattern unit test): 67 tests
✅ PASS: 67 / 67
❌ FAIL: 0
⚠️  SKIP: 0
Thời gian chạy: ~6 giây (không cần DB/Redis)
```

> Ghi chú: `FilmBookingBackendApplicationTests` (Spring full context) không được tính vào đây vì yêu cầu kết nối DB/Redis thật. Các test pattern đều chạy hoàn toàn độc lập, không cần infrastructure.

### 3.2 Kết quả theo từng Pattern (08)

| Pattern | Test Class | Số test | Kết quả |
|---------|-----------|---------|---------|
| Specification (Pricing) | `PricingConditionsTest` | 9 | ✅ ALL PASS |
| Strategy | `PricingStrategyTest` | 21 | ✅ ALL PASS |
| Decorator | `DecoratorTest` | 9 | ✅ ALL PASS |
| CoR Validation | `PriceValidationChainTest` | 10 | ✅ ALL PASS |
| Proxy Cache | `CachingDynamicPricingProxyTest` | 9 | ✅ ALL PASS |
| Service (E2E) | `DynamicPricingServiceImplTest` | 9 | ✅ ALL PASS |
| **TỔNG** | | **67** | **✅ 100%** |

---

## 4. SOLID Principles — Kiểm Tra Toàn Bộ

| Nguyên tắc | Áp dụng trong dự án |
|------------|---------------------|
| **S** — Single Responsibility | Mỗi Handler (CoR) 1 rule; mỗi Decorator 1 khoản tiền; mỗi Strategy 1 công thức |
| **O** — Open/Closed | Thêm Decorator/Handler/Strategy mới = thêm class mới, không sửa class cũ |
| **L** — Liskov Substitution | Proxy thay thế MovieServiceImpl hoàn toàn; Decorator thay thế PriceCalculator |
| **I** — Interface Segregation | `PriceCalculator` (1 method), `PricingStrategy` (4 method hẹp), `StatsComponent` (1 method) |
| **D** — Dependency Inversion | Tất cả service inject abstraction; Spring IoC quản lý concrete impl |

---

## 5. Hướng Dẫn Tra Cứu Báo Cáo Chi Tiết

Để xem chi tiết từng phần, cô có thể mở các file sau:

| File | Nội dung |
|------|---------|
| [`docs/bao-cao-pattern-1-7.md`](bao-cao-pattern-1-7.md) | Chi tiết 7 pattern (01-07): bài toán, thiết kế, code, test |
| [`docs/bao-cao-dynamic-pricing.md`](bao-cao-dynamic-pricing.md) | Dynamic Pricing Engine (08): 5 pattern kết hợp, 67 test |
| [`UML/08-dynamic-pricing-engine.md`](../UML/08-dynamic-pricing-engine.md) | UML Class Diagram đầy đủ (Mermaid) |
| [`docs/patterns/08-dynamic-pricing-engine.md`](patterns/08-dynamic-pricing-engine.md) | Tài liệu kỹ thuật chi tiết |

---

## 6. Điểm Nổi Bật Kỹ Thuật

### 6.1 Dynamic Pricing Engine (tính năng nâng cao)

Tính năng cộng điểm — kết hợp **5 design pattern** để giải quyết bài toán tính giá vé phức tạp:

- **Specification**: 4 predicate điều kiện (weekend, holiday, early bird, occupancy)
- **Strategy**: 4 chiến lược giá theo thứ tự ưu tiên (OCP — thêm mới không sửa cũ)
- **Decorator**: 4 lớp cộng/trừ tiền độc lập, xếp chồng linh hoạt
- **Chain of Responsibility**: 3 rule validation (an toàn, chống gian lận)
- **Proxy**: Redis cache 5 phút (giảm tải DB/CPU)

### 6.2 Không cần Spring context để test

Toàn bộ 67 test unit đều chạy trong **dưới 6 giây** mà không cần kết nối DB hay Redis. Điều này thể hiện thiết kế clean: logic nghiệp vụ hoàn toàn tách biệt khỏi infrastructure.

### 6.3 Backward Compatible API

`PriceBreakdownDTO` được mở rộng với các field mới (`occupancySurcharge`, `membershipDiscount`, `appliedStrategy`) nhưng giữ nguyên 4 field cũ → Frontend/Mobile không bị lỗi khi Backend cập nhật.

---

## 7. Cấu Trúc Thư Mục Báo Cáo

```
Design_Pattern/
├── docs/
│   ├── bao-cao-tong-hop.md          ← File này (tổng quan)
│   ├── bao-cao-pattern-1-7.md       ← Chi tiết 7 pattern cơ bản
│   ├── bao-cao-dynamic-pricing.md   ← Chi tiết Dynamic Pricing Engine
│   └── patterns/
│       ├── 01-chain-of-responsibility.md
│       ├── 02-mediator.md
│       ├── ...
│       └── 08-dynamic-pricing-engine.md
├── UML/
│   ├── 01-chain-of-responsibility.md
│   ├── ...
│   └── 08-dynamic-pricing-engine.md
├── plans/
│   ├── 00-patterns-conventions.md   ← Quy ước chung
│   ├── 01 → 07 ...                 ← Plan cho từng pattern
│   └── 08 → 13 ...                 ← Plan cho Dynamic Pricing (6 phase)
└── backend/
    └── src/
        ├── main/java/               ← Source code
        └── test/java/               ← Test code
```
