package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.CartService;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品推荐工具
 * 负责处理商品推荐相关的请求
 * 可以根据用户历史行为推荐商品
 */
@Component
public class ProductRecommendationTool implements Tool {
    
    /**
     * 商品服务
     * 用于查询商品信息
     */
    @Autowired
    private ProductService productService;
    
    /**
     * 订单服务
     * 用于查询用户订单历史
     */
    @Autowired
    private OrderService orderService;
    
    /**
     * 购物车服务
     * 用于查询用户购物车
     */
    @Autowired
    private CartService cartService;
    
    /**
     * 执行商品推荐功能
     * 根据用户历史购买记录推荐商品
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 商品推荐的文本描述
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        try {
            // 获取用户购买历史
            Map<String, Long> categoryCount = getUserPurchaseHistory(userId);
            
            // 如果没有购买历史，推荐热门商品
            if (categoryCount.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getPopularProducts());
                return Result.success(responseData);
            }
            
            // 找到用户购买最多的类别
            String favoriteCategory = categoryCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            
            if (favoriteCategory == null) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getPopularProducts());
                return Result.success(responseData);
            }
            
            // 推荐该类别的商品
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", getProductsByCategory(favoriteCategory));
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("推荐商品失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户购买历史统计
     * @param userId 用户ID
     * @return 类别购买次数统计
     */
    private Map<String, Long> getUserPurchaseHistory(Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        List<Long> allProductIds = new java.util.ArrayList<>();
        
        for (Order order : orders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                allProductIds.add(item.getProductId());
            }
        }
        
        List<Product> allProducts = productService.getProductsByIds(allProductIds);
        Map<Long, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        Map<String, Long> categoryCount = new java.util.HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                Product product = productMap.get(item.getProductId());
                if (product != null) {
                    String categoryName = getCategoryName(product.getCategoryId());
                    categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0L) + 1);
                }
            }
        }
        
        return categoryCount;
    }
    
    /**
     * 获取热门商品推荐
     * @return 热门商品的文本描述
     */
    private String getPopularProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            
            if (products == null || products.isEmpty()) {
                return "暂无商品推荐。";
            }
            
            // 取前5个商品作为推荐
            List<Product> recommended = products.stream()
                    .limit(5)
                    .collect(Collectors.toList());
            
            // 构建推荐商品的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("🌟 热门商品推荐\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            for (Product product : recommended) {
                sb.append("📦 ").append(product.getName()).append("\n");
                sb.append("   价格：¥").append(product.getPrice()).append("\n");
                sb.append("   分类：").append(getCategoryName(product.getCategoryId())).append("\n");
                sb.append("   ───────────────────────────\n");
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            return sb.toString();
            
        } catch (Exception e) {
            return "获取热门商品失败：" + e.getMessage();
        }
    }
    
    /**
     * 根据类别获取商品推荐
     * @param categoryName 类别名称
     * @return 该类别商品的文本描述
     */
    private String getProductsByCategory(String categoryName) {
        try {
            List<Product> allProducts = productService.getAllProducts();
            
            // 筛选指定类别的商品
            List<Product> categoryProducts = allProducts.stream()
                    .filter(p -> categoryName.equals(getCategoryName(p.getCategoryId())))
                    .limit(5)
                    .collect(Collectors.toList());
            
            if (categoryProducts.isEmpty()) {
                return getPopularProducts();
            }
            
            // 构建推荐商品的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("🎯 为您推荐 ").append(categoryName).append(" 商品\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            for (Product product : categoryProducts) {
                sb.append("📦 ").append(product.getName()).append("\n");
                sb.append("   价格：¥").append(product.getPrice()).append("\n");
                sb.append("   ───────────────────────────\n");
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            return sb.toString();
            
        } catch (Exception e) {
            return "获取推荐商品失败：" + e.getMessage();
        }
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
        return "商品推荐工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "根据用户历史行为推荐商品";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"推荐", "推荐商品", "有什么好货", "热门商品", "猜你喜欢"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("PRODUCT_RECOMMEND");
    }
}
