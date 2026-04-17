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
| [`05-composite.md`](./05-composite.md) | Composite | Tổng hợp số liệu Dashboard từ nhiều Leaf |
| [`06-singleton.md`](./06-singleton.md) | Singleton (Spring IoC) | Chia sẻ RestTemplate bean duy nhất |
| [`08-dynamic-pricing-engine.md`](./08-dynamic-pricing-engine.md) | Specification + Strategy + Decorator + CoR | Dynamic Pricing Engine — tính giá vé thông minh |
