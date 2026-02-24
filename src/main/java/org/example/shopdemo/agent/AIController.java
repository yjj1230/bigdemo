package org.example.shopdemo.agent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.AIRecommendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI服务", description = "AI智能服务相关接口")
public class AIController {

    @Autowired
    private EnhancedAIService enhancedAIService;

    @PostMapping("/customer-service")
    @Operation(summary = "智能客服", description = "基于用户历史的智能客服")
    public Result<String> intelligentCustomerService(@RequestParam Long userId, @RequestParam String question) {
        String response = enhancedAIService.intelligentCustomerService(userId, question);
        System.out.println("AIResponse: " + response);
        return Result.success("操作成功" , response);
    }

    @PostMapping("/recommendation")
    @Operation(summary = "个性化推荐", description = "基于用户行为的个性化商品推荐")
    public Result<String> personalizedRecommendation(@RequestParam Long userId, @Valid @RequestBody AIRecommendRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMsg.append(error.getDefaultMessage()).append("; ");
            }
            return Result.error(errorMsg.toString());
        }
        String recommendation = enhancedAIService.personalizedRecommendation(userId, request);
        return Result.success("操作成功" , recommendation);
    }

    @PostMapping("/search")
    @Operation(summary = "智能搜索", description = "基于实际商品数据的智能搜索")
    public Result<String> intelligentSearch(@RequestParam String searchQuery) {
        String result = enhancedAIService.intelligentSearch(searchQuery);
        System.out.println("AIResponse: " + result);
        return Result.success("操作成功" , result);
    }

    @PostMapping("/order-assistant")
    @Operation(summary = "订单助手", description = "基于订单数据的智能订单助手")
    public Result<String> intelligentOrderAssistant(@RequestParam Long userId, @RequestParam String orderNo) {
        String result = enhancedAIService.intelligentOrderAssistant(userId, orderNo);
        return Result.success("操作成功" , result);
    }

    @PostMapping("/product-comparison")
    @Operation(summary = "商品对比", description = "基于用户数据的智能商品对比")
    public Result<String> intelligentProductComparison(
            @RequestParam Long userId,
            @RequestParam Long product1Id,
            @RequestParam Long product2Id) {
        String result = enhancedAIService.intelligentProductComparison(userId, product1Id, product2Id);
        return Result.success("操作成功" , result);
    }
}