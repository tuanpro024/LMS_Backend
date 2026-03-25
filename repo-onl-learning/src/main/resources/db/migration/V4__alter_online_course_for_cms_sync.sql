ALTER TABLE online_course
    ADD COLUMN code VARCHAR(50) NULL AFTER id,
    ADD COLUMN course_type VARCHAR(20) NULL AFTER name,
    ADD COLUMN level VARCHAR(20) NULL AFTER course_type,
    ADD COLUMN total_lessons INT NULL AFTER level,
    ADD COLUMN cms_synced BIT(1) NOT NULL DEFAULT 0 AFTER rating;

CREATE INDEX idx_online_course_code ON online_course(code);
CREATE INDEX idx_online_course_type ON online_course(course_type);
CREATE INDEX idx_online_course_cms_synced ON online_course(cms_synced);
