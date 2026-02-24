-- 数据库索引优化脚本
-- 为购物商城数据库添加性能优化索引

USE users;

-- ============================================
-- 用户表索引优化
-- ============================================
-- 添加手机号索引（用于手机号登录/查询）
CREATE INDEX idx_phone ON `user`(phone);

-- 添加状态索引（用于筛选启用用户）
CREATE INDEX idx_status ON `user`(status);

-- 添加角色索引（用于管理员查询）
CREATE INDEX idx_role ON `user`(role);

-- 添加用户名+状态复合索引（用于登录时验证用户状态）
CREATE INDEX idx_username_status ON `user`(username, status);

-- ============================================
-- 商品表索引优化
-- ============================================
-- 添加商品名称全文索引（用于商品搜索）
-- 注意：MySQL 5.7.6+ 支持中文全文索引，需要ngram解析器
-- ALTER TABLE product ADD FULLTEXT INDEX ft_name_description(name, description) WITH PARSER ngram;

-- 添加价格索引（用于价格范围查询）
CREATE INDEX idx_price ON product(price);

-- 添加销量索引（用于销量排序）
CREATE INDEX idx_sales ON product(sales);

-- 添加库存索引（用于库存查询）
CREATE INDEX idx_stock ON product(stock);

-- 添加分类+状态+创建时间复合索引（用于分类商品查询）
CREATE INDEX idx_category_status_time ON product(category_id, status, create_time);

-- 添加状态+创建时间复合索引（用于首页商品列表）
CREATE INDEX idx_status_create_time ON product(status, create_time);

-- 添加状态+销量复合索引（用于热销商品查询）
CREATE INDEX idx_status_sales ON product(status, sales DESC);

-- ============================================
-- 订单表索引优化
-- ============================================
-- 添加用户+状态+创建时间复合索引（用于用户订单查询）
CREATE INDEX idx_user_status_time ON `order`(user_id, status, create_time DESC);

-- 添加用户+状态复合索引（用于特定状态订单查询）
CREATE INDEX idx_user_status ON `order`(user_id, status);

-- 添加状态+创建时间复合索引（用于订单管理）
CREATE INDEX idx_status_create_time ON `order`(status, create_time DESC);

-- 添加支付时间索引（用于订单统计）
CREATE INDEX idx_pay_time ON `order`(pay_time);

-- 添加完成时间索引（用于订单统计）
CREATE INDEX idx_finish_time ON `order`(finish_time);

-- ============================================
-- 订单详情表索引优化
-- ============================================
-- 添加订单+商品复合索引（用于查询订单中的商品）
CREATE INDEX idx_order_product ON order_item(order_id, product_id);

-- ============================================
-- 用户地址表索引优化
-- ============================================
-- 添加用户+默认地址复合索引（用于查询默认地址）
CREATE INDEX idx_user_default ON address(user_id, is_default);

-- ============================================
-- 购物车表索引优化
-- ============================================
-- 已有唯一索引 uk_user_product (user_id, product_id)，无需额外添加

-- ============================================
-- 商品评价表索引优化
-- ============================================
-- 添加商品+创建时间复合索引（用于商品评价查询）
CREATE INDEX idx_product_time ON product_review(product_id, create_time DESC);

-- 添加用户+创建时间复合索引（用于用户评价查询）
CREATE INDEX idx_user_time ON product_review(user_id, create_time DESC);

-- 添加商品+评分复合索引（用于筛选评分）
CREATE INDEX idx_product_rating ON product_review(product_id, rating);

-- ============================================
-- 商品收藏表索引优化
-- ============================================
-- 添加用户+创建时间复合索引（用于用户收藏列表查询）
CREATE INDEX idx_user_create_time ON product_favorite(user_id, create_time DESC);

-- ============================================
-- 搜索历史表索引优化
-- ============================================
-- 添加用户+最后搜索时间复合索引（用于用户搜索历史查询）
CREATE INDEX idx_user_last_time ON search_history(user_id, last_search_time DESC);

-- 添加关键词+搜索次数复合索引（用于热门搜索）
CREATE INDEX idx_keyword_count ON search_history(keyword, search_count DESC);

-- ============================================
-- 商品分类表索引优化
-- ============================================
-- 添加父ID+状态复合索引（用于查询子分类）
CREATE INDEX idx_parent_status ON category(parent_id, status);

-- 添加层级+排序复合索引（用于分类列表）
CREATE INDEX idx_level_sort ON category(level, sort_order);

-- ============================================
-- 索引使用建议
-- ============================================
-- 1. 定期分析索引使用情况：
--    SELECT * FROM sys.schema_index_statistics WHERE table_schema = 'shopmall';
--
-- 2. 删除未使用的索引：
--    DROP INDEX index_name ON table_name;
--
-- 3. 定期优化表：
--    OPTIMIZE TABLE table_name;
--
-- 4. 监控慢查询：
--    SHOW VARIABLES LIKE 'slow_query_log';
--    SET GLOBAL slow_query_log = 'ON';
--    SET GLOBAL long_query_time = 2;
--
-- 5. 使用EXPLAIN分析查询计划：
--    EXPLAIN SELECT * FROM product WHERE category_id = 1 AND status = 1;
