-- 购物商城数据库初始化脚本
-- 创建数据库


USE users;

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键自增',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
  `password` VARCHAR(255) NOT NULL COMMENT '密码，加密存储',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱，唯一',
  `phone` VARCHAR(20) COMMENT '手机号',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` TINYINT DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_email (`email`),
  INDEX idx_phone (`phone`),
  INDEX idx_status (`status`),
  INDEX idx_role (`role`),
  INDEX idx_username_status (`username`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商品分类表
CREATE TABLE `category` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID，主键自增',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `level` TINYINT DEFAULT 1 COMMENT '分类层级：1-一级分类，2-二级分类',
  `icon` VARCHAR(255) COMMENT '分类图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_parent_id (`parent_id`),
  INDEX idx_parent_status (`parent_id`, `status`),
  INDEX idx_level_sort (`level`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE `product` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID，主键自增',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `description` TEXT COMMENT '商品描述',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
  `original_price` DECIMAL(10, 2) COMMENT '原价',
  `stock` INT DEFAULT 0 COMMENT '库存数量',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `main_image` VARCHAR(255) COMMENT '主图URL',
  `images` TEXT COMMENT '商品图片，JSON格式存储',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_category_id (`category_id`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_price (`price`),
  INDEX idx_sales (`sales`),
  INDEX idx_stock (`stock`),
  INDEX idx_category_status_time (`category_id`, `status`, `create_time`),
  INDEX idx_status_create_time (`status`, `create_time`),
  INDEX idx_status_sales (`status`, `sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 用户地址表
CREATE TABLE `address` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID，主键自增',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `district` VARCHAR(50) COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_user_default (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 购物车表
CREATE TABLE `cart` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车ID，主键自增',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT DEFAULT 1 COMMENT '商品数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_product (`user_id`, `product_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 订单表
CREATE TABLE `order` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID，主键自增',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10, 2) NOT NULL COMMENT '实付金额',
  `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消，5-已退款',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
  `remark` VARCHAR(500) COMMENT '订单备注',
  `pay_time` DATETIME COMMENT '支付时间',
  `ship_time` DATETIME COMMENT '发货时间',
  `finish_time` DATETIME COMMENT '完成时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_order_no (`order_no`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_user_status_time (`user_id`, `status`, `create_time`),
  INDEX idx_user_status (`user_id`, `status`),
  INDEX idx_status_create_time (`status`, `create_time`),
  INDEX idx_pay_time (`pay_time`),
  INDEX idx_finish_time (`finish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单详情表
CREATE TABLE `order_item` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单详情ID，主键自增',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `product_image` VARCHAR(255) COMMENT '商品图片',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total_price` DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_order_id (`order_id`),
  INDEX idx_product_id (`product_id`),
  INDEX idx_order_product (`order_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- 插入测试数据
-- 插入管理员账号
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@shop.com', '13800138000', '超级管理员', 1, 1);

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `nickname`, `role`, `status`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@shop.com', '13900139000', '测试用户', 0, 1);

-- 插入商品分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`, `status`) VALUES
('电子产品', 0, 1, 1, 1),
('服装鞋帽', 0, 1, 2, 1),
('食品饮料', 0, 1, 3, 1),
('手机', 1, 2, 1, 1),
('电脑', 1, 2, 2, 1),
('男装', 2, 2, 1, 1),
('女装', 2, 2, 2, 1);

-- 插入测试商品
INSERT INTO `product` (`name`, `description`, `category_id`, `price`, `original_price`, `stock`, `sales`, `main_image`, `status`) VALUES
('iPhone 15 Pro Max', '苹果最新旗舰手机，搭载A17 Pro芯片', 4, 9999.00, 10999.00, 100, 50, 'https://example.com/iphone.jpg', 1),
('MacBook Pro 14', '苹果笔记本电脑，M3 Pro芯片', 5, 14999.00, 16999.00, 50, 30, 'https://example.com/macbook.jpg', 1),
('男士休闲T恤', '纯棉舒适，夏季必备', 6, 99.00, 129.00, 200, 100, 'https://example.com/tshirt.jpg', 1),
('女士连衣裙', '优雅时尚，适合多种场合', 7, 299.00, 399.00, 150, 80, 'https://example.com/dress.jpg', 1);

-- 商品评价表
CREATE TABLE `product_review` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID，主键自增',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
  `content` TEXT NOT NULL COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片，JSON格式存储',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `reply` TEXT COMMENT '商家回复',
  `reply_time` DATETIME COMMENT '回复时间',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_product_id (`product_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_product_time (`product_id`, `create_time`),
  INDEX idx_user_time (`user_id`, `create_time`),
  INDEX idx_product_rating (`product_id`, `rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- 商品收藏表
CREATE TABLE `product_favorite` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID，主键自增',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_user_product (`user_id`, `product_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_user_create_time (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- 搜索历史表
CREATE TABLE `search_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '搜索历史ID，主键自增',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `keyword` VARCHAR(200) NOT NULL COMMENT '搜索关键词',
  `search_count` INT DEFAULT 1 COMMENT '搜索次数',
  `last_search_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后搜索时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_keyword (`keyword`),
  INDEX idx_last_search_time (`last_search_time`),
  INDEX idx_user_last_time (`user_id`, `last_search_time`),
  INDEX idx_keyword_count (`keyword`, `search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';
