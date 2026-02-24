-- 创建测试物流数据
-- 首先查询订单ID
SELECT id, order_no, status FROM `order` LIMIT 5;

-- 为订单创建物流信息（假设订单ID为1）
-- 如果你的订单ID不同，请修改下面的订单ID

-- 物流信息1：已揽收
INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
VALUES (1, 'SF1234567890', '顺丰快递', '已揽收', '包裹已揽收，等待发货', '深圳市', NOW(), NOW());

-- 物流信息2：运输中
INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
VALUES (1, 'SF1234567890', '顺丰快递', '运输中', '包裹正在运输中，即将到达', '广州市', NOW(), NOW());

-- 物流信息3：派送中
INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
VALUES (1, 'SF1234567890', '顺丰快递', '派送中', '快递员正在派送中，请保持电话畅通', '北京市朝阳区', NOW(), NOW());

-- 物流信息4：已签收
INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
VALUES (1, 'SF1234567890', '顺丰快递', '已签收', '包裹已签收，感谢您的购买', '北京市朝阳区', NOW(), NOW());

-- 如果有其他订单，也可以为它们创建物流信息
-- 例如为订单ID 2创建物流
-- INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
-- VALUES (2, 'YT9876543210', '圆通快递', '已揽收', '包裹已揽收', '上海市', NOW(), NOW());

-- 查看创建的物流信息
SELECT * FROM logistics WHERE order_id = 1 ORDER BY create_time DESC;