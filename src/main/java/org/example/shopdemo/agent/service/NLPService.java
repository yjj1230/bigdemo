package org.example.shopdemo.agent.service;

import org.example.shopdemo.agent.model.Intent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * agent（tool、Agenttype、LLm（大语言模型）、initialize、NLPService）
 * 自然语言处理服务
 * 负责识别用户消息的意图和提取参数
 * 使用关键词匹配的方式实现简单的意图识别
 * 代理类型Agenttype：物流查询、订单查询、优惠券领取、优惠券列表、
 * 积分查询、积分使用、商品搜索、商品推荐、商品对比、购物车查询、退款申请、退款查询、消息查询、收藏查询、问候、客服
 */
@Service
public class NLPService {
    
    /**
     * 意图关键词映射表
     * 键：意图类型，值：该意图对应的关键词数组
     * 用于匹配用户输入，识别用户意图
     */
    private static final Map<String, String[]> INTENT_KEYWORDS = new HashMap<>();
    
    /**
     * 静态初始化块
     * 在类加载时初始化意图关键词映射表
     */
    static {
        // 物流相关（优先级高，放在前面）
        INTENT_KEYWORDS.put("LOGISTICS_QUERY", new String[]{"物流", "快递", "配送", "发货", "运单", "物流信息", "快递信息", "配送信息"});
        
        // 订单相关（使用更具体的关键词，避免与物流查询冲突）
        INTENT_KEYWORDS.put("ORDER_QUERY", new String[]{"查找订单", "我的订单", "订单详情", "订单状态", "订单列表", "查看订单","订单信息"});
        
        // 优惠券相关
        INTENT_KEYWORDS.put("COUPON_RECEIVE", new String[]{"领券", "领优惠", "优惠券", "优惠卷", "领个券"});
        INTENT_KEYWORDS.put("COUPON_LIST", new String[]{"优惠券", "优惠卷", "我的券", "可用券"});
        
        // 积分相关
        INTENT_KEYWORDS.put("POINTS_QUERY", new String[]{"积分", "我的积分", "积分查询", "积分余额"});
        INTENT_KEYWORDS.put("POINTS_USE", new String[]{"用积分", "积分抵扣", "兑换积分"});
        
        // 商品相关
        INTENT_KEYWORDS.put("PRODUCT_SEARCH", new String[]{"商品", "搜商品", "找商品", "搜索"});
        INTENT_KEYWORDS.put("PRODUCT_RECOMMEND", new String[]{"推荐", "推荐商品", "有什么好货", "热门商品", "猜你喜欢"});
        INTENT_KEYWORDS.put("PRODUCT_COMPARE", new String[]{"对比", "比较", "商品对比", "哪个好", "推荐哪个"});
        
        // 购物车相关
        INTENT_KEYWORDS.put("CART_QUERY", new String[]{"购物车", "我的购物车", "购物车查询","查看购物车"});
        
        // 退款相关
        INTENT_KEYWORDS.put("REFUND_APPLY", new String[]{"退款", "退货", "售后", "申请退款"});
        INTENT_KEYWORDS.put("REFUND_QUERY", new String[]{"退款进度", "退款状态", "退款查询"});
        
        // 消息相关
        INTENT_KEYWORDS.put("NOTIFICATION_QUERY", new String[]{"消息", "通知", "我的消息", "未读消息"});
        
        // 收藏相关
        INTENT_KEYWORDS.put("FAVORITE_QUERY", new String[]{"收藏", "我的收藏", "收藏夹","查询收藏"});
        
        // 问候相关
        INTENT_KEYWORDS.put("GREETING", new String[]{"你好", "您好", "hello", "hi", "嗨"});
        
        // 客服相关
        INTENT_KEYWORDS.put("CUSTOMER_SERVICE", new String[]{"客服", "帮助", "能做什么", "功能", "我的账户", "个人信息"});
    }
    
