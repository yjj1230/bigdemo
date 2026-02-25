package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Logistics;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.service.LogisticsService;
import org.example.shopdemo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流工具
 * 负责处理物流相关的查询请求
 * 可以查询订单的物流信息和物流轨迹
 */
@Component
public class LogisticsTool implements Tool {
    
    /**
     * 物流服务
     * 用于查询物流信息
     */
    @Autowired
    private LogisticsService logisticsService;
    
    /**
     * 订单服务
     * 用于查询订单信息
     */
    @Autowired
    private OrderService orderService;
    
    /**
     * 执行物流查询功能
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        // 从参数中获取订单号
        String orderNo = (String) params.get("orderNo");
        
        // 如果没有订单号，提示用户输入
        if (orderNo == null || orderNo.isEmpty()) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "请提供订单号，例如：查询订单1234567890的物流信息");
            return Result.success(responseData);
        }
        
        try {
            // 通过订单号查询订单详情
            Order order = orderService.getOrderByOrderNo(orderNo);
            
            // 检查订单是否存在
            if (order == null) {
                return Result.error("抱歉，没有找到订单号为 " + orderNo + " 的订单。");
            }
            
            // 检查用户是否有权限查看该订单
            if (!order.getUserId().equals(userId)) {
                return Result.error("抱歉，您无权查看该订单的物流信息。");
            }
            
            // 检查订单是否已发货
            if (order.getStatus() < 2) {
                return Result.error("订单 " + orderNo + " 还未发货，暂无物流信息。");
            }
            
            // 查询物流信息
            List<Logistics> logisticsList = logisticsService.getLogisticsByOrderId(order.getId());
            
            // 检查物流信息是否存在
            if (logisticsList == null || logisticsList.isEmpty()) {
                return Result.error("订单 " + orderNo + " 的物流信息暂未更新。");
            }
            
            // 获取最新的物流信息
            Logistics latestLogistics = logisticsList.get(0);
            
            // 构建物流信息的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("🚚 物流追踪\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("订单号：").append(order.getOrderNo()).append("\n");
            sb.append("物流公司：").append(latestLogistics.getLogisticsCompany()).append("\n");
            sb.append("运单号：").append(latestLogistics.getLogisticsNo()).append("\n");
            sb.append("\n物流轨迹：\n");
            
            // 遍历物流轨迹，显示每条物流信息
            for (Logistics logistics : logisticsList) {
                sb.append("  ").append(logistics.getUpdateTime()).append("\n");
                sb.append("  ").append(logistics.getStatus()).append("\n");
                sb.append("  ").append(logistics.getLocation()).append("\n");
                sb.append("  ───────────────────────────\n");
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            responseData.put("navigationType", "logistics");
            
            // 创建跳转参数
            Map<String, Object> navParams = new HashMap<>();
            navParams.put("orderId", order.getId());
            navParams.put("orderNo", order.getOrderNo());
            responseData.put("navigationParams", navParams);
            
            // 返回包含跳转信息的响应
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("查询物流信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "物流工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "查询订单物流信息、物流追踪";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"物流", "快递", "配送", "发货", "运单"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("LOGISTICS_QUERY");
    }
}
