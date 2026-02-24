package org.example.shopdemo.agent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.AIRecommendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 增强版AI功能控制器
 * 提供结合数据库数据的AI功能API接口
 */
@RestController
@RequestMapping("/ai/enhanced")
@Tag(name = "增强AI功能", description = "结合数据库数据的智能AI功能接口")
public class EnhancedAIController {

    @Autowired
    private EnhancedAIService enhancedAIService;

    /**
     * 基于用户历史的智能客服
     * @param userId 用户ID（从JWT token中获取）
     * @param question 用户问题
     * @return AI回答
     */
    @PostMapping("/customer-service")
    @Operation(summary = "智能客服（增强版）", description = "结合用户订单、购物车等历史数据回答问题")
    public Result<String> intelligentCustomerService(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "用户问题", example = "我的订单什么时候能到？")
            @RequestParam String question) {
        Long userId = getUserIdFromToken(token);
        String answer = enhancedAIService.intelligentCustomerService(userId, question);
        return Result.success(answer);
    }

    /**
     * 基于用户行为的个性化商品推荐
     * @param userId 用户ID（从JWT token中获取）
     * @param request 推荐请求
     * @return 推荐结果
     */
    @PostMapping("/personalized-recommend")
    @Operation(summary = "个性化商品推荐", description = "基于用户购买历史、购物车等数据进行个性化推荐")
    public Result<String> personalizedRecommendation(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Valid @RequestBody AIRecommendRequest request) {
        Long userId = getUserIdFromToken(token);
        String recommendation = enhancedAIService.personalizedRecommendation(userId, request);
        return Result.success(recommendation);
    }

    /**
     * 基于实际商品数据的智能搜索
     * @param searchQuery 搜索词
     * @return 搜索建议和商品列表
     */
    @PostMapping("/intelligent-search")
    @Operation(summary = "智能搜索（增强版）", description = "结合数据库商品信息进行智能搜索")
    public Result<String> intelligentSearch(
            @Parameter(description = "搜索词", example = "便宜的手机")
            @RequestParam String searchQuery) {
        String result = enhancedAIService.intelligentSearch(searchQuery);
        return Result.success(result);
    }

    /**
     * 基于订单数据的智能订单助手
     * @param userId 用户ID（从JWT token中获取）
     * @param orderNo 订单号
     * @return 订单信息和状态说明
     */
    @PostMapping("/order-assistant")
    @Operation(summary = "智能订单助手", description = "查询订单信息并让AI解释状态")
    public Result<String> intelligentOrderAssistant(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "订单号", example = "ORD202402220001")
            @RequestParam String orderNo) {
        Long userId = getUserIdFromToken(token);
        String result = enhancedAIService.intelligentOrderAssistant(userId, orderNo);
        return Result.success(result);
    }

    /**
     * 基于用户数据的智能商品对比
     * @param userId 用户ID（从JWT token中获取）
     * @param product1Id 商品1 ID
     * @param product2Id 商品2 ID
     * @return 对比结果
     */
    @PostMapping("/product-comparison")
    @Operation(summary = "智能商品对比", description = "结合用户购买历史进行商品对比")
    public Result<String> intelligentProductComparison(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "商品1 ID", example = "1")
            @RequestParam Long product1Id,
            @Parameter(description = "商品2 ID", example = "2")
            @RequestParam Long product2Id) {
        Long userId = getUserIdFromToken(token);
        String comparison = enhancedAIService.intelligentProductComparison(userId, product1Id, product2Id);
        return Result.success(comparison);
    }

    /**
     * 从JWT token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        return org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
    }
}
