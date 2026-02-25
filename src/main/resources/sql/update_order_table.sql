-- 更新订单表，添加优惠券相关字段
ALTER TABLE `order` 
ADD COLUMN `coupon_id` BIGINT DEFAULT NULL COMMENT '优惠券ID' AFTER `user_id`,
ADD COLUMN `original_amount` DECIMAL(10, 2) DEFAULT NULL COMMENT '原始金额' AFTER `coupon_id`,
ADD COLUMN `coupon_discount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠券折扣金额' AFTER `original_amount`;

-- 添加索引以提高查询性能
ALTER TABLE `order` ADD INDEX `idx_coupon_id` (`coupon_id`);
