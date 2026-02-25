package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.entity.User;
import org.example.shopdemo.service.CartService;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能客服工具
 * 负责处理智能客服相关的请求
 * 可以回答用户的各种问题
 */
@Component
public class CustomerServiceTool implements Tool {
    
    /**
     * 用户服务
     * 用于查询用户信息
     */
    @Autowired
    private UserService userService;
    
    /**
     * 订单服务
     * 用于查询用户订单
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
     * 执行智能客服功能
     * 根据用户问题提供智能回答
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 智能客服的回复
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        try {
            // 获取用户信息
            User user = userService.getUserInfoById(userId);
            
            // 获取用户订单数量
            List<Order> orders = orderService.getUserOrders(userId);
            int orderCount = orders != null ? orders.size() : 0;
            
            // 获取用户购物车商品数量
            List<CartDTO> cartItems = cartService.getUserCart(userId);
            int cartCount = cartItems != null ? cartItems.size() : 0;
            
            // 根据用户问题类型提供不同的回答
            if (message.contains("你好") || message.contains("您好") || message.contains("hi")) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getGreeting(user));
                return Result.success(responseData);
            } else if (message.contains("订单") || message.contains("我的订单")) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getOrderInfo(userId, orders));
                return Result.success(responseData);
            } else if (message.contains("购物车") || message.contains("我的购物车")) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getCartInfo(cartItems));
                return Result.success(responseData);
            } else if (message.contains("账户") || message.contains("我的账户") || message.contains("个人信息")) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getUserInfo(user));
                return Result.success(responseData);
            } else if (message.contains("帮助") || message.contains("能做什么") || message.contains("功能")) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getHelpInfo());
                return Result.success(responseData);
            } else {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", getDefaultResponse(user, orderCount, cartCount));
                return Result.success(responseData);
            }
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("抱歉，处理您的问题时出现了错误：" + e.getMessage());
        }
    }
    
    /**
     * 获取问候语
     * @param user 用户信息
     * @return 问候语
     */
    private String getGreeting(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("👋 您好，").append(user != null ? user.getNickname() : "用户").append("！\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("欢迎使用智能客服系统！\n");
        sb.append("我可以帮您：\n");
        sb.append("  • 查询订单信息\n");
        sb.append("  • 查询购物车\n");
        sb.append("  • 查询账户信息\n");
        sb.append("  • 商品推荐\n");
        sb.append("  • 商品对比\n");
        sb.append("  • 查询物流\n");
        sb.append("  • 查询优惠券\n");
        sb.append("  • 查询积分\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("请问有什么可以帮您的？");
        return sb.toString();
    }
    
    /**
     * 获取订单信息
     * @param userId 用户ID
     * @param orders 订单列表
     * @return 订单信息的文本描述
     */
    private String getOrderInfo(Long userId, List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 您的订单信息\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (orders == null || orders.isEmpty()) {
            sb.append("您还没有任何订单。快去选购商品吧！");
        } else {
            sb.append("订单总数：").append(orders.size()).append("\n");
            
            // 显示最近的3个订单
            int showCount = Math.min(3, orders.size());
            sb.append("\n最近").append(showCount).append("个订单：\n");
            
            for (int i = 0; i < showCount; i++) {
                Order order = orders.get(i);
                sb.append("  ").append(i + 1).append(". ")
                  .append(order.getOrderNo()).append("\n");
                sb.append("     状态：").append(getOrderStatusText(order.getStatus()))
                  .append("，金额：¥").append(order.getTotalAmount()).append("\n");
            }
            
            if (orders.size() > 3) {
                sb.append("\n  ... 还有 ").append(orders.size() - 3).append(" 个订单\n");
            }
        }
        
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return sb.toString();
    }
    
    /**
     * 获取购物车信息
     * @param cartItems 购物车商品列表
     * @return 购物车信息的文本描述
     */
    private String getCartInfo(List<CartDTO> cartItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 您的购物车\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (cartItems == null || cartItems.isEmpty()) {
            sb.append("购物车是空的，快去添加商品吧！");
        } else {
            sb.append("商品数量：").append(cartItems.size()).append("\n");
            
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
            
            for (CartDTO item : cartItems) {
                Product product = item.getProduct();
                if (product != null) {
                    sb.append("\n📦 ").append(product.getName()).append("\n");
                    sb.append("   数量：").append(item.getQuantity()).append("\n");
                    sb.append("   价格：¥").append(product.getPrice()).append("\n");
                    sb.append("   小计：¥").append(product.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()))).append("\n");
                    
                    totalAmount = totalAmount.add(product.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
                }
            }
            
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("总计：¥").append(totalAmount);
        }
        
        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return sb.toString();
    }
    
    /**
     * 获取用户信息
     * @param user 用户信息
     * @return 用户信息的文本描述
     */
    private String getUserInfo(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("👤 您的账户信息\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (user != null) {
            sb.append("昵称：").append(user.getNickname()).append("\n");
            sb.append("用户名：").append(user.getUsername()).append("\n");
            sb.append("邮箱：").append(user.getEmail()).append("\n");
            sb.append("手机号：").append(user.getPhone()).append("\n");
            sb.append("状态：").append(getUserStatusText(user.getStatus())).append("\n");
        } else {
            sb.append("未找到用户信息。");
        }
        
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return sb.toString();
    }
    
    /**
     * 获取帮助信息
     * @return 帮助信息的文本描述
     */
    private String getHelpInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("💡 智能客服帮助\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("我可以帮您：\n\n");
        sb.append("📦 订单相关：\n");
        sb.append("  • 查询订单\n");
        sb.append("  • 查询订单详情\n");
        sb.append("  • 查询物流信息\n\n");
        sb.append("🛒 购物相关：\n");
        sb.append("  • 查询购物车\n");
        sb.append("  • 商品推荐\n");
        sb.append("  • 商品对比\n\n");
        sb.append("💰 优惠相关：\n");
        sb.append("  • 查询优惠券\n");
        sb.append("  • 领取优惠券\n");
        sb.append("  • 查询积分\n\n");
        sb.append("👤 账户相关：\n");
        sb.append("  • 查询个人信息\n");
        sb.append("  • 查询账户状态\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("直接告诉我您需要什么帮助即可！");
        return sb.toString();
    }
    
    /**
     * 获取默认回复
     * @param user 用户信息
     * @param orderCount 订单数量
     * @param cartCount 购物车商品数量
     * @return 默认回复
     */
    private String getDefaultResponse(User user, int orderCount, int cartCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("🤔 我不太确定您的意思\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("不过我可以告诉您：\n");
        sb.append("  • 您有 ").append(orderCount).append(" 个订单\n");
        sb.append("  • 购物车中有 ").append(cartCount).append(" 件商品\n");
        sb.append("\n您可以询问关于订单、购物车、优惠券、积分等问题。");
        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return sb.toString();
    }
    
    /**
     * 获取订单状态文本
     * @param status 订单状态码
     * @return 订单状态文本
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
     * 获取用户状态文本
     * @param status 用户状态码
     * @return 用户状态文本
     */
    private String getUserStatusText(Integer status) {
        switch (status) {
            case 0: return "正常";
            case 1: return "禁用";
            default: return "未知";
        }
    }
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "智能客服工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "回答用户的各种问题";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"你好", "您好", "客服", "帮助", "能做什么", "功能", "我的账户", "个人信息"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("CUSTOMER_SERVICE");
    }
}
