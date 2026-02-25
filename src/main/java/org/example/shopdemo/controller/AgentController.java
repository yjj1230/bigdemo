package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.agent.service.ConversationHistoryService;
import org.example.shopdemo.agent.service.IntelligentAgent;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.utils.jwtutil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能Agent控制器
 * 提供统一的对话接口，处理用户的各种自然语言请求
 * 通过意图识别和工具路由，自动调用相应的服务
 */
@RestController
@RequestMapping("/api/agent")
@Tag(name = "智能Agent", description = "智能对话接口，支持自然语言交互")
public class AgentController {
    
    /**
     * 智能Agent服务
     * 负责处理用户消息、识别意图、路由到合适的工具
     */
    @Autowired
    private IntelligentAgent intelligentAgent;
    
    /**
     * 对话历史管理服务
     * 用于存储和管理用户的对话历史
     */
    @Autowired
    private ConversationHistoryService historyService;
    
    /**
     * 处理用户消息
     * 这是Agent的主要入口，接收用户的自然语言输入并返回响应
     * @param request 包含用户消息的请求体
     * @param token JWT token，用于获取用户ID
     * @return Agent的响应
     */
    @PostMapping("/chat")
    @Operation(summary = "智能对话", description = "发送消息给智能Agent，Agent会自动识别意图并返回相应结果")
    public Result<String> chat(
            @RequestBody Map<String, String> request,
            @Parameter(description = "JWT token") @RequestHeader("Authorization") String token) {
        try {
            // 从请求中获取用户消息
            String message = request.get("message");
            
            // 检查消息是否为空
            if (message == null || message.trim().isEmpty()) {
                return Result.error(400, "消息不能为空");
            }
            
            // 从token中提取纯token（去除Bearer前缀）
            String actualToken = jwtutil.extractToken(token);
            if (actualToken == null) {
                return Result.error(401, "token格式错误");
            }
            
            // 从token中提取用户ID
            Long userId = jwtutil.getUserIdFromToken(actualToken);
            
            // 调用智能Agent处理用户消息
            String response = intelligentAgent.processUserMessage(message, userId);

            System.out.println("Agent response: " + response);
            // 返回Agent的响应
            return Result.success("操作成功" ,response);
            
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return Result.error(500, "处理消息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取Agent支持的功能列表
     * 返回Agent支持的所有工具和功能描述
     * @return 支持的功能列表
     */
    @GetMapping("/capabilities")
    @Operation(summary = "获取Agent能力", description = "获取Agent支持的所有功能和工具列表")
    public Result<Map<String, Object>> getCapabilities() {
        try {
            Map<String, Object> capabilities = new HashMap<>();
            
            // 添加支持的意图列表
            capabilities.put("intents", new String[]{
                "ORDER_QUERY",      // 查询订单
                "LOGISTICS_QUERY",   // 查询物流
                "COUPON_RECEIVE",   // 领取优惠券
                "COUPON_LIST",      // 查看优惠券
                "POINTS_QUERY",      // 查询积分
                "POINTS_USE",       // 使用积分
                "PRODUCT_SEARCH",    // 搜索商品
                "PRODUCT_RECOMMEND", // 商品推荐
                "PRODUCT_COMPARE",  // 商品对比
                "CART_QUERY",       // 查询购物车
                "REFUND_APPLY",     // 申请退款
                "REFUND_QUERY",     // 查询退款
                "NOTIFICATION_QUERY",// 查询消息
                "FAVORITE_QUERY",   // 查询收藏
                "GREETING",         // 问候
                "CUSTOMER_SERVICE"   // 智能客服
            });
            
            // 添加支持的示例问题
            capabilities.put("examples", new String[]{
                "帮我查一下订单",
                "查询订单123的物流信息",
                "领个优惠券",
                "我的积分有多少",
                "我的优惠券",
                "你好"
            });
            
            // 添加工具列表
            capabilities.put("tools", intelligentAgent.getAllTools());
            
            return Result.success(capabilities);
            
        } catch (Exception e) {
            return Result.error(500, "获取能力列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * 用于检查Agent服务是否正常运行
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查Agent服务是否正常运行")
    public Result<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "ok");
        health.put("message", "Agent服务运行正常");
        return Result.success(health);
    }
    
    /**
     * 获取对话历史
     * 获取用户的对话历史记录
     * @param token JWT token，用于获取用户ID
     * @return 对话历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取对话历史", description = "获取用户的对话历史记录")
    public Result<Object> getHistory(
            @Parameter(description = "JWT token") @RequestHeader("Authorization") String token) {
        try {
            // 从token中提取纯token（去除Bearer前缀）
            String actualToken = jwtutil.extractToken(token);
            if (actualToken == null) {
                return Result.error(401, "token格式错误");
            }
            
            // 从token中提取用户ID
            Long userId = jwtutil.getUserIdFromToken(actualToken);
            
            // 获取对话历史
            return Result.success(historyService.getAllMessages(userId));
            
        } catch (Exception e) {
            return Result.error(500, "获取对话历史失败：" + e.getMessage());
        }
    }
    
    /**
     * 清空对话历史
     * 清空用户的对话历史记录
     * @param token JWT token，用于获取用户ID
     * @return 操作结果
     */
    @DeleteMapping("/history")
    @Operation(summary = "清空对话历史", description = "清空用户的对话历史记录")
    public Result<String> clearHistory(
            @Parameter(description = "JWT token") @RequestHeader("Authorization") String token) {
        try {
            // 从token中提取纯token（去除Bearer前缀）
            String actualToken = jwtutil.extractToken(token);
            if (actualToken == null) {
                return Result.error(401, "token格式错误");
            }
            
            // 从token中提取用户ID
            Long userId = jwtutil.getUserIdFromToken(actualToken);
            
            // 清空对话历史
            historyService.clearHistory(userId);
            
            return Result.success("对话历史已清空");
            
        } catch (Exception e) {
            return Result.error(500, "清空对话历史失败：" + e.getMessage());
        }
    }
}
