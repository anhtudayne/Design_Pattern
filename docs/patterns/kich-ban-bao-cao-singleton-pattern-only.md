# Kịch bản báo cáo — Singleton (UML pattern only)

> **Sơ đồ tham chiếu:** [UML/pattern-only/06-singleton.md](../../UML/pattern-only/06-singleton.md)  
> **Tài liệu chi tiết:** [06-singleton.md](06-singleton.md) · [singleton-resttemplate-package-vi.md](singleton-resttemplate-package-vi.md)

**Cách dùng:** Bài trình bày đi theo **thứ tự thời gian** — trước hết chuẩn bị bean khi hệ thống lên, sau đó service dùng bean khi có nghiệp vụ. Mỗi bước có **Lời thoại** (nói với hội đồng) và **Ghi chú** (chỉ slide / nói ngoài).

---

## Giới thiệu ngắn

**Lời thoại:** Phần Singleton trong dự án gắn với **một client HTTP dùng chung** cho tích hợp MoMo. Nhóm không dùng `getInstance()` trong class `RestTemplate`, mà để **Spring IoC** tạo đúng một bean `RestTemplate` và inject vào service. Sơ đồ pattern only tóm lược ba mắt xích: cấu hình tạo bean, container giữ singleton, service tiêu thụ qua constructor.

**Ghi chú:** Hành động: mở [UML/pattern-only/06-singleton.md](../../UML/pattern-only/06-singleton.md), nhìn tổng thể trước khi vào từng bước.

---

## Luồng 1 — Khi ứng dụng khởi động (cấu hình → bean → container)

### Bước 1.1 — `RestTemplateConfig` đăng ký bean

**Lời thoại:** Khi Spring Boot khởi động, lớp cấu hình `RestTemplateConfig` được quét. Method được đánh dấu `@Bean` trả về một đối tượng `RestTemplate`. Đây là điểm **duy nhất** mô tả “tạo client HTTP” cho toàn ứng dụng: timeout, converter hay interceptor sau này có thể gom tại đây.

**Ghi chú:** Hành động: trên UML, chỉ `RestTemplateConfig` và mũi tên `@Bean creates` tới `RestTemplate`. Nói ngoài: [`RestTemplateConfig.java`](../../backend/src/main/java/com/cinema/booking/config/RestTemplateConfig.java).

### Bước 1.2 — Container giữ một instance (singleton scope)

**Lời thoại:** Bean `RestTemplate` được đăng ký với **scope singleton mặc định**. Nghĩa là sau lần tạo đầu tiên, mọi chỗ yêu cầu cùng kiểu đó đều nhận **cùng một instance**. Trách nhiệm “chỉ có một” thuộc về **container**, không phải code static bên trong `RestTemplate`.

**Ghi chú:** Hành động: chỉ `SpringApplicationContext` và mũi tên “manages as singleton”. Nói ngoài: đây là khái niệm minh họa IoC trên sơ đồ.

---

## Luồng 2 — Khi có nghiệp vụ thanh toán MoMo (runtime)

### Bước 2.1 — `MomoServiceImpl` đã được inject sẵn `RestTemplate`

**Lời thoại:** Trước khi có request, Spring đã tạo `MomoServiceImpl` và truyền vào constructor tham số `RestTemplate` — chính bean singleton ở luồng 1. Service không gọi `new RestTemplate()` trong method; toàn bộ gọi HTTP ra cổng MoMo dùng field đã inject.

**Ghi chú:** Hành động: chỉ `MomoServiceImpl`, mũi tên constructor injection tới `RestTemplate`. Nói ngoài: [`MomoServiceImpl.java`](../../backend/src/main/java/com/cinema/booking/service/impl/MomoServiceImpl.java).

### Bước 2.2 — Thực hiện `createPayment` (hoặc luồng tương đương)

**Lời thoại:** Khi tầng controller hoặc service khác cần tạo giao dịch MoMo, `MomoServiceImpl` dựng payload, ký HMAC, rồi dùng `RestTemplate` để **POST** sang endpoint cấu hình. Nếu bật chế độ dev bỏ gọi ngoài, logic vẫn giữ nguyên wiring bean — chỉ nhánh nghiệp vụ không gọi HTTP thật.

**Ghi chú:** Hành động: có thể chỉ lại cặp Service–`RestTemplate` và note trên lớp `RestTemplate` (singleton scope). Nói ngoài: `momo.dev-skip-external` trong source; không có trên pattern-only.

---

## Điểm cần làm rõ với hội đồng (ranh giới sơ đồ)

**Lời thoại:** Tài liệu pattern only ghi thêm: `SecurityConfig` **không** dùng `RestTemplate` — cấu hình JWT và phân quyền là **luồng khác**, không nằm trong Singleton này. Note trên hình cũng nhắc một bean, nhiều client inject cùng instance.

**Ghi chú:** Hành động: chỉ note gắn `RestTemplate`; đọc thêm câu dưới diagram trong file UML về `SecurityConfig`. Nói ngoài: trả lời nếu hỏi nhầm lẫn với bảo mật.

---

## Kết (~20–30 giây)

**Lời thoại:** Tóm lại, luồng chức năng là: **khởi động** tạo một bean `RestTemplate` qua cấu hình và container; **runtime** service MoMo chỉ việc dùng bean đã inject. Mở rộng sau: thêm service HTTP khác bằng cách khai báo `RestTemplate` trong constructor, không nhân bản factory rải rác.

**Ghi chú:** Nói ngoài — có thể thu nhỏ slide, nhìn lại toàn sơ đồ theo hai luồng 1 và 2.

---

## Thời lượng (tham khảo)

**Lời thoại:** _(Không đọc.)_

**Ghi chú:** Nói ngoài — khoảng 3 phút nếu chỉ Luồng 1 + 2 gọn; 4 phút nếu giải thích thêm ranh giới `SecurityConfig`.
