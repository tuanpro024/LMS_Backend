-- Create user_card_progress table for tracking individual user progress
CREATE TABLE IF NOT EXISTS user_card_progress (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    card_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_LEARNED',
    last_reviewed_at TIMESTAMP,
    review_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_card (user_id, card_id),
    KEY idx_user_id (user_id),
    KEY idx_card_id (card_id),
    KEY idx_status (status),
    CONSTRAINT fk_user_card_progress_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Create index for faster queries
CREATE INDEX idx_user_study_set_status ON user_card_progress(user_id, card_id, status);
