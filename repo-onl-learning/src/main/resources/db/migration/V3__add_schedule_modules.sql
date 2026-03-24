-- V3__add_schedule_modules.sql
-- Bảng lưu các module ôn luyện (flashcard, writing, kanji, pronunciation, quiz)
-- gắn vào từng buổi học (syllabus_schedule) bởi ADMIN/TEACHER_MANAGER.

CREATE TABLE schedule_modules (
    id                  VARCHAR(26)     NOT NULL                    COMMENT 'ULID sinh bởi BaseEntity',
    schedule_id         VARCHAR(26)     NOT NULL                    COMMENT 'FK → syllabus_schedule.id',
    module_type         ENUM('FLASHCARD','WRITING','KANJI','PRONUNCIATION','QUIZ')
                                        NOT NULL                    COMMENT 'Loại module ôn luyện',
    module_order        INT             NOT NULL                    COMMENT 'Thứ tự hiển thị trong buổi học',
    title               VARCHAR(255)    NOT NULL                    COMMENT 'Tên module',
    description         TEXT                                        COMMENT 'Mô tả module',
    content_set_id      VARCHAR(26)                                 COMMENT 'ID của StudySet ở repo ngoài',
    content_folder_id   VARCHAR(26)                                 COMMENT 'ID của Folder ở repo ngoài (optional)',
    external_ref_json   TEXT                                        COMMENT 'JSON metadata (repo name, deeplink, ...)',
    is_required         BIT(1)          NOT NULL DEFAULT 1          COMMENT 'Module bắt buộc?',
    is_active           BIT(1)          NOT NULL DEFAULT 1          COMMENT 'Hiển thị?',
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    deleted             BIT(1)          NOT NULL DEFAULT 0          COMMENT 'Soft delete',

    PRIMARY KEY (id),
    -- Safety net cho concurrency: chặn 2 module cùng order trong cùng buổi (active)
    UNIQUE KEY uk_schedule_order (schedule_id, module_order),
    INDEX idx_sm_schedule_id (schedule_id),
    INDEX idx_sm_deleted (deleted),
    CONSTRAINT fk_sm_schedule
        FOREIGN KEY (schedule_id) REFERENCES syllabus_schedule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
