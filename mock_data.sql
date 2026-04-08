USE film_booking_web;

-- 1. Xóa dữ liệu cũ (Tùy chọn, cân nhắc kỹ trước khi tháo comment)
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE users;
-- TRUNCATE TABLE membership_tiers;
-- TRUNCATE TABLE movies;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 21. MEMBERSHIP_TIERS (Hạng Thành Viên) 
-- Cột theo Schema: name, min_spending, discount_percent
-- ==========================================
INSERT INTO membership_tiers (name, min_spending, discount_percent) VALUES 
('Thành viên Mới (Newbie)', 0.00, 0.00),
('Thành viên Bạc (Silver)', 1500000.00, 5.00),
('Thành viên Vàng (Gold)', 5000000.00, 10.00),
('Thành viên Kim Cương (Diamond)', 10000000.00, 15.00);

-- ==========================================
-- 1. USERS
-- Mật khẩu BCrypt: '$2a$10$y...' == '123456'
-- ==========================================
INSERT INTO users (tier_id, fullname, email, password_hash, phone, role, total_spending) VALUES 
(4, 'Quản Trị Hệ Thống', 'admin@galaxycinema.vn', '$2a$10$y6uOhZ.qI.R3rR8T2o1R6OOhsE2.uCrtQ00zV5h1uQ6Fj5U8M2mOO', '0987654321', 'ADMIN', 15000000.00),
(2, 'Người Dùng Mẫu 1', 'user1@gmail.com', '$2a$10$y6uOhZ.qI.R3rR8T2o1R6OOhsE2.uCrtQ00zV5h1uQ6Fj5U8M2mOO', '0912345678', 'USER', 1600000.00);

-- ==========================================
-- 14. LOCATIONS (Thành phố)
-- ==========================================
INSERT INTO locations (name) VALUES ('Hồ Chí Minh'), ('Hà Nội'), ('Đà Nẵng');

-- ==========================================
-- 4. CINEMAS (Cụm rạp)
-- ==========================================
INSERT INTO cinemas (location_id, name, address, hotline) VALUES 
(1, 'Galaxy Nguyễn Du', '116 Nguyễn Du, Quận 1, TP.HCM', '19002224'),
(2, 'Galaxy Tràng Thi', 'Mipec Tower, Tầng 5, Hà Nội', '19002225');

-- ==========================================
-- 5. ROOMS (Phòng chiếu)
-- ==========================================
INSERT INTO rooms (cinema_id, name, screen_type) VALUES 
(1, 'Phòng 1 Khổng Lồ', 'IMAX'),
(1, 'Phòng 2 Tiêu Chuẩn', '2D'),
(2, 'Phòng 1 Tràng Thi', '2D');

-- ==========================================
-- 6. SEATS (Sơ đồ ghế)
-- ==========================================
INSERT INTO seats (room_id, seat_row, seat_number, seat_type, price_surcharge, is_active) VALUES 
(1, 'A', 1, 'STANDARD', 0.00, TRUE), (1, 'A', 2, 'STANDARD', 0.00, TRUE), (1, 'A', 3, 'STANDARD', 0.00, TRUE),
(1, 'B', 1, 'VIP', 20000.00, TRUE), (1, 'B', 2, 'VIP', 20000.00, TRUE), (1, 'B', 3, 'VIP', 20000.00, TRUE),
(1, 'C', 1, 'COUPLE', 50000.00, TRUE);

-- ==========================================
-- 2. MOVIES (Phim)
-- ==========================================
INSERT INTO movies (title, description, duration_minutes, release_date, language, age_rating, poster_url, trailer_url, status) VALUES 
('Dune: Hành Tinh Cát 2', 'Hành trình trả thù vĩ đại.', 166, '2024-03-01', 'Tiếng Anh', 'C16', 'dune2.jpg', 'https://youtube.com/dune2', 'NOW_SHOWING'),
('Mai (Bản Đạo Diễn Cắt)', 'Phim Tâm lý sâu sắc nhất năm.', 131, '2024-02-10', 'Tiếng Việt', 'C18', 'mai.jpg', 'https://youtube.com/mai', 'NOW_SHOWING'),
('Avatar 3: Đứa Con Của Lửa', 'Siêu phim của James Cameron.', 190, '2025-12-19', 'Tiếng Anh', 'C13', 'avatar3.jpg', 'https://youtube.com/avatar3', 'COMING_SOON');

-- ==========================================
-- 3. GENRES (Thể loại)
-- ==========================================
INSERT INTO genres (name) VALUES ('Hành Động'), ('Khoa Học Viễn Tưởng'), ('Hoạt Hình'), ('Kinh Dị'), ('Tâm Lý'), ('Lãng Mạn');

-- MAPPING: MOVIE_GENRES
INSERT INTO movie_genres (movie_id, genre_id) VALUES (1, 2), (1, 1), (2, 5), (2, 6), (3, 2);

-- ==========================================
-- 15. DIRECTORS (Đạo diễn)
-- ==========================================
INSERT INTO directors (full_name, bio) VALUES 
('Trấn Thành', 'Đạo diễn Nghìn tỷ của điện ảnh Việt'),
('Denis Villeneuve', 'Đạo diễn thiên tài phi thường');

-- MAPPING: MOVIE_DIRECTORS 
INSERT INTO movie_directors (movie_id, director_id) VALUES (1, 2), (2, 1);

-- ==========================================
-- 16. ACTORS (Diễn viên)
-- ==========================================
INSERT INTO actors (full_name, bio) VALUES 
('Tuấn Trần', 'Nam chính điện ảnh Việt'),
('Phương Anh Đào', 'Nữ diễn viên thực lực'),
('Timothée Chalamet', 'Ngôi sao điện ảnh Hollywood');

-- MAPPING: MOVIE_ACTORS
INSERT INTO movie_actors (movie_id, actor_id, role_name) VALUES 
(1, 3, 'Paul Atreides'), (2, 1, 'Sâu'), (2, 2, 'Mai');

-- ==========================================
-- 7. SHOWTIMES (Suất chiếu)
-- ==========================================
INSERT INTO showtimes (movie_id, room_id, start_time, end_time, base_price) VALUES 
(1, 1, '2026-03-30 18:00:00', '2026-03-30 20:46:00', 150000.00),
(1, 2, '2026-03-30 21:00:00', '2026-03-30 23:46:00', 120000.00),
(2, 2, '2026-04-01 19:30:00', '2026-04-01 21:41:00', 95000.00);

-- ==========================================
-- 8. FNB_ITEMS (Đồ ăn nước uống)
-- ==========================================
INSERT INTO fnb_items (name, description, price, image_url, is_active) VALUES 
('Combo 1 Bắp 1 Nước', 'Phù hợp đi một mình', 85000.00, 'combo1.jpg', TRUE),
('Combo 2 Bắp 2 Nước', 'Cỡ khổng lồ cho 2 người', 135000.00, 'combo2.jpg', TRUE);

-- ==========================================
-- 9. PROMOTIONS (Mã Khuyến Mãi)
-- ==========================================
INSERT INTO promotions (code, discount_percentage, max_discount_amount, min_purchase_amount, valid_from, valid_to, quantity) VALUES 
('GALAXYSALE', 10.00, 50000.00, 100000.00, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1000);
