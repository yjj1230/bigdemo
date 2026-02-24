-- 创建优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `description` varchar(500) DEFAULT NULL COMMENT '优惠券描述',
  `type` tinyint NOT NULL COMMENT '优惠券类型：1-满减券，2-折扣券，3-免运费券',
  `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '减免金额（满减券）',
  `discount_rate` decimal(5,4) DEFAULT NULL COMMENT '折扣率（折扣券）',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT '最低消费金额',
  `max_discount` decimal(10,2) DEFAULT NULL COMMENT '最大优惠金额（折扣券）',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '总发行数量',
  `received_count` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
  `used_count` int NOT NULL DEFAULT '0' COMMENT '已使用数量',
  `limit_per_user` int NOT NULL DEFAULT '1' COMMENT '每人限领数量',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-未开始，2-进行中，3-已结束',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 创建用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `type` tinyint NOT NULL COMMENT '优惠券类型：1-满减券，2-折扣券，3-免运费券',
  `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '减免金额',
  `discount_rate` decimal(5,4) DEFAULT NULL COMMENT '折扣率',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT '最低消费金额',
  `max_discount` decimal(10,2) DEFAULT NULL COMMENT '最大优惠金额',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-未使用，2-已使用，3-已过期',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 插入示例优惠券数据
INSERT INTO `coupon` (`name`, `description`, `type`, `discount_amount`, `discount_rate`, `min_amount`, `max_discount`, `total_count`, `limit_per_user`, `start_time`, `end_time`, `status`) VALUES
('新人专享券', '新用户注册即可领取', 1, 50.00, NULL, 200.00, NULL, 1000, 1, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 2),
('满500减100', '满500元减100元', 1, 100.00, NULL, 500.00, NULL, 500, 2, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 2),
('8折优惠券', '全场8折优惠', 2, NULL, 0.8000, 100.00, 200.00, 300, 1, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 2),
('免运费券', '免运费优惠', 3, NULL, NULL, 0.00, NULL, 2000, 3, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 2),
('满1000减200', '满1000元减200元', 1, 200.00, NULL, 1000.00, NULL, 200, 1, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 2);
