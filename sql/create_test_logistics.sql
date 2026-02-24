-- 为所有已发货的订单（status >= 2）创建物流信息

-- 首先查看所有订单
SELECT id, order_no, status, receiver_name, receiver_address FROM `order` ORDER BY id;

-- 为订单ID 1 创建完整的物流轨迹
INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
VALUES
(1, 'SF1234567890', '顺丰快递', '已揽收', '包裹已揽收，等待发货', '深圳市', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR),
(1, 'SF1234567890', '顺丰快递', '运输中', '包裹正在运输中，即将到达', '广州市', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR),
(1, 'SF1234567890', '顺丰快递', '派送中', '快递员正在派送中，请保持电话畅通', '北京市朝阳区', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR),
(1, 'SF1234567890', '顺丰快递', '已签收', '包裹已签收，感谢您的购买', '北京市朝阳区', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR);

-- 为订单ID 2 创建物流轨迹（如果存在）
-- INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time)
-- VALUES
-- (2, 'YT9876543210', '圆通快递', '已揽收', '包裹已揽收', '上海市', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR),
-- (2, 'YT9876543210', '圆通快递', '运输中', '包裹正在运输中', '北京市', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR),
-- (2, 'YT9876543210', '圆通快递', '已签收', '包裹已签收', '北京市海淀区', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR);

-- 查看创建的物流信息
SELECT * FROM logistics ORDER BY create_time DESC LIMIT 20;