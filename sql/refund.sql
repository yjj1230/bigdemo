-- 退款退货表
CREATE TABLE IF NOT EXISTS `refund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_item_id` BIGINT COMMENT '订单商品ID（部分退款时使用）',
  `refund_no` VARCHAR(100) NOT NULL COMMENT '退款单号',
  `reason` VARCHAR(500) NOT NULL COMMENT '退款原因',
  `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
  `status` VARCHAR(50) NOT NULL COMMENT '退款状态：待审核、审核通过、审核拒绝、退款中、退款完成',
  `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
  `refund_method` VARCHAR(50) COMMENT '退款方式：原路退回、银行转账',
  `refund_account` VARCHAR(200) COMMENT '退款账户',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_refund_no` (`refund_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款退货表';