# Hướng Dẫn Khởi Chạy Dự Án StarCine

**Thành viên thực hiện:**
- Võ Văn Tú - 23110359
- Vũ Anh Quốc - 23110296

Dự án StarCine bao gồm 2 phần chính: **Frontend** (ReactJS + Vite) và **Backend** (Spring Boot + Java). Dưới đây là các bước cơ bản để thiết lập và chạy dự án này trên môi trường phát triển cục bộ (Local).

## Yêu Cầu Cài Đặt (Prerequisites)
Để đảm bảo dự án chạy trơn tru, máy tính của bạn cần cài đặt sẵn một số công cụ sau:
1. **Node.js** (Khuyên dùng v18.x trở lên) - Dùng cho việc chạy Frontend.
2. **Java Development Kit (JDK)**: Phiên bản 17 (hoặc tương thích với dự án) - Dùng cho Backend.

---

## 1. Khởi Chạy Máy Chủ Nghiệp Vụ (Backend - Spring Boot)
Dự án được khởi tạo bằng Maven. Có nhiều cách để bạn chạy source Backend nằm trong thư mục `backend/`.

**Cách 1: Khởi chạy bằng Terminal (Khuyên dùng sử dụng Maven Wrapper sẵn có)**
- **Bước 1**: Mở thư mục backend ở trên Terminal:
  ```bash
  cd backend
  ```
- **Bước 2**: Thực thi quá trình cấu hình lệnh khởi chạy:
  - *Đối với hệ điều hành macOS / Linux:*
    ```bash
    ./mvnw spring-boot:run
    ```
  - *Đối với hệ điều hành Windows:*
    ```bash
    mvnw.cmd spring-boot:run
    ```

**Cách 2: Khởi chạy bằng IDE (IntelliJ IDEA, Eclipse)**
1. File -> Open (Hoặc Open Project) và chọn trực tiếp đến thư mục `backend`.
2. Đợi IDE thực thi việc đồng bộ, cài đặt tải xuống các file thư viện trong file `pom.xml`.
3. Khi tải xong, tìm đến class main của dự án (Thường là `...Application.java`) và nhấn nút biểu tượng `Run` (Mũi tên xanh) trên IDE.

Hệ thống Spring Boot thường mặc định sẽ lắng nghe ở port `localhost:8080`.

---

## 2. Khởi Chạy Giao Diện (Frontend - ReactJS)
Tất cả các mã nguồn giao diện nằm trong thư mục `frontend/`.

- **Bước 1**: Mở Terminal/Command Prompt và di chuyển vào thư mục Frontend:
  ```bash
  cd frontend
  ```

- **Bước 2**: Cài đặt các gói thư viện (Dependencies):
  ```bash
  npm install
  ```

- **Bước 3**: Chạy server ở môi trường phát triển (Development):
  ```bash
  npm run dev
  ```
  Sau khi chạy thành công, project frontend sẽ được biên dịch và thiết lập một máy chủ localhost (thường nằm ở `http://localhost:5173`). Bạn có thể click / truy cập link này trên trình duyệt web để tiến hành xem giao diện.

---

## 3. Quản Lý Cấu Hình Biến Môi Trường (`.env`)
Dự án hiện tại đang chứa 1 file `.env`: 
- `backend/.env`

**Đặc biệt lưu ý ở Backend (`backend/.env`):**
Cấu hình hiện tại đã tích hợp và trỏ sẵn kết nối đến các môi trường đám mây (Cloud) như:
- **MySQL Database**: `mysql-....aivencloud.com` (Đã có sẵn data, không cần cài MySQL trên máy tính cục bộ nếu chỉ test kết nối).
- **Redis Cache**: Kết nối qua Redis Lab.
- **Dịch vụ hình ảnh Cloudinary**, **SMTP Email (Gửi mail)** và **Cổng Thanh Toán Demo (MoMo)**.


## 4. Kiểm thử Cổng Thanh toán 
Vì Backend được tích hợp cổng thanh toán trực tuyến MoMo. Nếu bạn muốn kiểm tra hoàn thiện một luồng thanh toán:
1. Bạn sẽ cần cài đặt **Ngrok** để public dịch vụ Backend từ cổng 8080 thành một đường dẫn HTTPS công khai.
2. Lấy link public đó cấu hình ngược vào biến `NGROK_HOST` ở file `.env` của `backend` để cho MoMo có thể trả về IPN (Webhook báo kết quả giao dịch thành công).