    /**
     * 识别用户消息的意图
     * 通过关键词匹配的方式识别用户想要做什么
     * 优先匹配包含更多关键词的意图
     * @param message 用户输入的消息
     * @return 识别出的意图对象，如果无法识别则返回未知意图
     */
    public Intent detectIntent(String message) {
        // 检查消息是否为空
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        // 将消息转换为小写，便于匹配
        String lowerMessage = message.toLowerCase();
        
        //**************** 用于存储匹配到的意图及其匹配的关键词数量
        Map<String, Integer> intentMatches = new HashMap<>();
        
        // 遍历所有意图类型，匹配关键词
        for (Map.Entry<String, String[]> entry : INTENT_KEYWORDS.entrySet()) {
            int matchCount = 0;
            for (String keyword : entry.getValue()) {
                // 如果消息中包含该关键词，增加匹配计数
                if (lowerMessage.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            // 如果有匹配，记录该意图及其匹配的关键词数量
            if (matchCount > 0) {
                intentMatches.put(entry.getKey(), matchCount);
            }
        }
        
        // 如果没有匹配到任何意图，返回未知意图
        if (intentMatches.isEmpty()) {
            return new Intent("UNKNOWN", "未知意图", new HashMap<>(), 0.0);
        }
        
        // 找到匹配关键词数量最多的意图
        String bestIntent = null;
        int maxMatches = 0;
        for (Map.Entry<String, Integer> entry : intentMatches.entrySet()) {
            if (entry.getValue() > maxMatches) {
                maxMatches = entry.getValue();
                // 获取匹配关键词数量最多的意图类型
                bestIntent = entry.getKey();
            }
        }
        
        // 如果有多个意图匹配相同数量的关键词，使用优先级规则
        // 物流查询优先于订单查询
        if (maxMatches == 1) {
            if (intentMatches.containsKey("LOGISTICS_QUERY") && intentMatches.containsKey("ORDER_QUERY")) {
                bestIntent = "LOGISTICS_QUERY";
            }
        }
        
        // 提取参数   意图类型bestIntent  根据不同的信息提取不同的参数放入params中
        Map<String, Object> params = extractParams(message, bestIntent);
        // 返回识别出的意图       获取意图的描述getIntentDescription(bestIntent)
        return new Intent(bestIntent, getIntentDescription(bestIntent), params, 0.9);
    }
    
    /**
     * 从用户消息中提取参数
     * 根据意图类型提取不同的参数，如订单号、搜索关键词等
     * @param message 用户输入的消息
     * @param intentType 意图类型
     * @return 提取的参数映射表
     */
    private Map<String, Object> extractParams(String message, String intentType) {
        Map<String, Object> params = new HashMap<>();
        
        // 根据意图类型提取不同的参数
        switch (intentType) {
            case "ORDER_QUERY":
                // 提取订单号
                params.put("orderNo", extractOrderNo(message));
                break;
            case "LOGISTICS_QUERY":
                // 提取订单号
                params.put("orderNo", extractOrderNo(message));
                break;
            case "PRODUCT_SEARCH":
                // 提取搜索关键词
                params.put("keyword", extractKeyword(message));
                break;
            case "REFUND_APPLY":
                // 提取订单号
                params.put("orderNo", extractOrderNo(message));
                break;
            case "PRODUCT_COMPARE":
                // 提取商品ID
                extractProductIds(message, params);
                break;
        }
        
        return params;
    }
    
    /**
     * 从消息中提取订单号
     * 支持多种格式：订单号123、订单：123、订单号：123、ORD+数字、纯数字（10位以上）
     * @param message 用户输入的消息
     * @return 提取的订单号，如果没有找到则返回null
     */
    private String extractOrderNo(String message) {
        // 匹配"订单号123"、"订单：123"、"订单号：123"、"ORD+数字"等格式
        Pattern pattern = Pattern.compile("订单号*[:：]?\\s*([A-Za-z]+\\d+|\\d{10,})");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 匹配字母开头的订单号（如ORD20260224211108325）
        pattern = Pattern.compile("\\b([A-Za-z]+\\d{10,})\\b");
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 匹配纯数字（10位以上）
        pattern = Pattern.compile("\\b(\\d{10,})\\b");
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 从消息中提取搜索关键词
     * 提取"商品"、"搜"、"找"、"搜索"后面的内容作为关键词
     * @param message 用户输入的消息
     * @return 提取的关键词，如果没有找到则返回null
     */
    private String extractKeyword(String message) {
        // 定义可能包含关键词的触发词
        String[] keywords = {"商品", "搜", "找", "搜索"};
        
        // 遍历触发词，提取后面的内容
        for (String keyword : keywords) {
            int index = message.indexOf(keyword);
            if (index != -1) {
                String result = message.substring(index + keyword.length()).trim();
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * 从消息中提取商品ID
     * 支持多种格式：商品1、商品ID1、商品ID：1、商品：1
     * @param message 用户输入的消息
     * @param params 参数映射表，用于存储提取的商品ID
     */
    private void extractProductIds(String message, Map<String, Object> params) {
        // 匹配"商品1"、"商品ID1"、"商品ID：1"、"商品：1"、"商品1和商品2"等格式
        Pattern pattern = Pattern.compile("商品(?:ID)?[:：]?\\s*(\\d+)");
        Matcher matcher = pattern.matcher(message);
        
        java.util.List<Long> productIds = new java.util.ArrayList<>();
        
        // 提取所有匹配的商品ID
        while (matcher.find()) {
            try {
                Long productId = Long.parseLong(matcher.group(1));
                productIds.add(productId);
            } catch (NumberFormatException e) {
                // 忽略无法解析的数字
            }
        }
        
        // 如果找到了两个商品ID，分别存储
        if (productIds.size() >= 2) {
            params.put("product1Id", productIds.get(0));
            params.put("product2Id", productIds.get(1));
        } else if (productIds.size() == 1) {
            // 如果只找到一个商品ID，存储为第一个商品
            params.put("product1Id", productIds.get(0));
        }
    }
    
    /**
     * 获取意图的描述
     * @param intentType 意图类型
     * @return 意图描述
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
}
