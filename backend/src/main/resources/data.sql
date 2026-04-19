-- Seed/reset data only.
-- Schema normalization has been moved to normalized_db.sql.

DELETE FROM notifications;
DELETE FROM payments;
DELETE FROM fnb_lines;
DELETE FROM tickets;
DELETE FROM bookings;
DELETE FROM showtimes;
DELETE FROM movie_casts;
DELETE FROM movie_genres;
DELETE FROM cast_members;
DELETE FROM movies;
DELETE FROM genres;
DELETE FROM seats;
DELETE FROM seat_types;
DELETE FROM rooms;
DELETE FROM cinemas;
DELETE FROM locations;
DELETE FROM fnb_items;
DELETE FROM user_accounts;
DELETE FROM admins;
DELETE FROM staffs;
DELETE FROM customers;
DELETE FROM users;
DELETE FROM promotions;

-- Ensure deterministic IDs for tables inserted without explicit PK
ALTER TABLE seat_types AUTO_INCREMENT = 1;
ALTER TABLE fnb_items AUTO_INCREMENT = 1;
ALTER TABLE cast_members AUTO_INCREMENT = 1;

-- Users (base + inheritance tables)
INSERT INTO users (id, fullname, phone) VALUES
  (-1, 'Khách vãng lai', '0000000000'),
  (1, 'System Admin', '0900000001'),
  (2, 'Counter Staff', '0900000002'),
  (3, 'Nguyen Van A', '0900000003'),
  (4, 'Tran Thi B', '0900000004'),
  (5, 'Counter Staff 2', '0900000005');

INSERT INTO admins (user_id) VALUES (1);
INSERT INTO staffs (user_id) VALUES (2), (5);
INSERT INTO customers (user_id) VALUES (-1), (3), (4);

