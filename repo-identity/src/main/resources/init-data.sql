INSERT INTO roles(id, name, created_at, updated_at, deleted) VALUES
    ('01JFZC5Y3K1M7X9C6T2B4N8PQ', 'ROLE_ADMIN', NOW(), NOW(), false)
    ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO roles(id, name, created_at, updated_at, deleted) VALUES
    ('01JFZC5Y3K1M7X9C6T2B4N8PR', 'ROLE_USER', NOW(), NOW(), false)
    ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO roles(id, name, created_at, updated_at, deleted) VALUES
    ('01JFZC5Y3K1M7X9C6T2B4TEAC', 'ROLE_TEACHER', NOW(), NOW(), false)
    ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Thêm user Admin
INSERT INTO users(id, email, password, full_name, auth_provider, status, email_verified, created_at, updated_at, deleted)
SELECT '01JFZC5Y3K1M7X9C6T2B4N8PS',
       'admin@local',
       '$2a$12$Dg0z4Hq3ThMWuha3KvJ5/.3FJAPgMLj3pqBNbFbVCP6bZ4VMmz6h.',
       'Admin',
       'LOCAL',
       'ACTIVE',
       true,
       NOW(),
       NOW(),
       false
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@local');

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@local'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- Thêm user Teacher
INSERT INTO users(id, email, password, full_name, auth_provider, status, email_verified, created_at, updated_at, deleted)
SELECT '01JFZC5Y3K1M7X9C6T2B4N8PT',
       'teacher@local',
       '$2a$12$Dg0z4Hq3ThMWuha3KvJ5/.3FJAPgMLj3pqBNbFbVCP6bZ4VMmz6h.',
       'Teacher',
       'LOCAL',
       'ACTIVE',
       true,
       NOW(),
       NOW(),
       false
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'teacher@local');

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_TEACHER'
WHERE u.email = 'teacher@local'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- Thêm user Student
INSERT INTO users(id, email, password, full_name, auth_provider, status, email_verified, created_at, updated_at, deleted)
SELECT '01STUDENT00000000000000001',
       'student@local.com',
       '$2a$12$Dg0z4Hq3ThMWuha3KvJ5/.3FJAPgMLj3pqBNbFbVCP6bZ4VMmz6h.',
       'Local Student',
       'LOCAL',
       'ACTIVE',
       true,
       NOW(),
       NOW(),
       false
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'student@local.com');

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_USER'
WHERE u.email = 'student@local.com'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

