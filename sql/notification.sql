-- 消息通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型：订单通知、系统通知、营销通知',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(1000) NOT NULL COMMENT '通知内容',
  `is_read` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
  `related_order_id` BIGINT COMMENT '关联订单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';