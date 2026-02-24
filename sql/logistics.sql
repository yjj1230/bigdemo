-- 物流追踪表
CREATE TABLE IF NOT EXISTS `logistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `logistics_no` VARCHAR(100) NOT NULL COMMENT '物流单号',
  `logistics_company` VARCHAR(100) NOT NULL COMMENT '物流公司',
  `status` VARCHAR(50) NOT NULL COMMENT '物流状态：已揽收、运输中、派送中、已签收',
  `description` VARCHAR(500) COMMENT '物流描述',
  `location` VARCHAR(200) COMMENT '当前位置',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流追踪表';