package org.example.shopdemo.agent;

import org.example.shopdemo.dto.AIRecommendRequest;
import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.entity.Cart;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.entity.User;
import org.example.shopdemo.service.CartService;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.ProductService;
import org.example.shopdemo.service.UserService;
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

/**
 * 增强版AI服务类
 * 结合数据库数据提供更智能的AI功能
 * 支持AI响应缓存，提高性能
 */
@Service
public class EnhancedAIService {

    @Autowired
    @Qualifier("deepSeekChatModel")
    private ChatModel deepSeekChatModel;

    @Autowired
    @Qualifier("zhiPuAiChatModel")
    private ChatModel zhiPuAiChatModel;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisCacheService redisCacheService;

    private static final String AI_CACHE_PREFIX = "ai:response:";

    /**
     * 生成缓存键
     * @param prefix 缓存前缀
     * @param params 参数列表
     * @return 缓存键
     */
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

    /**
     * MD5哈希
     * @param input 输入字符串
     * @return 哈希值
     */
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

    /**
     * 获取ZhipuAI ChatClient
     */
    private ChatClient getDeepSeekChatClient() {
        return ChatClient.builder(deepSeekChatModel).build();
    }

    /**
     * 获取ZhipuAI ChatClient
     */
    private ChatClient getZhipuAiChatClient() {

        return ChatClient.builder(zhiPuAiChatModel).build();
    }

    /**
     * 基于用户历史的智能客服
     * 结合用户订单、购物车等信息回答问题
     * @param userId 用户ID
     * @param question 用户问题
     * @return AI回答
     */
    public String intelligentCustomerService(Long userId, String question) {
        String cacheKey = generateCacheKey("customer", userId, question);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        User user = userService.getUserInfoById(userId);
        List<Order> orders = orderService.getUserOrders(userId);
        List<CartDTO> cartItems = cartService.getUserCart(userId);

        //context上下文
        StringBuilder context = new StringBuilder();
        context.append("用户信息：\n");
        context.append("- 用户名：").append(user.getNickname() != null ? user.getNickname() : user.getUsername()).append("\n");
        context.append("- 注册时间：").append(user.getCreateTime()).append("\n\n");

        if (!orders.isEmpty()) {
            context.append("最近订单：\n");
            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                Order order = orders.get(i);
//                这些String.format调用主要用于：
//                生成结构化的用户数据展示
//                格式化商品价格和数量信息
//                创建易于AI理解和处理的上下文信息
                context.append(String.format("- 订单号：%s，金额：%.2f，状态：%s，时间：%s\n",
                        order.getOrderNo(),
                        order.getTotalAmount(),
                        getOrderStatusText(order.getStatus()),
                        order.getCreateTime()));
            }
            context.append("\n");
        }

