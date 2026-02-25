-- 为refund表添加user_id字段
ALTER TABLE `refund` ADD COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID' AFTER `id`;
