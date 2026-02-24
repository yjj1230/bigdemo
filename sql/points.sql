-- 积分记录表
CREATE TABLE IF NOT EXISTS `points` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `points` INT NOT NULL COMMENT '积分数量（正数为获得，负数为消费）',
  `type` VARCHAR(50) NOT NULL COMMENT '积分类型：购物获得、评价获得、签到获得、积分消费',
  `description` VARCHAR(500) COMMENT '积分说明',
  `related_order_id` BIGINT COMMENT '关联订单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';