INSERT INTO user_accounts (id, user_id, email, password_hash) VALUES
  (-1, -1, 'guest@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (1, 1, 'admin@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (2, 2, 'staff@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (3, 3, 'user1@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (4, 4, 'user2@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (5, 5, 'staff2@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK');

-- Geography and cinemas
INSERT INTO locations (id, name) VALUES
  (1, 'TP Ho Chi Minh'),
  (2, 'Ha Noi');

INSERT INTO cinemas (id, location_id, name, address) VALUES
  (1, 1, 'StarCine Landmark', '208 Nguyen Huu Canh, Binh Thanh, HCM'),
  (2, 2, 'StarCine Tay Ho', '101 Xuan Dieu, Tay Ho, Ha Noi');

INSERT INTO rooms (id, cinema_id, name, screen_type) VALUES
  (1, 1, 'Room 1', '2D'),
  (2, 1, 'Room 2', 'IMAX'),
  (3, 2, 'Room 1', '3D');

INSERT INTO seat_types (name, price_surcharge) VALUES
  ('STANDARD', 0.00),
  ('VIP', 25000.00),
  ('COUPLE', 50000.00);

INSERT INTO seats (id, room_id, seat_code, seat_type_id, is_active) VALUES
-- Row A (Standard)
  (1, 1, 'A1', 1, true), (2, 1, 'A2', 1, true), (3, 1, 'A3', 1, true), (4, 1, 'A4', 1, true),
  (5, 1, 'A5', 1, true), (6, 1, 'A6', 1, true), (7, 1, 'A7', 1, true), (8, 1, 'A8', 1, true),
  -- Row B (Standard)
  (9, 1, 'B1', 1, true), (10, 1, 'B2', 1, true), (11, 1, 'B3', 1, true), (12, 1, 'B4', 1, true),
  (13, 1, 'B5', 1, true), (14, 1, 'B6', 1, true), (15, 1, 'B7', 1, true), (16, 1, 'B8', 1, true),
  -- Row C (VIP)
  (17, 1, 'C1', 2, true), (18, 1, 'C2', 2, true), (19, 1, 'C3', 2, true), (20, 1, 'C4', 2, true),
  (21, 1, 'C5', 2, true), (22, 1, 'C6', 2, true), (23, 1, 'C7', 2, true), (24, 1, 'C8', 2, true),
  -- Row D (VIP)
  (25, 1, 'D1', 2, true), (26, 1, 'D2', 2, true), (27, 1, 'D3', 2, true), (28, 1, 'D4', 2, true),
  (29, 1, 'D5', 2, true), (30, 1, 'D6', 2, true), (31, 1, 'D7', 2, true), (32, 1, 'D8', 2, true),
  -- Row E (Couple)
  (33, 1, 'E1', 3, true), (34, 1, 'E2', 3, true), (35, 1, 'E3', 3, true), (36, 1, 'E4', 3, true),
  -- Room 2
  (37, 2, 'A1', 1, true), (38, 2, 'A2', 1, true), (39, 2, 'C1', 3, true),
  -- Room 3
  (40, 3, 'A1', 1, true), (41, 3, 'A2', 1, true);

-- F&B catalog (exactly matches FnbItem entity fields)
INSERT INTO fnb_items (fnb_item_id, name, description, price, image_url, is_active) VALUES
  (1, 'Combo Bap Nuoc Nho', '1 bap ngo nho + 1 nuoc ngot 500ml', 69000.00, 'https://api-website.cinestar.com.vn/media/.thumbswysiwyg/pictures/PICCONNEW/CNS035_COMBO_GAU.png?rand=1723084117', true),
  (2, 'Combo Bap Nuoc Lon', '1 bap ngo lon + 2 nuoc ngot 500ml', 109000.00, 'https://down-vn.img.susercontent.com/file/5178202fa8a147917d01aedc379736d0', true),
  (3, 'Bap Caramel', 'Bap ngo caramel vi ngot', 45000.00, 'https://images.pexels.com/photos/30925516/pexels-photo-30925516.jpeg', true),
  (4, 'Coca Cola 500ml', 'Nuoc ngot Coca Cola chai 500ml', 25000.00, 'https://images.pexels.com/photos/14650670/pexels-photo-14650670.jpeg', true),
  (5, 'Tra Dao Chanh Sa', 'Tra dao lanh vi chanh sa', 35000.00, 'https://images.pexels.com/photos/33573171/pexels-photo-33573171.jpeg', true);

-- Catalog
INSERT INTO genres (id, name) VALUES
  (1, 'Action'),
  (2, 'Sci-Fi'),
  (3, 'Drama');

INSERT INTO movies (id, title, description, duration_minutes, release_date, language, poster_url, status) VALUES
  (1, 'Nebula Strike', 'A rescue mission across deep space.', 128, '2026-03-20', 'English', 'https://cdn2.fptshop.com.vn/unsafe/1920x0/filters:format(webp):quality(75)/2024_2_22_638442168156516339_poster-phim-hoat-hinh.jpg', 'NOW_SHOWING'),
  (2, 'Silent River', 'A family drama in a northern town.', 110, '2026-04-01', 'Vietnamese', 'https://ephoto360.com/uploads/worigin/2020/07/20/taoanhcheposterphimdinhmusuongonline5f156a56b32ca_da3b502225e6c1f091aa5fafb6be6ea4.jpg', 'COMING_SOON');

INSERT INTO movie_genres (movie_id, genre_id) VALUES
  (1, 1),
  (1, 2),
  (2, 3);

INSERT INTO cast_members (full_name, bio, birth_date, nationality, image_url) VALUES
  ('Liam Carter', 'Lead actor in multiple sci-fi titles.', '1988-09-12', 'US', 'https://example.com/cast/liam-carter.jpg'),
  ('Ngoc Anh', 'Vietnamese actress and producer.', '1992-05-30', 'VN', 'https://example.com/cast/ngoc-anh.jpg'),
  ('Harper Lee', 'Director known for atmospheric storytelling.', '1979-11-03', 'UK', 'https://example.com/cast/harper-lee.jpg');

INSERT INTO movie_casts (id, movie_id, cast_member_id, role_name, role_type) VALUES
  (1, 1, 1, 'Captain Jax', 'ACTOR'),
  (2, 1, 3, NULL, 'DIRECTOR'),
  (3, 2, 2, 'Mai', 'ACTOR');

-- Showtimes
INSERT INTO showtimes (id, movie_id, room_id, start_time, end_time, base_price) VALUES
  (1, 1, 1, '2026-04-18 09:00:00', '2026-04-18 11:08:00', 90000.00),
  (2, 1, 2, '2026-04-18 19:30:00', '2026-04-18 21:38:00', 140000.00),
  (3, 2, 3, '2026-04-19 14:00:00', '2026-04-19 15:50:00', 80000.00),
  (4, 1, 1, '2026-04-20 10:00:00', '2026-04-20 12:08:00', 95000.00),
  (5, 1, 2, '2026-04-20 20:00:00', '2026-04-20 22:08:00', 150000.00),
  (6, 2, 3, '2026-04-21 15:00:00', '2026-04-21 16:50:00', 85000.00);

-- Promotions, booking flow
INSERT INTO promotions (id, code, discount_type, discount_value, valid_to) VALUES
  (1, 'WELCOME10', 'PERCENT', 10.00, '2026-12-31 23:59:59'),
  (2, 'SAVE30K', 'FIXED', 30000.00, '2026-12-31 23:59:59');

-- =========================================================================
-- BOOKING & STATE PATTERN TEST DATA
-- =========================================================================

-- 1. PENDING (Chờ thanh toán) - User Nguyen Van A (ID 3)
INSERT INTO bookings (id, booking_code, user_id, status, created_at) VALUES 
  (1, 'BK-PENDING001', 3, 'PENDING', '2026-04-19 10:00:00');
INSERT INTO tickets (id, booking_id, movie_id, showtime_id, seat_id, unit_price) VALUES
  (1, 1, 1, 1, 1, 90000.00); -- Ghế A1, Room 1

-- 2. CONFIRMED (Đã thanh toán) - Khách vãng lai (ID -1)
INSERT INTO bookings (id, booking_code, user_id, status, created_at) VALUES 
  (2, 'BK-CONFIRM002', -1, 'CONFIRMED', '2026-04-19 11:30:00');
INSERT INTO tickets (id, booking_id, movie_id, showtime_id, seat_id, unit_price) VALUES
  (2, 2, 1, 1, 17, 115000.00), -- VIP C1
  (3, 2, 1, 1, 18, 115000.00); -- VIP C2
INSERT INTO fnb_lines (id, booking_id, fnb_item_id, quantity) VALUES
  (1, 2, 2, 1); -- Combo Bap Nuoc Lon
INSERT INTO payments (id, booking_id, amount, payment_method, status, paid_at) VALUES
  (1, 2, 339000.00, 'CASH', 'SUCCESS', '2026-04-19 11:31:00');

-- 3. CANCELLED (Đã hủy vì chưa thanh toán kịp) - Tran Thi B (ID 4)
INSERT INTO bookings (id, booking_code, user_id, status, created_at) VALUES 
  (3, 'BK-CANCEL003', 4, 'CANCELLED', '2026-04-18 15:00:00');
INSERT INTO tickets (id, booking_id, movie_id, showtime_id, seat_id, unit_price) VALUES
  (4, 3, 2, 3, 40, 80000.00); -- Ghế A1, Room 3

-- 4. REFUNDED (Đã hoàn tiền) - Nguyen Van A (ID 3)
INSERT INTO bookings (id, booking_code, user_id, status, created_at) VALUES 
  (4, 'BK-REFUND004', 3, 'REFUNDED', '2026-04-17 09:20:00');
INSERT INTO tickets (id, booking_id, movie_id, showtime_id, seat_id, unit_price) VALUES
  (5, 4, 1, 2, 39, 190000.00); -- COUPLE C1, Room 2
INSERT INTO payments (id, booking_id, amount, payment_method, status, paid_at) VALUES
  (2, 4, 190000.00, 'MOMO', 'SUCCESS', '2026-04-17 09:22:00');

-- 5. PRINTED (Đã in vé tại quầy) - Khách vãng lai (ID -1)
INSERT INTO bookings (id, booking_code, user_id, status, created_at) VALUES 
  (5, 'BK-PRINT005', -1, 'PRINTED', '2026-04-19 12:45:00');
INSERT INTO tickets (id, booking_id, movie_id, showtime_id, seat_id, unit_price) VALUES
  (6, 5, 2, 3, 41, 80000.00); -- Ghế A2, Room 3
INSERT INTO fnb_lines (id, booking_id, fnb_item_id, quantity) VALUES
  (2, 5, 4, 2); -- 2 Coca Cola 500ml
INSERT INTO payments (id, booking_id, amount, payment_method, status, paid_at) VALUES
  (3, 5, 130000.00, 'CASH', 'SUCCESS', '2026-04-19 12:46:00');
