-- Schema normalization script (idempotent)
-- Runs before data.sql to align legacy DB columns with current JPA entities.

-- cast_members.id -> cast_member_id
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'cast_members'
        AND COLUMN_NAME = 'id'
    ),
    'ALTER TABLE cast_members CHANGE COLUMN id cast_member_id INT NOT NULL AUTO_INCREMENT',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- seat_types.id -> seat_id
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'seat_types'
        AND COLUMN_NAME = 'id'
    ),
    'ALTER TABLE seat_types CHANGE COLUMN id seat_id INT NOT NULL AUTO_INCREMENT',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- fnb_items.id -> fnb_item_id
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'fnb_items'
        AND COLUMN_NAME = 'id'
    ),
    'ALTER TABLE fnb_items CHANGE COLUMN id fnb_item_id INT NOT NULL AUTO_INCREMENT',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- bookings: keep user_id only, remove legacy customer_id
SET @has_customer_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookings' AND COLUMN_NAME = 'customer_id'
);
SET @has_user_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookings' AND COLUMN_NAME = 'user_id'
);
SET @sql := IF(
  @has_customer_id = 1 AND @has_user_id = 0,
  'ALTER TABLE bookings CHANGE COLUMN customer_id user_id INT NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (
  SELECT MAX(kcu.CONSTRAINT_NAME)
  FROM information_schema.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = DATABASE()
    AND kcu.TABLE_NAME = 'bookings'
    AND kcu.COLUMN_NAME = 'customer_id'
    AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
);
SET @sql := IF(
  @has_customer_id = 1 AND @has_user_id = 1 AND @fk IS NOT NULL,
  CONCAT('ALTER TABLE bookings DROP FOREIGN KEY `', @fk, '`'),
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF(
  @has_customer_id = 1 AND @has_user_id = 1,
  'ALTER TABLE bookings DROP COLUMN customer_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- payments: keep payment_method only, remove legacy method
SET @has_method := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'method'
);
SET @has_payment_method := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'payment_method'
);
SET @sql := IF(
  @has_method = 1 AND @has_payment_method = 0,
  'ALTER TABLE payments CHANGE COLUMN method payment_method VARCHAR(20) NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF(
  @has_method = 1 AND @has_payment_method = 1,
  'ALTER TABLE payments DROP COLUMN method',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- tickets: keep unit_price only, remove legacy price
SET @has_price := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tickets' AND COLUMN_NAME = 'price'
);
SET @has_unit_price := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tickets' AND COLUMN_NAME = 'unit_price'
);
SET @sql := IF(
  @has_price = 1 AND @has_unit_price = 0,
  'ALTER TABLE tickets CHANGE COLUMN price unit_price DECIMAL(10,2) NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF(
  @has_price = 1 AND @has_unit_price = 1,
  'ALTER TABLE tickets DROP COLUMN price',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- fnb_items remove legacy FKs/columns, ensure is_active exists
SET @fk := (
  SELECT MAX(kcu.CONSTRAINT_NAME)
  FROM information_schema.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = DATABASE()
    AND kcu.TABLE_NAME = 'fnb_items'
    AND kcu.COLUMN_NAME = 'cinema_id'
    AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE fnb_items DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (
  SELECT MAX(kcu.CONSTRAINT_NAME)
  FROM information_schema.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = DATABASE()
    AND kcu.TABLE_NAME = 'fnb_items'
    AND kcu.COLUMN_NAME = 'category_id'
    AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE fnb_items DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fnb_items' AND COLUMN_NAME = 'is_active'
    ),
    'SELECT 1',
    'ALTER TABLE fnb_items ADD COLUMN is_active BIT(1) NOT NULL DEFAULT b''1'''
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fnb_items' AND COLUMN_NAME = 'category_id'
    ),
    'ALTER TABLE fnb_items DROP COLUMN category_id',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fnb_items' AND COLUMN_NAME = 'cinema_id'
    ),
    'ALTER TABLE fnb_items DROP COLUMN cinema_id',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
UPDATE fnb_items SET is_active = b'1' WHERE is_active IS NULL;

-- Additional legacy columns user requested to remove
SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cinemas' AND COLUMN_NAME = 'hotline'
  ), 'ALTER TABLE cinemas DROP COLUMN hotline', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (
  SELECT MAX(kcu.CONSTRAINT_NAME)
  FROM information_schema.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = DATABASE()
    AND kcu.TABLE_NAME = 'customers'
    AND kcu.COLUMN_NAME = 'tier_id'
    AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE customers DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'loyalty_points'
  ), 'ALTER TABLE customers DROP COLUMN loyalty_points', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'total_spending'
  ), 'ALTER TABLE customers DROP COLUMN total_spending', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND COLUMN_NAME = 'tier_id'
  ), 'ALTER TABLE customers DROP COLUMN tier_id', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fnb_lines' AND COLUMN_NAME = 'unit_price'
  ), 'ALTER TABLE fnb_lines DROP COLUMN unit_price', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (
  SELECT MAX(kcu.CONSTRAINT_NAME)
  FROM information_schema.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = DATABASE()
    AND kcu.TABLE_NAME = 'fnb_lines'
    AND kcu.COLUMN_NAME = 'item_id'
    AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE fnb_lines DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fnb_lines' AND COLUMN_NAME = 'item_id'
  ), 'ALTER TABLE fnb_lines DROP COLUMN item_id', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'movies' AND COLUMN_NAME = 'age_rating'
  ), 'ALTER TABLE movies DROP COLUMN age_rating', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'movies' AND COLUMN_NAME = 'trailer_url'
  ), 'ALTER TABLE movies DROP COLUMN trailer_url', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND COLUMN_NAME = 'created_at'
  ), 'ALTER TABLE notifications DROP COLUMN created_at', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(EXISTS(
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'created_at'
  ), 'ALTER TABLE users DROP COLUMN created_at', 'SELECT 1')
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Drop legacy tables not mapped by current entities.
-- Keep entity tables and explicit many-to-many bridge table: movie_genres.
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS artists;
DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS membership_tiers;
DROP TABLE IF EXISTS fnb_categories;
DROP TABLE IF EXISTS fnb_item_inventory;
DROP TABLE IF EXISTS promotion_inventory;
DROP TABLE IF EXISTS fnb_items_backup_20260417;
SET FOREIGN_KEY_CHECKS = 1;
