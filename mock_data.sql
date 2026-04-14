USE film_booking_web;

-- 1. Chèn dữ liệu vào bảng gốc users
INSERT INTO users (id, fullname, phone) VALUES
(1, 'Nguyen Nhat Huy', '0901234567'),
(2, 'Vo Van Tu', '0907654321'),
(3, 'Vu Anh Quoc', '0911223344'),
(4, 'Staff Member A', '0988776655');

-- 2. Chèn dữ liệu tài khoản vào user_accounts (Liên kết 1-1 với users)
-- Mật khẩu: 'password123' (BCrypt $2a$10, verified)
INSERT INTO user_accounts (user_id, email, password_hash) VALUES
(1, 'huy.it@example.com', '$2a$10$CFf0QcO8MiBVVkd3Gt427u2dcD88U7nPa5YcRcWF.Rk0SD5QetiPW'),
(2, 'tu.vo@example.com', '$2a$10$CFf0QcO8MiBVVkd3Gt427u2dcD88U7nPa5YcRcWF.Rk0SD5QetiPW'),
(3, 'quoc.vu@example.com', '$2a$10$CFf0QcO8MiBVVkd3Gt427u2dcD88U7nPa5YcRcWF.Rk0SD5QetiPW'),
(4, 'staff@f3.vn', '$2a$10$CFf0QcO8MiBVVkd3Gt427u2dcD88U7nPa5YcRcWF.Rk0SD5QetiPW');

-- 3. Phân vai trò vào các bảng con (Joined Inheritance)
-- Nguyen Nhat Huy là Customer
INSERT INTO customers (user_id, tier_id, total_spending, loyalty_points) 
VALUES (1, 1, 500000.00, 100);

-- Vo Van Tu và Vu Anh Quoc là Admin
INSERT INTO admins (user_id) VALUES (2), (3);

-- Staff Member A là Staff
INSERT INTO staffs (user_id) VALUES (4);

-- 4. Dữ liệu loại ghế (Cần thiết cho Cinema Aggregate)
INSERT INTO seat_types (id, name, price_surcharge) VALUES
(1, 'STANDARD', 0.00),
(2, 'VIP', 20000.00),
(3, 'COUPLE', 50000.00);

-- 5. Dữ liệu Phim và Thể loại
INSERT INTO genres (id, name) VALUES (1, 'Hành động'), (2, 'Kinh dị'), (3, 'Hoạt hình');

INSERT INTO movies (id, title, duration_minutes, status, poster_url, trailer_url) VALUES
(1, 'Kung Fu Panda 4', 94, 'NOW_SHOWING',
 'https://picsum.photos/seed/starcine-movie-1/480/720', NULL),
(2, 'Dune: Part Two', 166, 'NOW_SHOWING',
 'https://picsum.photos/seed/starcine-movie-2/480/720', NULL);

-- 6. Dữ liệu CastMember (Hệ thống mới)
INSERT INTO cast_members (id, full_name, bio, image_url) VALUES
(1, 'Denis Villeneuve', 'Đạo diễn tài năng người Canada', 'https://picsum.photos/seed/starcine-cast-1/400/400'),
(2, 'Timothée Chalamet', 'Nam diễn viên thủ vai Paul Atreides', 'https://picsum.photos/seed/starcine-cast-2/400/400');

-- 7. Quản lý vai trò trong phim
INSERT INTO movie_casts (movie_id, cast_member_id, role_name, role_type) VALUES
(2, 1, NULL, 'DIRECTOR'),
(2, 2, 'Paul Atreides', 'ACTOR');

-- 8. Gắn thể loại cho phim
INSERT INTO movie_genres (movie_id, genre_id) VALUES
(1, 3),
(2, 1),
(2, 2);

-- 9. Dữ liệu Location / Cinema / Room
INSERT INTO locations (id, name) VALUES
(1, 'TP. Ho Chi Minh'),
(2, 'Ha Noi'),
(3, 'Da Nang');

