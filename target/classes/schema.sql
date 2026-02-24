-- 创建默认管理员账号
-- 用户名: admin
-- 密码: admin123 (BCrypt加密后的值)
-- 角色: ADMIN (1)
-- 状态: NORMAL (1)

INSERT INTO user (username, password, email, phone, nickname, avatar, role, status, create_time, update_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@shopdemo.com', '13800138000', '系统管理员', NULL, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;
