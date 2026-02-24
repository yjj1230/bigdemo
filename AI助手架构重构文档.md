# AI助手架构重构文档

## 架构概述

将原有的单体AI服务重构为基于Tools的Agent架构，提高代码的可维护性、可扩展性和复用性。

## 架构设计

### 1. 核心组件

```
┌─────────────────────────────────────────────────────────┐
│                    EnhancedAIService                      │
│                    (对外接口层)                            │
│  - intelligentCustomerService()                           │
│  - personalizedRecommendation()                           │
│  - intelligentSearch()                                    │
│  - intelligentOrderAssistant()                            │
│  - intelligentProductComparison()                         │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                      ShoppingAgent                        │
│                   (Agent协调层)                            │
│  - handleCustomerService()                               │
│  - handleProductRecommendation()                          │
│  - handleProductSearch()                                  │
│  - handleOrderAssistant()                                 │
│  - handleProductComparison()                              │
│  - AI模型管理                                             │
│  - 缓存管理                                               │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│CustomerService│  │ProductRecomm-│  │OrderAssistant│
│    Tool      │  │  endation    │  │    Tool      │
│              │  │    Tool      │  │              │
│- getUserInfo │  │- searchProds │  │- getOrderByNo│
│- getUserOrders│  │- getProduct  │  │- getOrderById│
│- getUserCart  │  │- getUserHistory││- getOrderItems│
│- getStatusText│  │- getUserCart │  │- getStatusText│
└──────────────┘  └──────────────┘  └──────────────┘
                                              │
                                              ▼
                                    ┌──────────────┐
                                    │ProductCompar-│
                                    │   isonTool   │
                                    │              │
                                    │- getProduct  │
                                    │- getPurchase │
                                    │  Count       │
                                    └──────────────┘
```

### 2. Tools层设计

每个Tool负责特定的数据访问和业务逻辑：

#### CustomerServiceTool
- **职责**：提供用户相关的数据访问
- **方法**：
  - `getUserInfo(userId)` - 获取用户信息
  - `getUserOrders(userId)` - 获取用户订单
  - `getUserCart(userId)` - 获取用户购物车
  - `getOrderStatusText(status)` - 获取订单状态文本

#### ProductRecommendationTool
- **职责**：提供商品推荐相关的数据访问
- **方法**：
  - `searchProducts(keyword)` - 搜索商品
  - `getProductById(productId)` - 获取商品详情
  - `getProductsByIds(productIds)` - 批量获取商品
  - `getUserOrders(userId)` - 获取用户订单
  - `getUserCart(userId)` - 获取用户购物车
  - `getUserPurchaseHistory(userId)` - 获取用户购买历史
  - `getCategoryName(categoryId)` - 获取分类名称

#### OrderAssistantTool
- **职责**：提供订单相关的数据访问
- **方法**：
  - `getOrderByOrderNo(orderNo)` - 根据订单号查询
  - `getOrderById(orderId)` - 根据ID查询
  - `getOrderItems(orderId)` - 获取订单商品
  - `getOrderStatusText(status)` - 获取订单状态文本

#### ProductComparisonTool
- **职责**：提供商品对比相关的数据访问
- **方法**：
  - `getProductById(productId)` - 获取商品详情
  - `getUserPurchaseCount(userId)` - 获取用户购买统计
  - `getCategoryName(categoryId)` - 获取分类名称

### 3. Agent层设计

ShoppingAgent负责协调各个Tools，处理AI交互和缓存：

```java
@Service
public class ShoppingAgent {
    
    // 注入各个Tools
    @Autowired
    private CustomerServiceTool customerServiceTool;
    
    @Autowired
    private ProductRecommendationTool productRecommendationTool;
    
    @Autowired
    private OrderAssistantTool orderAssistantTool;
    
    @Autowired
    private ProductComparisonTool productComparisonTool;
    
    // AI模型
    @Autowired
    private ChatModel deepSeekChatModel;
    @Autowired
    private ChatModel zhiPuAiChatModel;
    
    // 缓存服务
    @Autowired
    private RedisCacheService redisCacheService;
    
    // 处理各种AI请求的方法
    public String handleCustomerService(Long userId, String question);
    public String handleProductRecommendation(Long userId, AIRecommendRequest request);
    public String handleProductSearch(String searchQuery);
    public String handleOrderAssistant(Long userId, String orderNo);
    public String handleProductComparison(Long userId, Long product1Id, Long product2Id);
}
```

