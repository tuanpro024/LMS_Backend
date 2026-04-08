CREATE TABLE schedule_module_user_progress (
    id                  VARCHAR(26)     NOT NULL,
    module_id           VARCHAR(26)     NOT NULL,
    user_id             VARCHAR(64)     NOT NULL,
    user_email          VARCHAR(255)    NULL,
    progress_percentage DOUBLE          NOT NULL DEFAULT 0,
    completed           BIT(1)          NOT NULL DEFAULT 0,
    completed_items     INT             NULL,
    total_items         INT             NULL,
    last_interacted_at  DATETIME(6)     NULL,
    completed_at        DATETIME(6)     NULL,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    deleted             BIT(1)          NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_smup_module_user (module_id, user_id),
    KEY idx_smup_module (module_id),
    KEY idx_smup_user (user_id),
    KEY idx_smup_deleted (deleted),
    CONSTRAINT fk_smup_module FOREIGN KEY (module_id) REFERENCES schedule_modules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