        if (!cartItems.isEmpty()) {
            List<Long> productIds = cartItems.stream()
                    .map(CartDTO::getProductId)
                    .distinct()
                    .toList();
            List<Product> products = productService.getProductsByIds(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

            context.append("购物车商品：\n");
            for (CartDTO item : cartItems) {
                Product product = productMap.get(item.getProductId());
                if (product != null) {
                    context.append(String.format("- %s，数量：%d，单价：%.2f\n",
                            product.getName(), item.getQuantity(), product.getPrice()));
                }
            }
            context.append("\n");
        }

        String systemPrompt = """
            你是一个专业的购物商城智能客服助手，名字叫"小智"。
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

    /**
     * 基于用户行为的个性化商品推荐
     * 结合用户购买历史、购物车等数据
     * @param userId 用户ID
     * @param request 推荐请求
     * @return 推荐结果
     */
    public String personalizedRecommendation(Long userId, AIRecommendRequest request) {
        String cacheKey = generateCacheKey("recommend", userId, 
            request.getCategory(), request.getUserPreferences(), request.getPriceRange());
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        List<Order> orders = orderService.getUserOrders(userId);
        List<CartDTO> cartItems = cartService.getUserCart(userId);

        StringBuilder userContext = new StringBuilder();
        userContext.append("用户偏好：").append(request.getUserPreferences()).append("\n");
        userContext.append("商品分类：").append(request.getCategory()).append("\n");

        if (request.getPriceRange() != null && !request.getPriceRange().isEmpty()) {
            userContext.append("价格范围：").append(request.getPriceRange()).append("\n");
        }

        if (request.getBrandPreferences() != null && !request.getBrandPreferences().isEmpty()) {
            userContext.append("品牌偏好：").append(request.getBrandPreferences()).append("\n");
        }

        if (!orders.isEmpty()) {
            userContext.append("\n购买历史：\n");
            Map<String, Long> categoryCount = new java.util.HashMap<>();
            List<Long> allProductIds = new java.util.ArrayList<>();
            
            for (Order order : orders) {
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                for (OrderItem item : items) {
                    allProductIds.add(item.getProductId());
                }
            }
            
            List<Product> allProducts = productService.getProductsByIds(allProductIds);
            Map<Long, Product> productMap = allProducts.stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
            
            for (Order order : orders) {
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                for (OrderItem item : items) {
                    Product product = productMap.get(item.getProductId());
                    String categoryName = product != null ? getCategoryName(product.getCategoryId()) : "未分类";
                    categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0L) + 1);
                }
            }
            categoryCount.forEach((category, count) -> {
                userContext.append(String.format("- %s：购买%d次\n", category, count));
            });
        }

        if (!cartItems.isEmpty()) {
            List<Long> productIds = cartItems.stream()
                    .map(CartDTO::getProductId)
                    .distinct()
                    .toList();
            List<Product> products = productService.getProductsByIds(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

            userContext.append("\n购物车中的商品：\n");
            for (CartDTO item : cartItems) {
                Product product = productMap.get(item.getProductId());
                if (product != null) {
                    userContext.append(String.format("- %s（%s分类），价格：%.2f\n",
                            product.getName(),
                            getCategoryName(product.getCategoryId()),
                            product.getPrice()));
                }
            }
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

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userContext.toString())
                .call()
                .content();

        redisCacheService.set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }

    /**
     * 基于实际商品数据的智能搜索
     * 结合数据库中的商品信息进行搜索优化
     * @param searchQuery 搜索词
     * @return 搜索建议和商品列表
     */
    public String intelligentSearch(String searchQuery) {
        String cacheKey = generateCacheKey("search", searchQuery);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        List<Product> products = productService.searchProducts(searchQuery);

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

    /**
     * 基于订单数据的智能订单助手
     * 查询订单信息并让AI解释状态
     * @param userId 用户ID
     * @param orderNo 订单号
     * @return 订单信息和状态说明
     */
    public String intelligentOrderAssistant(Long userId, String orderNo) {
        String cacheKey = generateCacheKey("order", userId, orderNo);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        Order order = orderService.getOrderByOrderNo(orderNo);

        if (order == null || !order.getUserId().equals(userId)) {
            return "未找到该订单，请检查订单号是否正确。";
        }

        StringBuilder orderInfo = new StringBuilder();
        orderInfo.append("订单信息：\n");
        orderInfo.append("- 订单号：").append(order.getOrderNo()).append("\n");
        orderInfo.append("- 订单金额：").append(order.getTotalAmount()).append("\n");
        orderInfo.append("- 实付金额：").append(order.getPayAmount()).append("\n");
        orderInfo.append("- 订单状态：").append(getOrderStatusText(order.getStatus())).append("\n");
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
        List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
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

    /**
     * 基于用户数据的智能商品对比
     * 结合用户偏好进行商品对比
     * @param userId 用户ID
     * @param product1Id 商品1 ID
     * @param product2Id 商品2 ID
     * @return 对比结果
     */
    public String intelligentProductComparison(Long userId, Long product1Id, Long product2Id) {
        String cacheKey = generateCacheKey("compare", userId, product1Id, product2Id);
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null) {
            return (String) cachedResult;
        }

        Product product1 = productService.getProductById(product1Id);
        Product product2 = productService.getProductById(product2Id);

        if (product1 == null || product2 == null) {
            return "商品信息不存在，请检查商品ID。";
        }

        List<Order> userOrders = orderService.getUserOrders(userId);
        Map<Long, Integer> purchaseCount = new java.util.HashMap<>();
        for (Order order : userOrders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                purchaseCount.put(item.getProductId(), 
                        purchaseCount.getOrDefault(item.getProductId(), 0) + item.getQuantity());
            }
        }

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
                comparisonInfo.append(String.format("- 之前购买过商品1 %d 次\n", purchaseCount.get(product1Id)));
            }
            if (purchaseCount.containsKey(product2Id)) {
                comparisonInfo.append(String.format("- 之前购买过商品2 %d 次\n", purchaseCount.get(product2Id)));
            }
        }

        String systemPrompt = """
            你是一个智能商品对比助手。
            根据两个商品的详细信息，结合用户的购买历史，提供客观的对比分析和购买建议。
            
            对比要求：
            1. 客观公正地分析两个商品的优缺点
            2. 考虑用户的历史购买偏好
            3. 突出性价比、销量、库存等因素
            4. 给出明确的购买建议
            5. 语言简洁有说服力
            6. 长度控制在250字以内
            """;

        ChatClient chatClient = getDeepSeekChatClient();
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(comparisonInfo.toString())
                .call()
                .content();

        redisCacheService.set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "退款中";
            case 6 -> "已退款";
            default -> "未知";
        };
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "未分类";
        return productService.getCategoryName(categoryId);
    }
}
