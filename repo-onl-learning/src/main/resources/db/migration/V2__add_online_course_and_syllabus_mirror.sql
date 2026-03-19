-- V2__add_online_course_and_syllabus_mirror.sql
-- Tạo bảng online_course và các bảng mirror CMS syllabus

-- ============================================================
-- 1. online_course
-- ============================================================
CREATE TABLE online_course (
    id              VARCHAR(26)     NOT NULL                    COMMENT 'ULID tự sinh bởi BaseEntity',
    name            VARCHAR(255)    NOT NULL                    COMMENT 'Tên khóa học',
    thumbnail       TEXT                                        COMMENT 'URL ảnh thumbnail',
    description     TEXT                                        COMMENT 'Mô tả khóa học',
    syllabus_id     VARCHAR(26)                                 COMMENT 'FK -> syllabus.id (CMS mirror)',
    price           DECIMAL(12,2)                               COMMENT 'Giá khóa học',
    rating          DOUBLE          NOT NULL DEFAULT 0.0        COMMENT 'Số sao trung bình (0-5)',
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    deleted         BIT(1)          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    INDEX idx_syllabus_id (syllabus_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. syllabus  (mirror từ CMS)
-- ============================================================
CREATE TABLE syllabus (
    id                      VARCHAR(26)     NOT NULL            COMMENT 'ULID từ CMS',
    code                    VARCHAR(50),
    name                    VARCHAR(200)    NOT NULL,
    hsk_level               VARCHAR(20),
    type                    VARCHAR(50),
    version                 VARCHAR(20),
    author                  VARCHAR(100),
    description             TEXT,
    distribution_hours      TEXT,
    document_type           VARCHAR(100),
    prerequisite            TEXT,
    training_program        VARCHAR(100),
    student_responsibilities TEXT,
    minimum_passing_score   VARCHAR(50),
    score_range             VARCHAR(255),
    notes                   TEXT,
    teaching_methods        JSON,
    learning_tools          JSON,
    status                  ENUM('DRAFT','PENDING','APPROVED','REJECTED','ACTIVE') NOT NULL DEFAULT 'DRAFT',
    is_visible              BIT(1)          NOT NULL DEFAULT 1,
    reject_reason           TEXT,
    created_by              VARCHAR(26),
    approved_by             VARCHAR(26),
    total_sessions          INT,
    created_at              DATETIME(6),
    updated_at              DATETIME(6),
    approved_at             DATETIME(6),
    deleted                 BIT(1)          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_hsk_level (hsk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. syllabus_clo
-- ============================================================
CREATE TABLE syllabus_clo (
    id              VARCHAR(50)     NOT NULL                    COMMENT 'CLO id từ CMS (e.g. "CLO 1")',
    syllabus_id     VARCHAR(26)     NOT NULL,
    name            VARCHAR(200),
    description     TEXT,

    PRIMARY KEY (id),
    INDEX idx_syllabus_id (syllabus_id),
    CONSTRAINT fk_clo_syllabus FOREIGN KEY (syllabus_id) REFERENCES syllabus(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. syllabus_material
-- ============================================================
CREATE TABLE syllabus_material (
    id              BIGINT          NOT NULL                    COMMENT 'id auto từ CMS',
    syllabus_id     VARCHAR(26)     NOT NULL,
    title           VARCHAR(255),
    type            VARCHAR(50),
    isbn            VARCHAR(50),
    author          VARCHAR(255),
    publisher       VARCHAR(255),
    year            VARCHAR(10),
    edition         VARCHAR(100),

    PRIMARY KEY (id),
    INDEX idx_syllabus_id (syllabus_id),
    CONSTRAINT fk_material_syllabus FOREIGN KEY (syllabus_id) REFERENCES syllabus(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. syllabus_schedule
-- ============================================================
CREATE TABLE syllabus_schedule (
    id                      VARCHAR(26)     NOT NULL            COMMENT 'ULID từ CMS',
    syllabus_id             VARCHAR(26)     NOT NULL,
    session_no              INT,
    topic                   VARCHAR(255),
    content                 TEXT,
    delivery                VARCHAR(100),
    session_lo              TEXT,
    core_clo                VARCHAR(100),
    supporting_clo          VARCHAR(100),
    evidence                TEXT,
    itu                     VARCHAR(10),
    student_materials       TEXT,
    teacher_materials       TEXT,
    student_tasks           TEXT,
    teacher_tasks           TEXT,
    student_materials_link  VARCHAR(500),
    teacher_materials_link  VARCHAR(500),
    module_on_luyen         VARCHAR(500),

    PRIMARY KEY (id),
    INDEX idx_syllabus_id (syllabus_id),
    INDEX idx_session_no (session_no),
    CONSTRAINT fk_schedule_syllabus FOREIGN KEY (syllabus_id) REFERENCES syllabus(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. syllabus_grading
-- ============================================================
CREATE TABLE syllabus_grading (
    id                  VARCHAR(26)     NOT NULL                COMMENT 'ULID từ CMS',
    syllabus_id         VARCHAR(26)     NOT NULL,
    item                VARCHAR(100),
    type                VARCHAR(50),
    weight              INT,
    timing              VARCHAR(100),
    duration            VARCHAR(50),
    clo                 VARCHAR(100),
    organizational_form TEXT,
    criteria            TEXT,
    content_scope       TEXT,

    PRIMARY KEY (id),
    INDEX idx_syllabus_id (syllabus_id),
    CONSTRAINT fk_grading_syllabus FOREIGN KEY (syllabus_id) REFERENCES syllabus(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
