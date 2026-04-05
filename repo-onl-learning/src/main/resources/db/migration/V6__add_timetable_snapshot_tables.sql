-- V6__add_timetable_snapshot_tables.sql
-- Lưu snapshot timetable từ CMS chỉ với dữ liệu cần hiển thị cho học sinh.

CREATE TABLE timetable_classes (
    id              VARCHAR(40)     NOT NULL                    COMMENT 'class_id từ CMS',
    class_code      VARCHAR(100),
    class_name      VARCHAR(255),
    class_status    VARCHAR(50),
    start_date      VARCHAR(40),
    end_date        VARCHAR(40),
    sync_version    BIGINT          NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_tcl_sync_version (sync_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE timetable_sessions (
    id                  VARCHAR(40)     NOT NULL                COMMENT 'session_id từ CMS',
    class_id            VARCHAR(40)     NOT NULL,
    syllabus_schedule_id VARCHAR(26),
    session_no          INT,
    title               VARCHAR(255),
    date                VARCHAR(40),
    session_date        VARCHAR(40),
    start_time          VARCHAR(20),
    end_time            VARCHAR(20),
    status              VARCHAR(50),
    session_status      VARCHAR(50),
    is_exam             TINYINT(1),
    tutor_id            VARCHAR(40),
    tutor_email         VARCHAR(255),
    tutor_name          VARCHAR(255),
    instructor_name     VARCHAR(255),
    class_room          VARCHAR(500),
    attendance_status   VARCHAR(50),
    check_in_at         VARCHAR(40),
    exam_id             VARCHAR(40),
    exam_type           VARCHAR(100),
    exam_title          VARCHAR(255),
    exam_datetime       VARCHAR(40),
    exam_link           VARCHAR(500),
    exam_note           TEXT,
    sync_version        BIGINT          NOT NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_ts_class_id (class_id),
    INDEX idx_ts_class_date_time (class_id, session_date, start_time),
    INDEX idx_ts_sync_version (sync_version),
    CONSTRAINT fk_ts_class FOREIGN KEY (class_id) REFERENCES timetable_classes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE timetable_class_participants (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    class_id            VARCHAR(40)     NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    participant_role    ENUM('STUDENT','TUTOR') NOT NULL,
    sync_version        BIGINT          NOT NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_tcp_class_email_role (class_id, email, participant_role),
    INDEX idx_tcp_email (email),
    INDEX idx_tcp_email_role_class (email, participant_role, class_id),
    INDEX idx_tcp_sync_version (sync_version),
    CONSTRAINT fk_tcp_class FOREIGN KEY (class_id) REFERENCES timetable_classes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
