# UML Pattern-Only — Chỉ chứa Design Pattern

Thư mục này chứa 8 file UML **chỉ vẽ các class thuộc design pattern** — không gộp domain classdiagram từ [`classdiagram.md`](../../classdiagram.md).

So sánh với thư mục cha (`UML/`):

| Thư mục | Nội dung |
|---------|----------|
| `UML/*.md` | Domain classdiagram đầy đủ + Pattern (cả 2 gộp chung) |
| `UML/pattern-only/*.md` | **Chỉ Pattern** — gọn, tập trung vào design pattern |

---

| File | Pattern | Mô tả |
|------|---------|--------|
| [`01-chain-of-responsibility.md`](./01-chain-of-responsibility.md) | Chain of Responsibility | Validate checkout request qua chuỗi handler |
| [`02-mediator.md`](./02-mediator.md) | Mediator | Điều phối các tác vụ sau thanh toán MoMo |
| [`03-proxy.md`](./03-proxy.md) | Proxy (Caching) | Cache danh sách phim bằng Redis |
| [`04-specification.md`](./04-specification.md) | Specification | Lọc Showtime và tìm kiếm Booking linh hoạt |
| [`05-composite.md`](./05-composite.md) | Composite | Tổng hợp số liệu Dashboard từ nhiều Leaf |
| [`06-singleton.md`](./06-singleton.md) | Singleton (Spring IoC) | Chia sẻ RestTemplate bean duy nhất |
| [`07-prototype.md`](./07-prototype.md) | Prototype | Clone email template, điền dữ liệu thực |
| [`08-dynamic-pricing-engine.md`](./08-dynamic-pricing-engine.md) | Specification + Strategy + Decorator + CoR | Dynamic Pricing Engine — tính giá vé thông minh |
