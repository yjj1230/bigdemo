package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单工具
 * 负责处理订单相关的查询请求
 * 可以查询订单详情和订单列表
 */
@Component
public class OrderTool implements Tool {
    
    /**
     * 订单服务
     * 用于查询订单信息
     */
    @Autowired
    private OrderService orderService;
    
    /**
     * 执行订单查询功能
     * 根据参数决定查询订单详情还是订单列表
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result execute(String message, Long userId, Map<String, Object> params) {
        // 从参数中获取订单号
        String orderNo = (String) params.get("orderNo");
        // 如果有订单号，查询订单详情
        if (orderNo != null && !orderNo.isEmpty()) {
            return getOrderDetail(orderNo, userId);
        } else {
            // 否则查询订单列表
            return getOrderList(userId);
        }
    }
    
    /**
     * 查询订单详情
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result getOrderDetail(String orderNo, Long userId) {
        try {
            // 通过订单号调用订单服务查询订单详情
            Order order = orderService.getOrderByOrderNo(orderNo);
            
            // 检查订单是否存在
            if (order == null) {
                return Result.error("抱歉，没有找到订单号为 " + orderNo + " 的订单。");
            }
            
            // 检查用户是否有权限查看该订单
            if (!order.getUserId().equals(userId)) {
                return Result.error("抱歉，您无权查看该订单。");
            }
            
            // 查询订单项
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            
            // 构建订单详情的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("📦 订单详情\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("订单号：").append(order.getOrderNo()).append("\n");
            sb.append("订单状态：").append(getOrderStatusText(order.getStatus())).append("\n");
            sb.append("订单金额：¥").append(order.getTotalAmount()).append("\n");
            sb.append("下单时间：").append(order.getCreateTime()).append("\n");
            sb.append("收货人：").append(order.getReceiverName()).append("\n");
            sb.append("联系电话：").append(order.getReceiverPhone()).append("\n");
            sb.append("收货地址：").append(order.getReceiverAddress()).append("\n");
            
            // 如果订单包含商品，显示商品列表
            if (items != null && !items.isEmpty()) {
                sb.append("\n商品列表：\n");
                for (OrderItem item : items) {
                    sb.append("  • ").append(item.getProductName())
                      .append(" × ").append(item.getQuantity())
                      .append(" ¥").append(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()))).append("\n");
                }
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            
            // 返回响应
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("查询订单详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询订单列表
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result getOrderList(Long userId) {
        try {
            // 调用订单服务查询用户的订单列表
            List<Order> orders = orderService.getUserOrders(userId);
            
            // 检查订单列表是否为空
            if (orders == null || orders.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "您还没有任何订单。快去选购商品吧！");
                return Result.success(responseData);
            }
            
            // 构建订单列表的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("📋 我的订单列表\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // 遍历订单列表，显示每个订单的信息
            for (Order order : orders) {
                sb.append("订单号：").append(order.getOrderNo()).append("\n");
                sb.append("状态：").append(getOrderStatusText(order.getStatus())).append("\n");
                sb.append("金额：¥").append(order.getTotalAmount()).append("\n");
                sb.append("时间：").append(order.getCreateTime()).append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            }
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            
            // 返回响应
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("查询订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取订单状态的文本描述
     * @param status 订单状态码
     * @return 订单状态的文本描述
     */
    private String getOrderStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "订单工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "查询订单信息、订单详情、订单列表";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"查订单", "我的订单", "订单详情", "订单状态", "订单列表"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("ORDER_QUERY");
    }
}
