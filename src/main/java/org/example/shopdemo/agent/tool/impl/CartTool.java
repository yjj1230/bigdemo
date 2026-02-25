package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车工具
 * 负责处理购物车相关的查询请求
 * 可以查询用户的购物车列表
 */
@Component
public class CartTool implements Tool {
    
    /**
     * 购物车服务
     * 用于查询购物车信息
     */
    @Autowired
    private CartService cartService;
    
    /**
     * 执行购物车查询功能
     * 查询用户的购物车列表
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        return getCartList(userId);
    }
    
    /**
     * 查询购物车列表
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result<Map<String, Object>> getCartList(Long userId) {
        try {
            // 查询用户的购物车列表
            List<CartDTO> cartItems = cartService.getUserCart(userId);
            
            // 检查购物车是否为空
            if (cartItems == null || cartItems.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "🛒 您的购物车是空的");
                responseData.put("items", new java.util.ArrayList<>());
                responseData.put("total", 0);
                return Result.success(responseData);
            }
            
            // 计算总价
            BigDecimal total = cartItems.stream()
                    .filter(item -> item.getProduct() != null)
                    .map(item -> {
                        BigDecimal price = item.getProduct().getPrice();
                        BigDecimal quantity = new BigDecimal(item.getQuantity());
                        return price.multiply(quantity);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 构建购物车列表的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("🛒 您的购物车\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            for (CartDTO item : cartItems) {
                Product product = item.getProduct();
                if (product != null) {
                    sb.append("商品：").append(product.getName()).append("\n");
                    sb.append("价格：¥").append(product.getPrice()).append("\n");
                    sb.append("数量：").append(item.getQuantity()).append("\n");
                    BigDecimal price = product.getPrice();
                    BigDecimal quantity = new BigDecimal(item.getQuantity());
                    sb.append("小计：¥").append(price.multiply(quantity)).append("\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                }
            }
            
            sb.append("总计：¥").append(total).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            responseData.put("items", cartItems);
            responseData.put("total", total);
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            return Result.error("查询购物车时出现错误：" + e.getMessage());
        }
    }
    
    @Override
    public String getToolName() {
        return "购物车工具";
    }
    
    @Override
    public String getDescription() {
        return "查询用户的购物车列表、购物车详情";
    }
    
    @Override
    public String[] getKeywords() {
        return new String[]{"购物车", "我的购物车", "查看购物车", "购物车查询"};
    }
    
    @Override
    public boolean canHandle(String message) {
        return message.equals("CART_QUERY");
    }
}