# Kịch bản báo cáo — Composite (UML pattern only)

> **Sơ đồ tham chiếu:** [UML/pattern-only/05-composite.md](../../UML/pattern-only/05-composite.md)  
> **Tài liệu chi tiết:** [05-composite.md](05-composite.md) · [composite-dashboard-stats-package-vi.md](composite-dashboard-stats-package-vi.md)

**Cách dùng:** Bài báo cáo đi theo **luồng một request thống kê dashboard**: từ HTTP vào controller, qua composite, tới từng leaf. Mỗi bước có **Lời thoại** và **Ghi chú** (chỉ UML / nói ngoài).

---

## Giới thiệu ngắn

**Lời thoại:** Composite được dùng cho **API thống kê tổng quan admin**: một lần gọi cần gom nhiều chỉ số khác nhau. Thiết kế dùng interface `StatsComponent` làm hợp đồng chung; `DashboardStatsComposite` điều phối; các lớp `*StatsLeaf` mỗi lớp lo một phần dữ liệu. Sơ đồ pattern only thể hiện đúng thứ tự vai trò từ interface xuống controller.

**Ghi chú:** Hành động: mở [UML/pattern-only/05-composite.md](../../UML/pattern-only/05-composite.md). Nói ngoài: endpoint `GET /api/admin/dashboard/stats` — khớp [`DashboardController`](../../backend/src/main/java/com/cinema/booking/controller/DashboardController.java).

---

## Luồng hoạt động — từ request đến JSON thống kê

### Bước 1 — `DashboardController` nhận HTTP và chuẩn bị map kết quả

**Lời thoại:** Client gọi API lấy thống kê. `DashboardController` tạo một `HashMap` rỗng — đây sẽ là **vùng đích chung** mà mọi thành phần thống kê cùng ghi vào. Controller không tự đi gọi từng repository; nó ủy quyền cho một đối tượng composite duy nhất.

**Ghi chú:** Hành động: trên UML, chỉ `DashboardController` và method `getStats` / quan hệ tới `DashboardStatsComposite`. Nói ngoài: trong code, `getStats` tạo `stats` rồi gọi `dashboardStatsComposite.collect(stats)`.

### Bước 2 — `DashboardStatsComposite.collect` điều phối danh sách con

**Lời thoại:** `DashboardStatsComposite` đã được Spring inject sẵn danh sách mọi bean implement `StatsComponent`, nhưng **lúc khởi tạo** nó đã lọc bỏ chính nó khỏi list để tránh vòng lặp vô hạn. Khi `collect` chạy, composite **lần lượt** gọi `collect` trên từng phần tử trong `children`, truyền cùng một tham chiếu map — nên mỗi bước chỉ **bổ sung** key mới, không tạo map mới.

**Ghi chú:** Hành động: chỉ `DashboardStatsComposite`, field `children`, composition `children (DI)`, realization tới `StatsComponent`. Nói ngoài: [`DashboardStatsComposite.java`](../../backend/src/main/java/com/cinema/booking/pattern/composite/DashboardStatsComposite.java).

### Bước 3 — Từng `StatsComponent` leaf ghi số liệu vào map

**Lời thoại:** Mỗi leaf — ví dụ phim, người dùng, suất chiếu, F&B, vé, khuyến mãi, doanh thu — implement cùng method `collect` nhưng **nội dung khác nhau**: leaf đọc repository hoặc tổng hợp rồi `put` các key riêng vào map. Thứ tự gọi phụ thuộc thứ tự bean trong list inject; quan trọng là **hợp đồng thống nhất** và map đích chung.

**Ghi chú:** Hành động: quét các hộp `*StatsLeaf` trên UML và mũi tên tới `StatsComponent`. Nói ngoài: package [`pattern/composite`](../../backend/src/main/java/com/cinema/booking/pattern/composite/); có thể không đọc hết tên lớp nếu thiếu thời gian.

### Bước 4 — Controller trả HTTP 200 cùng map đã đầy

**Lời thoại:** Sau khi vòng `collect` kết thúc, map đã chứa đủ các chỉ số từng leaf ghi vào. Controller bọc map trong `ResponseEntity.ok` và trả về client — toàn bộ luồng chỉ có **một lời gọi** `collect` ở tầng điều phối.

**Ghi chú:** Hành động: chỉ lại `DashboardController` và đường ra (ý response). Nói ngoài: chi tiết `return ResponseEntity.ok(stats)` nằm trong source, không vẽ trên class diagram.

---

## Bước “âm thầm” lúc khởi động Spring (bổ sung luồng)

**Lời thoại:** Trước cả request đầu tiên, Spring đã dựng `DashboardStatsComposite` với constructor nhận `List<StatsComponent>`: mọi `@Component` leaf được gom vào list, composite lọc bản thân. **Không** có method `add()` trong composite — cấu trúc cây do **container** quyết định, đúng ghi chú đầu file UML.

**Ghi chú:** Hành động: có thể chỉ đoạn markdown phía trên Mermaid trong [05-composite.md](../../UML/pattern-only/05-composite.md). Nói ngoài: đây là bước wiring, không phải một mũi tên runtime trên hình.

---

## Kết (~20–30 giây)

**Lời thoại:** Luồng chức năng rất thẳng: **Controller → một lần composite → nhiều leaf → một map trả về**. Composite Pattern giúp thêm chỉ số mới bằng class leaf mới mà không phình một method điều phối khổng lồ.

**Ghi chú:** Nói ngoài — tóm tắt; có thể panorama sơ đồ.

---

## Thời lượng (tham khảo)

**Lời thoại:** _(Không đọc.)_

**Ghi chú:** Nói ngoài — ~3 phút nếu chỉ Bước 1–4; ~4 phút nếu thêm đoạn khởi động Spring.
