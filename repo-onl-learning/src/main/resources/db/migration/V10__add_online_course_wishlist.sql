CREATE TABLE online_course_wishlist (
    id          VARCHAR(26) NOT NULL,
    user_id     VARCHAR(26) NOT NULL,
    course_id   VARCHAR(26) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    deleted     BIT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uq_onl_wishlist_user_course (user_id, course_id),
    INDEX idx_onl_wishlist_user (user_id),
    INDEX idx_onl_wishlist_course (course_id),
    INDEX idx_onl_wishlist_deleted (deleted),
    CONSTRAINT fk_onl_wishlist_course
        FOREIGN KEY (course_id) REFERENCES online_course (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
