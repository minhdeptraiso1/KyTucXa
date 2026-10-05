ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000);

COMMENT ON COLUMN rooms.image_url IS 'URL ảnh đại diện của phòng; null dùng ảnh mặc định ở frontend';