## 优势分析

### 1. 关注点分离
- **Tools层**：专注于数据访问和业务逻辑
- **Agent层**：专注于AI交互和流程协调
- **Service层**：专注于对外接口

### 2. 可维护性提升
- 每个Tool职责单一，易于理解和修改
- 新增功能只需添加新的Tool或扩展现有Tool
- 减少了代码重复

### 3. 可扩展性增强
- 新增AI功能：创建新的Tool方法 + 在Agent中添加处理方法
- 新增数据源：在对应的Tool中添加方法
- 新增AI模型：在Agent中添加新的ChatClient

### 4. 可测试性改善
- 每个Tool可以独立测试
- Agent层可以Mock Tools进行测试
- Service层可以Mock Agent进行测试

### 5. 代码复用
- Tools可以在不同的Agent中复用
- 通用的数据处理逻辑集中管理
- 避免重复实现相同的功能

## 使用示例

### 示例1：智能客服
```java
// Controller层
@Autowired
private EnhancedAIService enhancedAIService;

@GetMapping("/customer-service")
public Result<String> customerService(@RequestParam Long userId, 
                                       @RequestParam String question) {
    String response = enhancedAIService.intelligentCustomerService(userId, question);
    return Result.success(response);
}
```

### 示例2：商品推荐
```java
// Controller层
@PostMapping("/recommendation")
public Result<String> recommendation(@RequestParam Long userId,
                                     @RequestBody AIRecommendRequest request) {
    String response = enhancedAIService.personalizedRecommendation(userId, request);
    return Result.success(response);
}
```

## 扩展指南

### 添加新的AI功能

1. **在对应的Tool中添加数据访问方法**
```java
// ProductRecommendationTool.java
public List<Product> getHotProducts(int limit) {
    return productService.getHotProducts(limit);
}
```

2. **在ShoppingAgent中添加处理方法**
```java
public String handleHotProductsRecommendation(int limit) {
    List<Product> products = productRecommendationTool.getHotProducts(limit);
    
    String systemPrompt = "你是商品推荐专家...";
    String userMessage = "热门商品：" + formatProducts(products);
    
    ChatClient chatClient = getDeepSeekChatClient();
    return chatClient.prompt()
            .system(systemPrompt)
            .user(userMessage)
            .call()
            .content();
}
```

3. **在EnhancedAIService中添加对外接口**
```java
public String hotProductsRecommendation(int limit) {
    return shoppingAgent.handleHotProductsRecommendation(limit);
}
```

### 添加新的Tool

1. **创建新的Tool类**
```java
@Component
public class NewFeatureTool {
    
    @Autowired
    private SomeService someService;
    
    public SomeData getData(Long userId) {
        return someService.getData(userId);
    }
}
```

2. **在ShoppingAgent中注入并使用**
```java
@Autowired
private NewFeatureTool newFeatureTool;

public String handleNewFeature(Long userId) {
    SomeData data = newFeatureTool.getData(userId);
    // 处理数据并调用AI
}
```

## 性能优化

### 1. 缓存策略
- AI响应缓存：10-30分钟
- 缓存键生成：MD5哈希
- 缓存失效：时间过期

### 2. 数据库优化
- 批量查询：减少数据库访问次数
- 索引优化：确保查询效率
- 连接池：HikariCP

### 3. AI调用优化
- 并行调用：多个AI请求可以并行处理
- 超时控制：避免长时间等待
- 降级策略：AI服务不可用时返回默认响应

## 总结

通过将AI助手重构为基于Tools的Agent架构，我们实现了：

✅ **清晰的分层架构**：Service → Agent → Tools
✅ **高内聚低耦合**：每个组件职责明确
✅ **易于扩展**：新增功能只需添加Tool或扩展Agent
✅ **便于测试**：各层可以独立测试
✅ **代码复用**：Tools可以在多个Agent中复用
✅ **性能优化**：统一的缓存管理和AI调用策略

这种架构设计使得AI助手系统更加灵活、可维护和可扩展，为未来的功能扩展奠定了良好的基础。