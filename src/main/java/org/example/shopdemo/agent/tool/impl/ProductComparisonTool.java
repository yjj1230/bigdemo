package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品对比工具
 * 负责处理商品对比相关的请求
 * 可以对比两个商品的详细信息
 */
@Component
public class ProductComparisonTool implements Tool {
    
    /**
     * 商品服务
     * 用于查询商品信息
     */
    @Autowired
    private ProductService productService;
    
    /**
     * 订单服务
     * 用于查询用户购买历史
     */
    @Autowired
    private OrderService orderService;
    
    /**
     * 执行商品对比功能
     * 对比两个商品的详细信息
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 商品对比的文本描述
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        // 从参数中获取商品ID
        Long product1Id = (Long) params.get("product1Id");
        Long product2Id = (Long) params.get("product2Id");
        
        // 如果没有提供商品ID，提示用户输入
        if (product1Id == null || product2Id == null) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "请提供两个商品的ID，例如：对比商品1和商品2");
            return Result.success(responseData);
        }
        
        try {
            // 查询两个商品的信息
            Product product1 = productService.getProductById(product1Id);
            Product product2 = productService.getProductById(product2Id);
            
            // 检查商品是否存在
            if (product1 == null) {
                return Result.error("抱歉，商品ID " + product1Id + " 不存在。");
            }
            if (product2 == null) {
                return Result.error("抱歉，商品ID " + product2Id + " 不存在。");
            }
            
            // 获取用户购买次数
            Map<Long, Integer> purchaseCount = getUserPurchaseCount(userId);
            int count1 = purchaseCount.getOrDefault(product1Id, 0);
            int count2 = purchaseCount.getOrDefault(product2Id, 0);
            
            // 构建商品对比的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("📊 商品对比\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // 对比商品1
            sb.append("【商品1】\n");
            sb.append("名称：").append(product1.getName()).append("\n");
            sb.append("价格：¥").append(product1.getPrice()).append("\n");
            sb.append("分类：").append(getCategoryName(product1.getCategoryId())).append("\n");
            sb.append("您的购买次数：").append(count1).append("\n");
            sb.append("────────────────────────────\n");
            
            // 对比商品2
            sb.append("【商品2】\n");
            sb.append("名称：").append(product2.getName()).append("\n");
            sb.append("价格：¥").append(product2.getPrice()).append("\n");
            sb.append("分类：").append(getCategoryName(product2.getCategoryId())).append("\n");
            sb.append("您的购买次数：").append(count2).append("\n");
            sb.append("────────────────────────────\n");
            
            // 价格对比
            sb.append("💰 价格对比：\n");
            if (product1.getPrice().compareTo(product2.getPrice()) < 0) {
                sb.append("  商品1更便宜，便宜 ¥").append(product2.getPrice().subtract(product1.getPrice())).append("\n");
            } else if (product1.getPrice().compareTo(product2.getPrice()) > 0) {
                sb.append("  商品2更便宜，便宜 ¥").append(product1.getPrice().subtract(product2.getPrice())).append("\n");
            } else {
                sb.append("  两款商品价格相同\n");
            }
            
            // 推荐建议
            sb.append("💡 推荐建议：\n");
            if (count1 > count2) {
                sb.append("  根据您的购买历史，您可能更喜欢商品1\n");
            } else if (count2 > count1) {
                sb.append("  根据您的购买历史，您可能更喜欢商品2\n");
            } else {
                sb.append("  两款商品都是不错的选择，建议根据您的需求选择\n");
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("对比商品失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户购买次数统计
     * @param userId 用户ID
     * @return 商品ID到购买次数的映射
     */
    private Map<Long, Integer> getUserPurchaseCount(Long userId) {
        List<Order> userOrders = orderService.getUserOrders(userId);
        Map<Long, Integer> purchaseCount = new java.util.HashMap<>();
        
        for (Order order : userOrders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                purchaseCount.put(item.getProductId(), 
                        purchaseCount.getOrDefault(item.getProductId(), 0) + item.getQuantity());
            }
        }
        
        return purchaseCount;
    }
    
    /**
     * 获取类别名称
     * @param categoryId 类别ID
     * @return 类别名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "未分类";
        return productService.getCategoryName(categoryId);
    }
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "商品对比工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "对比两个商品的详细信息";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"对比", "比较", "商品对比", "哪个好", "推荐哪个"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("PRODUCT_COMPARE");
    }
}