INSERT INTO cinemas (id, location_id, name, address) VALUES
(1, 1, 'Galaxy Nguyen Du', '116 Nguyen Du, Quan 1, TP.HCM'),
(2, 1, 'CGV Vincom Thu Duc', '216 Vo Van Ngan, Thu Duc, TP.HCM'),
(3, 2, 'Lotte Cinema West Lake', '272 Vo Chi Cong, Tay Ho, Ha Noi');

INSERT INTO rooms (id, cinema_id, name, screen_type) VALUES
(1, 1, 'Room 1', '2D'),
(2, 1, 'Room 2', 'IMAX'),
(3, 2, 'Room A', '3D'),
(4, 3, 'Room B', '2D');

-- 10. Dữ liệu ghế (mỗi phòng 12 ghế: A1-A6, B1-B6)
INSERT INTO seats (id, room_id, seat_type_id, seat_code) VALUES
(1, 1, 1, 'A1'), (2, 1, 1, 'A2'), (3, 1, 1, 'A3'), (4, 1, 2, 'A4'), (5, 1, 2, 'A5'), (6, 1, 3, 'A6'),
(7, 1, 1, 'B1'), (8, 1, 1, 'B2'), (9, 1, 1, 'B3'), (10, 1, 2, 'B4'), (11, 1, 2, 'B5'), (12, 1, 3, 'B6'),

(13, 2, 1, 'A1'), (14, 2, 1, 'A2'), (15, 2, 1, 'A3'), (16, 2, 2, 'A4'), (17, 2, 2, 'A5'), (18, 2, 3, 'A6'),
(19, 2, 1, 'B1'), (20, 2, 1, 'B2'), (21, 2, 1, 'B3'), (22, 2, 2, 'B4'), (23, 2, 2, 'B5'), (24, 2, 3, 'B6'),

(25, 3, 1, 'A1'), (26, 3, 1, 'A2'), (27, 3, 1, 'A3'), (28, 3, 2, 'A4'), (29, 3, 2, 'A5'), (30, 3, 3, 'A6'),
(31, 3, 1, 'B1'), (32, 3, 1, 'B2'), (33, 3, 1, 'B3'), (34, 3, 2, 'B4'), (35, 3, 2, 'B5'), (36, 3, 3, 'B6'),

(37, 4, 1, 'A1'), (38, 4, 1, 'A2'), (39, 4, 1, 'A3'), (40, 4, 2, 'A4'), (41, 4, 2, 'A5'), (42, 4, 3, 'A6'),
(43, 4, 1, 'B1'), (44, 4, 1, 'B2'), (45, 4, 1, 'B3'), (46, 4, 2, 'B4'), (47, 4, 2, 'B5'), (48, 4, 3, 'B6');

-- 11. Dữ liệu showtime
INSERT INTO showtimes (id, movie_id, room_id, start_time, end_time, base_price) VALUES
(1, 1, 1, '2026-04-11 09:00:00', '2026-04-11 10:34:00', 75000.00),
(2, 1, 1, '2026-04-11 14:00:00', '2026-04-11 15:34:00', 85000.00),
(3, 2, 2, '2026-04-11 19:00:00', '2026-04-11 21:46:00', 120000.00),
(4, 2, 3, '2026-04-12 20:30:00', '2026-04-12 23:16:00', 110000.00),
(5, 1, 4, '2026-04-12 10:30:00', '2026-04-12 12:04:00', 70000.00);

-- 12. Dữ liệu khuyến mãi
INSERT INTO promotions (id, code, discount_type, discount_value, valid_to) VALUES
(1, 'WELCOME10', 'PERCENT', 10.00, '2026-12-31 23:59:59'),
(2, 'GIAM30K', 'FIXED', 30000.00, '2026-12-31 23:59:59'),
(3, 'HET_HAN', 'PERCENT', 20.00, '2025-01-01 00:00:00');

INSERT INTO promotion_inventory (promotion_id, quantity) VALUES
(1, 100),
(2, 50),
(3, 0);

