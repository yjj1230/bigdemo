package org.example.shopdemo.agent;

import org.example.shopdemo.agent.tools.*;
import org.example.shopdemo.dto.AIRecommendRequest;
import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.entity.User;
import org.example.shopdemo.utils.RedisCacheService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ShoppingAgent {

    @Autowired
    @Qualifier("deepSeekChatModel")
    private ChatModel deepSeekChatModel;

    @Autowired
    @Qualifier("zhiPuAiChatModel")
    private ChatModel zhiPuAiChatModel;

    @Autowired
    private CustomerServiceTool customerServiceTool;

    @Autowired
    private ProductRecommendationTool productRecommendationTool;

    @Autowired
    private OrderAssistantTool orderAssistantTool;

    @Autowired
    private ProductComparisonTool productComparisonTool;

    @Autowired
    private RedisCacheService redisCacheService;

    private static final String AI_CACHE_PREFIX = "ai:response:";

    private String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(AI_CACHE_PREFIX);
        keyBuilder.append(prefix);
        for (Object param : params) {
            if (param != null) {
                keyBuilder.append(":").append(hash(param.toString()));
            }
        }
        return keyBuilder.toString();
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    private ChatClient getDeepSeekChatClient() {
        return ChatClient.builder(deepSeekChatModel).build();
    }

    private ChatClient getZhipuAiChatClient() {
        return ChatClient.builder(zhiPuAiChatModel).build();
    }

    public String handleCustomerService(Long userId, String question) {
        String cacheKey = generateCacheKey("customer", userId, question);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        User user = customerServiceTool.getUserInfo(userId);
        List<Order> orders = customerServiceTool.getUserOrders(userId);
        List<CartDTO> cartItems = customerServiceTool.getUserCart(userId);

        StringBuilder context = new StringBuilder();
        context.append("用户信息：\n");
        context.append("- 用户名：").append(user.getNickname() != null ? user.getNickname() : user.getUsername()).append("\n");
        context.append("- 注册时间：").append(user.getCreateTime()).append("\n\n");

        if (!orders.isEmpty()) {
            context.append("最近订单：\n");
            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                Order order = orders.get(i);
                context.append(String.format("- 订单号：%s，金额：%.2f，状态：%s，时间：%s\n",
                        order.getOrderNo(),
                        order.getTotalAmount(),
                        customerServiceTool.getOrderStatusText(order.getStatus()),
                        order.getCreateTime()));
            }
            context.append("\n");
        }

        if (!cartItems.isEmpty()) {
            context.append("购物车商品：\n");
            for (CartDTO item : cartItems) {
                Product product = productRecommendationTool.getProductById(item.getProductId());
                if (product != null) {
                    context.append(String.format("- %s，数量：%d，单价：%.2f\n",
                            product.getName(), item.getQuantity(), product.getPrice()));
                }
            }
            context.append("\n");
        }

        String systemPrompt = """
            你是一个专业的购物商城智能客服助手，名字叫\"小智\"。
            你可以根据用户的订单历史、购物车等信息，提供更个性化的服务。
            
            请遵循以下规则：
            1. 回答要简洁明了，友好热情
            2. 如果用户询问订单信息，结合提供的订单数据回答
            3. 如果用户询问购物车，结合购物车数据回答
            4. 如果问题涉及具体操作，引导用户到相应页面
            5. 回答时使用emoji表情增加亲和力
            6. 回答长度控制在150字以内
            7. 如果问题无法回答，建议用户联系人工客服
            """;

        String userMessage = context + "用户问题：" + question;

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        redisCacheService.set(cacheKey, result, 10, TimeUnit.MINUTES);
        return result;
    }

    public String handleProductRecommendation(Long userId, AIRecommendRequest request) {
        String cacheKey = generateCacheKey("recommend", userId, 
            request.getCategory(), request.getUserPreferences(), request.getPriceRange());
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        Map<String, Object> userContext = new java.util.HashMap<>();
        userContext.put("用户偏好", request.getUserPreferences());
        userContext.put("商品分类", request.getCategory());

        if (request.getPriceRange() != null && !request.getPriceRange().isEmpty()) {
            userContext.put("价格范围", request.getPriceRange());
        }

        if (request.getBrandPreferences() != null && !request.getBrandPreferences().isEmpty()) {
            userContext.put("品牌偏好", request.getBrandPreferences());
        }

        Map<String, Long> purchaseHistory = productRecommendationTool.getUserPurchaseHistory(userId);
        if (!purchaseHistory.isEmpty()) {
            userContext.put("购买历史", purchaseHistory);
        }

        List<CartDTO> cartItems = productRecommendationTool.getUserCart(userId);
        if (!cartItems.isEmpty()) {
            userContext.put("购物车商品", cartItems);
        }

        String systemPrompt = """
            你是一个专业的电商个性化推荐专家。
            根据用户的购买历史、购物车内容和偏好信息，提供精准的个性化推荐。
            
            推荐要求：
            1. 推荐要精准匹配用户需求和购买习惯
            2. 考虑用户的历史购买偏好
            3. 避免推荐购物车中已有的商品
            4. 突出推荐商品的独特优势
            5. 语言简洁有吸引力
            6. 长度控制在200字以内
            """;

        String userMessage = "用户信息：\n" + formatMap(userContext) + "\n请提供个性化推荐";

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
        redisCacheService.set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }


    public String handleProductSearch(String searchQuery) {
        String cacheKey = generateCacheKey("search", searchQuery);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        List<Product> products = productRecommendationTool.searchProducts(searchQuery);

        StringBuilder productInfo = new StringBuilder();
        if (!products.isEmpty()) {
            productInfo.append("找到以下相关商品：\n");
            for (int i = 0; i < Math.min(5, products.size()); i++) {
                Product p = products.get(i);
                productInfo.append(String.format("%d. %s\n", i + 1, p.getName()));
                productInfo.append(String.format("   价格：%.2f，销量：%d，库存：%d\n",
                        p.getPrice(), p.getSales(), p.getStock()));
                productInfo.append(String.format("   描述：%s\n\n", p.getDescription()));
            }
        } else {
            productInfo.append("未找到完全匹配的商品，但可以尝试以下搜索建议：\n");
        }

        String systemPrompt = """
            你是一个智能电商搜索助手。
            根据用户的搜索词和找到的商品信息，提供搜索建议和推荐。
            
            要求：
            1. 如果找到商品，突出最相关的商品
            2. 提供搜索优化建议
            3. 语言简洁明了
            4. 长度控制在200字以内
            """;

        String userMessage = "搜索词：" + searchQuery + "\n\n" + productInfo.toString();

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        redisCacheService.set(cacheKey, result, 15, TimeUnit.MINUTES);
        return result;
    }

    public String handleOrderAssistant(Long userId, String orderNo) {
        String cacheKey = generateCacheKey("order", userId, orderNo);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        Order order = orderAssistantTool.getOrderByOrderNo(orderNo);

        if (order == null || !order.getUserId().equals(userId)) {
            return "未找到该订单，请检查订单号是否正确。";
        }

        StringBuilder orderInfo = new StringBuilder();
        orderInfo.append("订单信息：\n");
        orderInfo.append("- 订单号：").append(order.getOrderNo()).append("\n");
        orderInfo.append("- 订单金额：").append(order.getTotalAmount()).append("\n");
        orderInfo.append("- 实付金额：").append(order.getPayAmount()).append("\n");
        orderInfo.append("- 订单状态：").append(orderAssistantTool.getOrderStatusText(order.getStatus())).append("\n");
        orderInfo.append("- 收货人：").append(order.getReceiverName()).append("\n");
        orderInfo.append("- 收货地址：").append(order.getReceiverAddress()).append("\n");
        orderInfo.append("- 下单时间：").append(order.getCreateTime()).append("\n");

        if (order.getPayTime() != null) {
            orderInfo.append("- 支付时间：").append(order.getPayTime()).append("\n");
        }
        if (order.getShipTime() != null) {
            orderInfo.append("- 发货时间：").append(order.getShipTime()).append("\n");
        }
        if (order.getFinishTime() != null) {
            orderInfo.append("- 完成时间：").append(order.getFinishTime()).append("\n");
        }

        orderInfo.append("\n订单商品：\n");
        List<OrderItem> orderItems = orderAssistantTool.getOrderItems(order.getId());
        for (OrderItem item : orderItems) {
            orderInfo.append(String.format("- %s x %d，单价：%.2f，小计：%.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()))));
        }

        String systemPrompt = """
            你是一个智能订单助手。
            根据订单详细信息，向用户解释订单状态，并提供下一步操作建议。
            
            要求：
            1. 用通俗易懂的语言解释订单状态
            2. 根据订单状态提供明确的下一步操作建议
            3. 语气友好，让用户安心
            4. 如果订单有异常情况，给出解决方案
            5. 长度控制在200字以内
            """;

        String userMessage = orderInfo.toString() + "\n请解释订单状态并提供建议";

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        redisCacheService.set(cacheKey, result, 20, TimeUnit.MINUTES);
        return result;
    }

    public String handleProductComparison(Long userId, Long product1Id, Long product2Id) {
        String cacheKey = generateCacheKey("compare", userId, product1Id, product2Id);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        Product product1 = productComparisonTool.getProductById(product1Id);
        Product product2 = productComparisonTool.getProductById(product2Id);

        if (product1 == null || product2 == null) {
            return "商品信息不存在，请检查商品ID。";
        }

        Map<Long, Integer> purchaseCount = productComparisonTool.getUserPurchaseCount(userId);

        StringBuilder comparisonInfo = new StringBuilder();
        comparisonInfo.append("商品1：\n");
        comparisonInfo.append("- 名称：").append(product1.getName()).append("\n");
        comparisonInfo.append("- 价格：").append(product1.getPrice()).append("\n");
        comparisonInfo.append("- 销量：").append(product1.getSales()).append("\n");
        comparisonInfo.append("- 库存：").append(product1.getStock()).append("\n");
        comparisonInfo.append("- 描述：").append(product1.getDescription()).append("\n\n");

        comparisonInfo.append("商品2：\n");
        comparisonInfo.append("- 名称：").append(product2.getName()).append("\n");
        comparisonInfo.append("- 价格：").append(product2.getPrice()).append("\n");
        comparisonInfo.append("- 销量：").append(product2.getSales()).append("\n");
        comparisonInfo.append("- 库存：").append(product2.getStock()).append("\n");
        comparisonInfo.append("- 描述：").append(product2.getDescription()).append("\n\n");

        if (purchaseCount.containsKey(product1Id) || purchaseCount.containsKey(product2Id)) {
            comparisonInfo.append("用户购买历史：\n");
            if (purchaseCount.containsKey(product1Id)) {
                comparisonInfo.append(String.format("- %s：购买%d次\n", 
                        product1.getName(), purchaseCount.get(product1Id)));
            }
            if (purchaseCount.containsKey(product2Id)) {
                comparisonInfo.append(String.format("- %s：购买%d次\n", 
                        product2.getName(), purchaseCount.get(product2Id)));
            }
            comparisonInfo.append("\n");
        }

        String systemPrompt = """
            你是一个智能商品对比助手。
            根据两个商品的详细信息，提供客观的对比分析和购买建议。
            
            要求：
            1. 从价格、销量、库存等多个维度进行对比
            2. 结合用户的购买历史给出建议
            3. 语言简洁专业
            4. 长度控制在200字以内
            """;

        String userMessage = comparisonInfo.toString() + "\n请提供对比分析和购买建议";

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        redisCacheService.set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }

    private String formatMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("- ").append(entry.getKey()).append("：");
            if (entry.getValue() instanceof Map) {
                sb.append("\n");
                Map<?, ?> subMap = (Map<?, ?>) entry.getValue();
                for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
                    sb.append("  - ").append(subEntry.getKey())
                      .append("：").append(subEntry.getValue()).append("\n");
                }
            } else {
                sb.append(entry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}