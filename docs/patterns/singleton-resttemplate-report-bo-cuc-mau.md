# Singleton (RestTemplate qua Spring IoC) — Trình bày theo bố cục mẫu

> **Luồng:** Khởi động ứng dụng Spring — tạo một bean `RestTemplate` (singleton scope mặc định); `MomoServiceImpl` dùng cùng instance khi `POST` tới cổng MoMo (trừ khi bật `momo.dev-skip-external`)  
> **Tài liệu kỹ thuật chi tiết:** [singleton-resttemplate-package-vi.md](singleton-resttemplate-package-vi.md) · [06-singleton.md](06-singleton.md)

---

## Các pattern áp dụng

Trong backend thanh toán MoMo, dự án áp dụng **Singleton thông qua Spring IoC**: class `RestTemplateConfig` khai báo `@Bean` method `restTemplate()` trả về **một** `RestTemplate` mà Spring container giữ trong **singleton scope** (mặc định). Service `MomoServiceImpl` nhận `RestTemplate` qua **constructor injection** — không gọi `new RestTemplate()` rải rác trong từng request. Đây là biến thể Singleton **do container quản lý**, khác Singleton cổ điển (`enum` / `getInstance` trong chính class).

---

## Lý do sử dụng

Gọi HTTP ra cổng đối tác (MoMo) cần client ổn định. Tạo `new RestTemplate()` mỗi lần gọi gây **overhead** (tạo object, pool kết nối), khó **cấu hình thống nhất** (timeout, TLS, interceptor) và dễ **không nhất quán** giữa các lớp.

**Spring Singleton bean** gom việc tạo instance **một lần** khi context khởi động; mọi service cần HTTP có thể inject **cùng** bean — phù hợp tài nguyên dùng chung và **một điểm** mở rộng cấu hình trong `RestTemplateConfig`.

---

## Ưu điểm

**Tiết kiệm tài nguyên và thống nhất cấu hình**: một instance dùng chung, có thể bổ sung timeout hoặc factory tại một chỗ (`@Bean` method) mà không sửa nhiều service.

**Dễ bảo trì và mở rộng**: service mới chỉ cần khai báo phụ thuộc `RestTemplate` trong constructor; tránh anti-pattern `new` trong method. Phù hợp vận hành lâu dài khi số tích hợp HTTP tăng.

Luồng nghiệp vụ vẫn **rõ ràng**: `MomoServiceImpl` tập trung logic ký HMAC và payload MoMo; phần vận chuyển HTTP dùng client đã inject. Có thể bật `momo.dev-skip-external` để dev không gọi ngoài mà vẫn giữ cùng wiring bean.