-- 13. Dữ liệu F&B
INSERT INTO fnb_items (id, name, description, price, is_active, image_url) VALUES
(1, 'Combo 1', '1 bap ngot + 1 pepsi lon', 89000.00, TRUE, 'https://picsum.photos/seed/starcine-fnb-combo/480/480'),
(2, 'Bap caramel', 'Bap vi caramel size vua', 55000.00, TRUE, 'https://picsum.photos/seed/starcine-fnb-popcorn/480/480'),
(3, 'Pepsi lon', 'Nuoc ngot pepsi 330ml', 25000.00, TRUE, 'https://picsum.photos/seed/starcine-fnb-soda/480/480'),
(4, 'Nachos phomai', 'Banh nachos kem sot phomai', 65000.00, TRUE, 'https://picsum.photos/seed/starcine-fnb-nachos/480/480'),
(5, 'Tra dao', 'Tra dao mat lanh', 30000.00, FALSE, 'https://picsum.photos/seed/starcine-fnb-tea/480/480');

INSERT INTO fnb_item_inventory (item_id, quantity) VALUES
(1, 100),
(2, 80),
(3, 200),
(4, 40),
(5, 0);

-- 14. Dữ liệu booking / ticket / fnb_lines / payment
INSERT INTO bookings (id, booking_code, customer_id, promotion_id, status, created_at) VALUES
(1, 'BK-20260410-0001', 1, 1, 'CONFIRMED', '2026-04-10 08:15:00'),
(2, 'BK-20260410-0002', 1, NULL, 'PENDING', '2026-04-10 09:20:00'),
(3, 'BK-20260410-0003', 1, 2, 'CANCELLED', '2026-04-10 11:05:00');

INSERT INTO tickets (id, booking_id, seat_id, showtime_id, price) VALUES
(1, 1, 4, 1, 95000.00),
(2, 1, 5, 1, 95000.00),
(3, 2, 10, 2, 105000.00),
(4, 3, 16, 3, 140000.00);

INSERT INTO fnb_lines (id, booking_id, item_id, quantity, unit_price) VALUES
(1, 1, 1, 1, 89000.00),
(2, 1, 3, 2, 25000.00),
(3, 2, 2, 1, 55000.00);

INSERT INTO payments (id, booking_id, amount, status, method, paid_at) VALUES
(1, 1, 324000.00, 'SUCCESS', 'MOMO', '2026-04-10 08:17:00'),
(2, 2, 160000.00, 'PENDING', 'VNPAY', NULL),
(3, 3, 110000.00, 'FAILED', 'CASH', NULL);

-- 15. Dữ liệu review
INSERT INTO reviews (id, movie_id, customer_id, rating, comment) VALUES
(1, 1, 1, 5, 'Phim vui, phu hop gia dinh, long tieng hay'),
(2, 2, 1, 4, 'Hinh anh dep, nhac phim tot, mach phim hoi cham');

-- 16. Dữ liệu notification
INSERT INTO notifications (id, user_id, title, message, is_read) VALUES
(1, 1, 'Dat ve thanh cong', 'Booking BK-20260410-0001 da thanh toan thanh cong.', TRUE),
(2, 1, 'Nhac lich chieu', 'Ban co suat chieu luc 14:00 ngay 11/04/2026.', FALSE),
(3, 2, 'Thong bao he thong', 'Dashboard doanh thu da cap nhat du lieu moi.', FALSE),
(4, 4, 'Ca truc moi', 'Ban duoc phan cong ca truc toi thu Bay.', FALSE);

-- 17. Mở rộng dữ liệu test đặt vé: đủ 5 phim + lịch chiếu trải 10 ngày
INSERT INTO movies (id, title, description, duration_minutes, release_date, language, status, poster_url, trailer_url) VALUES
(3, 'Avengers: Secret Wars', 'Sieu anh hung tap hop cho tran chien da vu tru.', 145, '2026-03-28', 'English', 'NOW_SHOWING',
 'https://picsum.photos/seed/starcine-movie-3/480/720', NULL),
