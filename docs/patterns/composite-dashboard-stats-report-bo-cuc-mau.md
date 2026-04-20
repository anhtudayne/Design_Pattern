# Composite (Dashboard thống kê) — Trình bày theo bố cục mẫu

> **Luồng:** `GET /api/admin/dashboard/stats` — gom số liệu tổng quan cho trang quản trị  
> **Tài liệu kỹ thuật chi tiết:** [composite-dashboard-stats-package-vi.md](composite-dashboard-stats-package-vi.md) · [05-composite.md](05-composite.md)

---

## Các pattern áp dụng

Trong hệ thống đặt vé rạp chiếu phim, nhóm xử lý **thống kê dashboard admin** áp dụng **Composite Pattern**: một interface chung `StatsComponent` (vai trò **Component**) cho phép mọi nút — dù là **một chỉ số đơn** (**Leaf**) hay **bộ gom** (**Composite**) — đều thực hiện cùng một thao tác `collect(Map)` để ghi dữ liệu vào một map phản hồi. Các Leaf cụ thể (`MovieStatsLeaf`, `UserStatsLeaf`, `ShowtimeStatsLeaf`, `FnbStatsLeaf`, `TicketStatsLeaf`, `PromotionStatsLeaf`, `RevenueStatsLeaf`) mỗi class chỉ lo **một loại số liệu** (đếm repository hoặc cộng doanh thu). `DashboardStatsComposite` nhận toàn bộ bean `StatsComponent` do Spring inject, **lọc bỏ chính nó** để tránh đệ quy, rồi gọi lần lượt `collect` trên cùng map đích. Controller chỉ cần **một dòng** gọi composite.

---

## Lý do sử dụng

Trang admin cần đồng thời nhiều chỉ số (phim, user, suất chiếu, F&B, vé, khuyến mãi, doanh thu). Nếu viết tất cả lời gọi repository vào một controller hoặc một method dài, mã sẽ **khó đọc**, **khó test**, và mỗi lần thêm chỉ số mới phải **mở lại** lớp điều phối — trái với hướng mở rộng an toàn.

**Composite** giải quyết việc client (ở đây là `DashboardController`) chỉ muốn **một điểm gọi** nhưng bên trong có nhiều nguồn dữ liệu: controller tạo `HashMap` và gọi `dashboardStatsComposite.collect(stats)` một lần; bên trong, từng Leaf tự `put` key riêng (`totalMovies`, `totalTickets`, …) nên **không trùng trách nhiệm**.

**Spring** đóng vai trò dựng sẵn danh sách các `StatsComponent`: mỗi Leaf là `@Component`; composite nhận `List<StatsComponent>` ở constructor — thêm Leaf mới **không cần** sửa danh sách thủ công trong composite.

---

## Ưu điểm

Cấu trúc **đóng với thay đổi ở controller, mở với mở rộng chỉ số**: thêm một file Leaf mới implement `StatsComponent` là dashboard có thêm số liệu mà **không bắt buộc** chỉnh `DashboardStatsComposite` hay controller.

Mã **dễ kiểm thử** hơn: có thể test từng Leaf với repository giả lập; có thể test composite với danh sách `StatsComponent` giả. **Hợp đồng API** thống nhất — một JSON map — trong khi bên trong tách mắt xích **rõ vai trò**.

Composite còn giúp **tránh lỗi đệ quy** nhờ bước lọc `instanceof DashboardStatsComposite` khi Spring vô tình đưa cả bean composite vào list (vì nó cũng implement `StatsComponent`). Tổng thể phù hợp **bảo trì và mở rộng** dashboard lâu dài.
