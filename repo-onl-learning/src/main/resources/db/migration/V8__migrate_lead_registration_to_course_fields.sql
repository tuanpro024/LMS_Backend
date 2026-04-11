-- Migrate lead registration schema from syllabus snapshot fields to course snapshot fields

ALTER TABLE lead_registration
    CHANGE COLUMN syllabus_id course_code VARCHAR(50) NOT NULL COMMENT 'Mã khóa học',
    CHANGE COLUMN syllabus_name course_name VARCHAR(255) NOT NULL COMMENT 'Snapshot tên khóa học tại thời điểm đăng ký',
    ADD COLUMN course_type VARCHAR(20) NULL COMMENT 'Loại khóa học' AFTER course_name;

ALTER TABLE lead_registration
    DROP INDEX uk_user_syllabus,
    DROP INDEX idx_syllabus_id,
    ADD UNIQUE KEY uk_user_course_code (user_id, course_code),
    ADD INDEX idx_course_code (course_code);