(4, 'Your Name', 'Cau chuyen tinh cam ky ao xuyen thoi gian.', 106, '2026-02-14', 'Japanese', 'NOW_SHOWING',
 'https://picsum.photos/seed/starcine-movie-4/480/720', NULL),
(5, 'The Conjuring 4', 'Phan moi cua vu tru kinh di nha Warren.', 118, '2026-04-01', 'English', 'NOW_SHOWING',
 'https://picsum.photos/seed/starcine-movie-5/480/720', NULL);

INSERT INTO genres (id, name) VALUES
(4, 'Tinh cam'),
(5, 'Vien tuong');

INSERT INTO movie_genres (movie_id, genre_id) VALUES
(3, 1), (3, 5),
(4, 3), (4, 4),
(5, 2);

INSERT INTO cast_members (id, full_name, bio, image_url) VALUES
(3, 'Russo Brothers', 'Dao dien noi tieng voi loat phim Marvel', 'https://picsum.photos/seed/starcine-cast-3/400/400'),
(4, 'Robert Downey Jr.', 'Dien vien dong Iron Man', 'https://picsum.photos/seed/starcine-cast-4/400/400'),
(5, 'Makoto Shinkai', 'Dao dien anime Nhat Ban', 'https://picsum.photos/seed/starcine-cast-5/400/400'),
(6, 'Mitsuha Voice Actress', 'Long tieng nhan vat Mitsuha', 'https://picsum.photos/seed/starcine-cast-6/400/400'),
(7, 'James Wan', 'Dao dien kinh di noi tieng', 'https://picsum.photos/seed/starcine-cast-7/400/400'),
(8, 'Patrick Wilson', 'Dien vien dong Ed Warren', 'https://picsum.photos/seed/starcine-cast-8/400/400');

INSERT INTO movie_casts (movie_id, cast_member_id, role_name, role_type) VALUES
(3, 3, NULL, 'DIRECTOR'),
(3, 4, 'Tony Stark Variant', 'ACTOR'),
(4, 5, NULL, 'DIRECTOR'),
(4, 6, 'Mitsuha', 'ACTOR'),
(5, 7, NULL, 'DIRECTOR'),
(5, 8, 'Ed Warren', 'ACTOR');

-- Thêm lịch chiếu từ 2026-04-11 đến 2026-04-20 (10 ngày liên tục)
INSERT INTO showtimes (id, movie_id, room_id, start_time, end_time, base_price) VALUES
-- 2026-04-11
(6, 3, 2, '2026-04-11 10:00:00', '2026-04-11 12:25:00', 125000.00),
(7, 4, 3, '2026-04-11 16:00:00', '2026-04-11 17:46:00', 90000.00),
(8, 5, 4, '2026-04-11 21:00:00', '2026-04-11 22:58:00', 115000.00),

-- 2026-04-12
(9, 3, 1, '2026-04-12 09:30:00', '2026-04-12 11:55:00', 120000.00),
(10, 1, 2, '2026-04-12 13:30:00', '2026-04-12 15:04:00', 85000.00),
(11, 2, 3, '2026-04-12 19:30:00', '2026-04-12 22:16:00', 115000.00),

-- 2026-04-13
(12, 4, 1, '2026-04-13 10:30:00', '2026-04-13 12:16:00', 85000.00),
(13, 5, 2, '2026-04-13 18:00:00', '2026-04-13 19:58:00', 120000.00),

-- 2026-04-14
(14, 2, 4, '2026-04-14 14:00:00', '2026-04-14 16:46:00', 108000.00),
(15, 3, 3, '2026-04-14 20:15:00', '2026-04-14 22:40:00', 118000.00),

-- 2026-04-15
(16, 1, 1, '2026-04-15 09:00:00', '2026-04-15 10:34:00', 78000.00),
(17, 4, 4, '2026-04-15 15:30:00', '2026-04-15 17:16:00', 88000.00),
(18, 5, 2, '2026-04-15 21:15:00', '2026-04-15 23:13:00', 122000.00),

