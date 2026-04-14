# Dynamic Pricing Engine — Trình bày theo bố cục mẫu

> **Luồng:** `POST /api/booking/calculate` — tính giá trước khi thanh toán  
> **Tài liệu kỹ thuật chi tiết:** [08-dynamic-pricing-engine.md](08-dynamic-pricing-engine.md)

---

## Các pattern áp dụng

Trong hệ thống đặt vé, nhóm xử lý **tính giá động (dynamic pricing)** sử dụng **năm pattern bổ trợ cho nhau** trên cùng một đường đi production: **Chain of Responsibility** (chuỗi kiểm tra điều kiện trước khi tính), **Proxy** (bọc engine tính giá để thêm cache Redis), **Specification** (đóng gói điều kiện nghiệp vụ dạng predicate cho phụ phí thời gian), **Strategy** (tách ba công thức: tiền vé, F&B, phụ phí thời gian), và **Decorator** (chuỗi giảm giá có thể kết hợp theo runtime). Các pattern này không đứng riêng lẻ: CoR chạy trước để đảm bảo dữ liệu hợp lệ và tái dùng kết quả truy vấn; Proxy chặn mọi lời gọi tính giá để tránh tính lặp; bên trong engine, Strategy và Specification phối hợp (strategy gọi predicate khi cần); cuối cùng Decorator cộng dồn các khoản giảm sau khi đã có subtotal.

---

## Lý do sử dụng

Luồng tính giá trong ứng dụng rạp chiếu phim cần vừa **an toàn nghiệp vụ** (suất chiếu còn hợp lệ, ghế còn trống, mã khuyến mãi còn hạn và còn lượt), vừa **linh hoạt công thức** (vé + F&B + phụ phí cuối tuần/ngày lễ + giảm theo hạng thành viên + giảm theo promo), vừa **chịu tải khi người dùng xem giá nhiều lần** với cùng bộ tham số, trong khi phần còn lại của nghiệp vụ (một điểm API thống nhất, một DTO kết quả chi tiết) vẫn phải giữ nguyên hợp đồng với client.

**Chain of Responsibility** giải quyết việc không thể nhồi hàng chục điều kiện kiểm tra vào một method duy nhất: mỗi handler đảm nhiệm một quy tắc, nối thành chuỗi có thứ tự, fail sớm khi vi phạm, đồng thời ghi `showtime` và `promotion` vào context để tầng sau không phải truy vấn lại cơ sở dữ liệu.

**Proxy** giải quyết bài toán preview giá lặp lại: một lớp bọc cùng giao diện `IPricingEngine` kiểm tra cache theo khóa ghép từ suất chiếu, ghế, mã promo và khách; cache hit thì trả ngay, cache miss mới ủy quyền xuống engine thật — caller (`BookingServiceImpl`) không cần biết có cache hay không.

**Specification** tách các điều kiện “cuối tuần”, “ngày lễ”, “đặt sớm”, “lấp đầy cao” khỏi code if-else rải rác trong strategy: mỗi điều kiện là một predicate có thể kiểm thử độc lập và ghép nối, giúp tránh sót quy tắc (ví dụ ngày lễ) và tập trung danh sách ngày lễ một nơi.

**Strategy** cho phép ba cách tính tiền khác nhau (vé, F&B, phụ phí thời gian) cùng chung một hợp đồng `calculate(context)` mà không switch theo loại trong một method khổng lồ; đồng thời F&B được tính trên dữ liệu đã resolve sẵn từ service, tránh N+1 truy vấn trong engine.

**Decorator** giải quyết việc có nhiều loại giảm giá có thể đồng thời tồn tại (promo + hạng thành viên) với tập phụ thuộc runtime: thay vì bùng nổ lớp kế thừa kiểu “chỉ promo / chỉ member / cả hai”, chuỗi decorator được dựng động quanh `NoDiscount`, mỗi lớp chỉ cộng thêm một loại chiết khấu và ủy quyền phần còn lại cho lớp bọc bên trong; phần validate promo đã được tách sang CoR nên tầng giảm giá chỉ tính thuần, đúng phân tầng trách nhiệm.

---

## Ưu điểm

Áp năm pattern trên mang lại khả năng mở rộng theo hướng **đóng với thay đổi, mở với mở rộng**: khi bổ sung quy tắc validate mới có thể thêm một handler và nối vào cấu hình chuỗi; khi bổ sung cách tính giá mới có thể thêm một strategy; khi bổ sung loại giảm giá mới có thể thêm một decorator; khi bổ sung điều kiện kinh doanh mới có thể bổ sung predicate trong lớp điều kiện — **ít phải sửa** các lớp điều phối và engine đang ổn định.

Mã nguồn **dễ kiểm thử và bảo trì** hơn vì từng mắt xích có phạm vi hẹp: handler chỉ lo một rule, strategy chỉ lo một công thức, decorator chỉ lo một khoản chiết khấu, specification chỉ lo một điều kiện; proxy và engine thật có thể thay thế lẫn nhau qua interface. Luồng cache có thể bật hoặc chỉnh TTL mà không làm lẫn logic tính giá cốt lõi.

Proxy còn giúp **giảm tải** cho cơ sở dữ liệu và CPU khi người dùng lặp lại cùng một bản xem giá. CoR giúp **giảm truy vấn dư thừa** nhờ tái dùng thực thể đã load. Specification giúp **giảm rủi ro cấu hình logic nghiệp vụ** (thiếu ngày lễ, điều kiện trùng lặp) vì quy tắc được tập trung và kiểm thử tách biệt. Decorator giúp **tránh nhầm giữa validate và chiết khấu**, giảm bug “validate promo ở hai nơi” hay “quên giảm hạng thành viên” so với cách nhồi toàn bộ vào một method.

Tổng thể, điểm gọi API vẫn **thống nhất** (`calculatePrice`), phản hồi vẫn **đầy đủ breakdown** (vé, F&B, phụ phí thời gian, giảm thành viên, tổng giảm, nhãn chiến lược, tổng cuối), trong khi bên trong được chia thành các tầng pattern **rõ vai trò, ít phụ thuộc chéo**, phù hợp phát triển và vận hành lâu dài.
