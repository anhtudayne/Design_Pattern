-- Chạy một lần trên DB đã tồn tại (trước đó fnb_items chưa có cinema_id)
-- Sau đó restart backend.

ALTER TABLE fnb_items ADD COLUMN cinema_id INT NULL AFTER id;
UPDATE fnb_items SET cinema_id = 1 WHERE cinema_id IS NULL;
ALTER TABLE fnb_items MODIFY cinema_id INT NOT NULL;
ALTER TABLE fnb_items ADD CONSTRAINT fk_fnb_items_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas(id);
