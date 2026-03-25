ALTER TABLE online_course
    MODIFY COLUMN rating DOUBLE NULL DEFAULT NULL COMMENT 'So sao trung binh (0-5), null khi chua co danh gia';