-- 2026-04-16
(19, 2, 1, '2026-04-16 11:00:00', '2026-04-16 13:46:00', 110000.00),
(20, 3, 4, '2026-04-16 19:00:00', '2026-04-16 21:25:00', 119000.00),

-- 2026-04-17
(21, 1, 3, '2026-04-17 10:00:00', '2026-04-17 11:34:00', 80000.00),
(22, 4, 2, '2026-04-17 14:30:00', '2026-04-17 16:16:00', 92000.00),
(23, 5, 1, '2026-04-17 22:00:00', '2026-04-17 23:58:00', 118000.00),

-- 2026-04-18
(24, 3, 2, '2026-04-18 09:45:00', '2026-04-18 12:10:00', 128000.00),
(25, 2, 4, '2026-04-18 17:00:00', '2026-04-18 19:46:00', 112000.00),

-- 2026-04-19
(26, 4, 1, '2026-04-19 08:30:00', '2026-04-19 10:16:00', 83000.00),
(27, 1, 3, '2026-04-19 13:00:00', '2026-04-19 14:34:00', 82000.00),
(28, 5, 2, '2026-04-19 20:30:00', '2026-04-19 22:28:00', 121000.00),

-- 2026-04-20
(29, 2, 1, '2026-04-20 10:15:00', '2026-04-20 13:01:00', 109000.00),
(30, 3, 4, '2026-04-20 16:45:00', '2026-04-20 19:10:00', 117000.00),
(31, 4, 3, '2026-04-20 21:00:00', '2026-04-20 22:46:00', 90000.00);

-- 18. Seed riêng cho trang Profile > Transactions (customer_id = 1)
-- Dùng id cao để không đụng dữ liệu cũ và hỗ trợ re-run script.
INSERT INTO bookings (id, booking_code, customer_id, promotion_id, status, created_at) VALUES
(101, 'BK-DEMO-TX-0101', 1, 1, 'CONFIRMED', '2026-04-12 09:15:00'),
(102, 'BK-DEMO-TX-0102', 1, NULL, 'CONFIRMED', '2026-04-13 18:20:00'),
(103, 'BK-DEMO-TX-0103', 1, 2, 'CANCELLED', '2026-04-14 20:05:00'),
(104, 'BK-DEMO-TX-0104', 1, NULL, 'PENDING', '2026-04-15 14:00:00')
ON DUPLICATE KEY UPDATE
customer_id = VALUES(customer_id),
promotion_id = VALUES(promotion_id),
status = VALUES(status),
created_at = VALUES(created_at);

INSERT INTO tickets (id, booking_id, seat_id, showtime_id, price) VALUES
(101, 101, 8, 2, 85000.00),
(102, 101, 9, 2, 85000.00),
(103, 102, 22, 3, 140000.00),
(104, 104, 31, 21, 80000.00)
ON DUPLICATE KEY UPDATE
booking_id = VALUES(booking_id),
seat_id = VALUES(seat_id),
showtime_id = VALUES(showtime_id),
price = VALUES(price);

INSERT INTO fnb_lines (id, booking_id, item_id, quantity, unit_price) VALUES
(101, 101, 1, 1, 89000.00),
(102, 102, 3, 2, 25000.00),
(103, 104, 2, 1, 55000.00)
ON DUPLICATE KEY UPDATE
booking_id = VALUES(booking_id),
item_id = VALUES(item_id),
quantity = VALUES(quantity),
unit_price = VALUES(unit_price);

INSERT INTO payments (id, booking_id, amount, status, method, paid_at) VALUES
(101, 101, 259000.00, 'SUCCESS', 'MOMO', '2026-04-12 09:17:00'),
(102, 102, 190000.00, 'SUCCESS', 'VNPAY', '2026-04-13 18:24:00'),
(103, 103, 110000.00, 'FAILED', 'MOMO', NULL),
(104, 104, 135000.00, 'PENDING', 'MOMO', NULL)
ON DUPLICATE KEY UPDATE
amount = VALUES(amount),
status = VALUES(status),
method = VALUES(method),
paid_at = VALUES(paid_at);