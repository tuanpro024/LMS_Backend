-- Initialize Type records for multimedia service
-- These correspond to TypeName enum values

INSERT INTO types (id, name, description, created_at, updated_at, deleted)
VALUES
    ('01TYPE0000000000000000001', 'FREE', 'Tự do - Free content accessible to all users', NOW(), NOW(), FALSE),
    ('01TYPE0000000000000000002', 'LEARNING_PATH', 'Ôn luyện - Structured learning path', NOW(), NOW(), FALSE),
    ('01TYPE0000000000000000003', 'VIDEO_COURSE', 'Video khóa học - Video-based courses', NOW(), NOW(), FALSE),
    ('01TYPE0000000000000000004', 'LEARNING', '1-1, 1-n - Interactive learning sessions', NOW(), NOW(), FALSE)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_at = NOW();
