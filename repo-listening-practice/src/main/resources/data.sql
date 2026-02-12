-- Initial Data for Types
-- IDs are manually generated ULID-like strings for consistency
INSERT IGNORE INTO types (id, name, description, created_at, updated_at, deleted) VALUES ('01HQJ7X0000000000000000001', 'FREE', 'Available for free users', NOW(), NOW(), 0);
INSERT IGNORE INTO types (id, name, description, created_at, updated_at, deleted) VALUES ('01HQJ7X0000000000000000002', 'LEARNING_PATH', 'Structured learning path', NOW(), NOW(), 0);
INSERT IGNORE INTO types (id, name, description, created_at, updated_at, deleted) VALUES ('01HQJ7X0000000000000000003', 'VIDEO_COURSE', 'Course based on videos', NOW(), NOW(), 0);
INSERT IGNORE INTO types (id, name, description, created_at, updated_at, deleted) VALUES ('01HQJ7X0000000000000000004', 'LEARNING', 'General learning content', NOW(), NOW(), 0);
