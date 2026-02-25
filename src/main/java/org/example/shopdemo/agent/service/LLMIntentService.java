package org.example.shopdemo.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.shopdemo.agent.model.Intent;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于LLM的意图识别服务
 * 使用大语言模型进行更准确的意图识别和参数提取
 * 相比关键词匹配，能更好地理解自然语言
 */
@Service
public class LLMIntentService {
    
    @Autowired
    private DeepSeekChatModel deepSeekChatModel;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 意图类型定义
     */
    private static final String[] INTENT_TYPES = {
        "ORDER_QUERY",           // 查询订单
        "LOGISTICS_QUERY",       // 查询物流
        "COUPON_RECEIVE",       // 领取优惠券
        "COUPON_LIST",          // 查看优惠券列表
        "POINTS_QUERY",          // 查询积分
        "POINTS_USE",            // 使用积分
        "PRODUCT_SEARCH",        // 搜索商品
        "PRODUCT_RECOMMEND",    // 商品推荐
        "PRODUCT_COMPARE",       // 商品对比
        "CART_QUERY",           // 查询购物车
        "REFUND_APPLY",        // 申请退款
        "REFUND_QUERY",         // 查询退款
        "NOTIFICATION_QUERY",    // 查询消息
        "FAVORITE_QUERY",      // 查询收藏
        "GREETING",            // 问候
        "CUSTOMER_SERVICE",     // 客服
        "UNKNOWN"              // 未知意图
    };
    
    /**
     * 使用LLM识别意图
     * @param message 用户消息
     * @return 识别的意图
     */
    public Intent detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new Intent("UNKNOWN", "未知意图", new HashMap<>(), 0.0);
        }
        
        try {
            // 构建系统提示
            String systemPrompt = buildSystemPrompt();
            
            // 构建用户消息
            String userPrompt = String.format(
                "请分析以下用户消息，识别用户的意图并提取相关参数：\n\n用户消息：%s\n\n" +
                "请以JSON格式返回，包含以下字段：\n" +
                "- intent: 意图类型（必须是：%s之一）\n" +
                "- confidence: 置信度（0-1之间的浮点数）\n" +
                "- params: 提取的参数对象（如orderNo、keyword等，如果没有则为空对象{}）\n\n" +
                "只返回JSON，不要其他内容。",
                message,
                String.join("、", INTENT_TYPES)
            );
            
            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.add(new UserMessage(userPrompt));
            
            // 调用LLM
            Prompt prompt = new Prompt(messages);
            ChatResponse response = deepSeekChatModel.call(prompt);
            String result = response.getResult().getOutput().getText();
            
            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(result, Map.class);
            
            String intentType = (String) responseMap.get("intent");
            Double confidence = ((Number) responseMap.get("confidence")).doubleValue();
            Map<String, Object> params = (Map<String, Object>) responseMap.get("params");
            
            // 验证意图类型
            if (!isValidIntent(intentType)) {
                intentType = "UNKNOWN";
            }
            
            return new Intent(intentType, getIntentDescription(intentType), params, confidence);
            
        } catch (Exception e) {
            System.err.println("LLM意图识别失败，回退到关键词匹配：" + e.getMessage());
            // 回退到关键词匹配
            return fallbackToKeywordMatching(message);
        }
    }
    
    /**
     * 构建系统提示
     * @return 系统提示字符串
     */
    private String buildSystemPrompt() {
        return "你是一个智能客服助手的意图识别模块。你的任务是：\n" +
            "1. 分析用户消息，识别用户的真实意图\n" +
            "2. 提取相关的参数（如订单号、商品名称等）\n" +
            "3. 判断意图的置信度\n\n" +
            "支持的意图类型：\n" +
            "- ORDER_QUERY: 查询订单信息、订单状态、订单详情\n" +
            "- LOGISTICS_QUERY: 查询物流信息、快递状态、配送进度\n" +
            "- COUPON_RECEIVE: 领取优惠券、获取优惠\n" +
            "- COUPON_LIST: 查看优惠券列表、我的优惠券\n" +
            "- POINTS_QUERY: 查询积分余额、我的积分\n" +
            "- POINTS_USE: 使用积分、积分抵扣\n" +
            "- PRODUCT_SEARCH: 搜索商品、找商品\n" +
            "- PRODUCT_RECOMMEND: 商品推荐、热门商品\n" +
            "- PRODUCT_COMPARE: 商品对比、比较商品\n" +
            "- CART_QUERY: 查询购物车、我的购物车\n" +
            "- REFUND_APPLY: 申请退款、退货、售后\n" +
            "- REFUND_QUERY: 查询退款进度、退款状态\n" +
            "- NOTIFICATION_QUERY: 查询消息、通知\n" +
            "- FAVORITE_QUERY: 查询收藏、我的收藏\n" +
            "- GREETING: 问候、打招呼\n" +
            "- CUSTOMER_SERVICE: 客服、帮助、能做什么\n\n" +
            "参数说明：\n" +
            "- orderNo: 订单号（如ORD20260224000743155）\n" +
            "- keyword: 搜索关键词（如手机、电脑）\n" +
            "- productId: 商品ID\n" +
            "- couponId: 优惠券ID\n" +
            "- points: 积分数量\n\n" +
            "请准确识别意图，置信度应该基于用户表达的明确程度。";
    }
    
    /**
     * 验证意图类型是否有效
     * @param intentType 意图类型
     * @return 是否有效
     */
    private boolean isValidIntent(String intentType) {
        if (intentType == null) {
            return false;
        }
        for (String validType : INTENT_TYPES) {
            if (validType.equals(intentType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取意图描述
     * @param intentType 意图类型
     * @return 描述
     */
    private String getIntentDescription(String intentType) {
        switch (intentType) {
            case "ORDER_QUERY": return "查询订单信息";
            case "LOGISTICS_QUERY": return "查询物流信息";
            case "COUPON_RECEIVE": return "领取优惠券";
            case "COUPON_LIST": return "查看优惠券列表";
            case "POINTS_QUERY": return "查询积分余额";
            case "POINTS_USE": return "使用积分";
            case "PRODUCT_SEARCH": return "搜索商品";
            case "PRODUCT_RECOMMEND": return "商品推荐";
            case "PRODUCT_COMPARE": return "商品对比";
            case "CART_QUERY": return "查询购物车";
            case "REFUND_APPLY": return "申请退款";
            case "REFUND_QUERY": return "查询退款进度";
            case "NOTIFICATION_QUERY": return "查询消息通知";
            case "FAVORITE_QUERY": return "查询收藏";
            case "GREETING": return "问候";
            case "CUSTOMER_SERVICE": return "智能客服";
            default: return "未知意图";
        }
    }
    
    /**
     * 回退到关键词匹配
     * 当LLM识别失败时使用
     * @param message 用户消息
     * @return 识别的意图
     */
    private Intent fallbackToKeywordMatching(String message) {
        // 这里可以调用原有的NLPService的关键词匹配逻辑
        // 或者实现一个简单的关键词匹配作为备选方案
        return new Intent("UNKNOWN", "未知意图", new HashMap<>(), 0.0);
    }
}
