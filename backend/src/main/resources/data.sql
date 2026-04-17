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
  (1, 'System Admin', '0900000001'),
  (2, 'Counter Staff', '0900000002'),
  (3, 'Nguyen Van A', '0900000003'),
  (4, 'Tran Thi B', '0900000004');

INSERT INTO admins (user_id) VALUES (1);
INSERT INTO staffs (user_id) VALUES (2);
INSERT INTO customers (user_id) VALUES (3), (4);

-- bcrypt for password: 123456
INSERT INTO user_accounts (id, user_id, email, password_hash) VALUES
  (1, 1, 'admin@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (2, 2, 'staff@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (3, 3, 'user1@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK'),
  (4, 4, 'user2@starcine.local', '$2b$10$HQeilHe0tQIj3qb3pw6GFuj.zu8LY/YPrqeLJTh0hiI21FyoFzsGK');

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
  (1, 1, 'A1', 1, true),
  (2, 1, 'A2', 1, true),
  (3, 1, 'B1', 2, true),
  (4, 1, 'B2', 2, true),
  (5, 2, 'A1', 1, true),
  (6, 2, 'A2', 1, true),
  (7, 2, 'C1', 3, true),
  (8, 3, 'A1', 1, true),
  (9, 3, 'A2', 1, true);

-- F&B catalog (exactly matches FnbItem entity fields)
INSERT INTO fnb_items (fnb_item_id, name, description, price, image_url, is_active) VALUES
  (1, 'Combo Bap Nuoc Nho', '1 bap ngo nho + 1 nuoc ngot 500ml', 69000.00, 'https://example.com/fnb/combo-small.jpg', true),
  (2, 'Combo Bap Nuoc Lon', '1 bap ngo lon + 2 nuoc ngot 500ml', 109000.00, 'https://example.com/fnb/combo-large.jpg', true),
  (3, 'Bap Caramel', 'Bap ngo caramel vi ngot', 45000.00, 'https://example.com/fnb/popcorn-caramel.jpg', true),
  (4, 'Coca Cola 500ml', 'Nuoc ngot Coca Cola chai 500ml', 25000.00, 'https://example.com/fnb/coca-500ml.jpg', true),
  (5, 'Tra Dao Chanh Sa', 'Tra dao lanh vi chanh sa', 35000.00, 'https://example.com/fnb/peach-tea.jpg', true);

-- Catalog
INSERT INTO genres (id, name) VALUES
  (1, 'Action'),
  (2, 'Sci-Fi'),
  (3, 'Drama');

INSERT INTO movies (id, title, description, duration_minutes, release_date, language, poster_url, status) VALUES
  (1, 'Nebula Strike', 'A rescue mission across deep space.', 128, '2026-03-20', 'English', 'https://example.com/posters/nebula-strike.jpg', 'NOW_SHOWING'),
  (2, 'Silent River', 'A family drama in a northern town.', 110, '2026-04-01', 'Vietnamese', 'https://example.com/posters/silent-river.jpg', 'COMING_SOON');

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
  (3, 2, 3, '2026-04-19 14:00:00', '2026-04-19 15:50:00', 80000.00);

-- Promotions, booking flow
INSERT INTO promotions (id, code, discount_type, discount_value, valid_to) VALUES
  (1, 'WELCOME10', 'PERCENT', 10.00, '2026-12-31 23:59:59'),
  (2, 'SAVE30K', 'FIXED', 30000.00, '2026-12-31 23:59:59');
