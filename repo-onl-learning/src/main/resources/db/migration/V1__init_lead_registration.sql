-- V1__init_lead_registration.sql
-- Flyway managed schema for repo-onl-learning

CREATE TABLE lead_registration (
    id              VARCHAR(26)     NOT NULL,
    syllabus_id     VARCHAR(50)     NOT NULL COMMENT 'ID khóa học bên CMS',
    syllabus_name   VARCHAR(255)    NOT NULL COMMENT 'Snapshot tên khóa học',
    user_id         VARCHAR(26)              COMMENT 'userId từ JWT (null nếu guest - không được phép theo business)',
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    note            TEXT,
    registered_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_syllabus (user_id, syllabus_id),
    INDEX idx_syllabus_id (syllabus_id),
    INDEX idx_registered_at (registered_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
