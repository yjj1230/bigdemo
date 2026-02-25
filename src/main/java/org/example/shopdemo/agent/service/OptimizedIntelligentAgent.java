package org.example.shopdemo.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.shopdemo.agent.config.AgentConfig;
import org.example.shopdemo.agent.model.ConversationHistory;
import org.example.shopdemo.agent.model.Intent;
import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 优化后的智能Agent服务
 * 整合了LLM意图识别、上下文管理、智能回复等功能
 */
@Service
public class OptimizedIntelligentAgent {
    
    @Autowired
    private DeepSeekChatModel deepSeekChatModel;
    
    @Autowired
    private List<Tool> tools;
    // 混合意图识别服务
    @Autowired
    private HybridIntentService hybridIntentService;

    // 会话历史服务
    @Autowired
    private ConversationHistoryService historyService;
    //对话
    @Autowired
    private ConversationContextService contextService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Map<String, Tool> toolMap = new HashMap<>();
    
    /**
     * 初始化工具映射
     * 在Spring容器初始化时自动调用此方法，将所有可用的工具注册到toolMap中
     * @param tools Spring自动注入的工具列表
     */
    @Autowired
    public void initTools(List<Tool> tools) {
        // 遍历所有工具，将其名称作为key，工具对象作为value存入map中
        for (Tool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            // 输出调试信息，显示注册的工具名称和描述
            System.out.println("注册工具: " + tool.getToolName() + " - " + tool.getDescription());
        }
        // 输出总注册工具数量的统计信息
        System.out.println("总共注册了 " + toolMap.size() + " 个工具");
    }
    
    /**
     * 处理用户消息（优化版）
     * @param message 用户消息
     * @param userId 用户ID
     * @return 响应
     */
    public String processUserMessage(String message, Long userId) {
        if (message == null || message.trim().isEmpty()) {
            return "请输入您的问题，我会尽力帮助您！";
        }
        
        try {
            // 1. 记录用户消息
            historyService.addUserMessage(userId, message);
            
            // 2. 检查是否有未完成的对话
            String currentIntent = contextService.getCurrentIntent(userId);
            if (currentIntent != null && !contextService.isContextExpired(userId, 300000)) { // 5分钟内有效
                // 继续之前的对话
                return continueConversation(message, userId, currentIntent);
            }
            
            // 3. 使用混合意图识别
            Intent intent = hybridIntentService.detectIntent(message);
            
            System.out.println("=== Agent调试信息 ===");
            System.out.println("用户消息: " + message);
            System.out.println("识别意图: " + intent.getType());
            System.out.println("意图描述: " + intent.getDescription());
            System.out.println("置信度: " + intent.getConfidence());
            System.out.println("参数: " + intent.getParams());
            System.out.println("参数详情: " + (intent.getParams() != null ? intent.getParams().toString() : "null"));
            
            // 4. 设置当前意图
            contextService.setCurrentIntent(userId, intent.getType());
            
            // 5. 路由到工具
            Tool tool = routeToTool(intent);
            
            System.out.println("路由到的工具: " + (tool != null ? tool.getToolName() : "无"));
            
            String response;
            
            if (tool == null) {
                // 使用LLM生成智能回复
                response = generateSmartResponse(userId, message, intent);
            } else {
                // 检查是否需要收集更多信息
                String missingInfo = checkMissingInfo(intent, userId);
                System.out.println("是否需要更多信息: " + (missingInfo != null ? "是 - " + missingInfo : "否"));
                
                if (missingInfo != null) {
                    // 需要更多信息，询问用户
                    response = missingInfo;
                } else {
                    // 执行工具
                    System.out.println("开始执行工具: " + tool.getToolName());
                    Result<Map<String, Object>> toolResult = tool.execute(message, userId, intent.getParams());
                    System.out.println("工具执行结果: " + toolResult.getMessage());
                    
                    // 转换为响应
                    Map<String, Object> responseMap = new HashMap<>();
                    if (toolResult.getCode() == 200) {
                        responseMap.put("message", toolResult.getData() != null ? toolResult.getData().get("message") : toolResult.getMessage());
                        responseMap.put("data", toolResult.getData());
                    } else {
                        responseMap.put("message", toolResult.getMessage());
                        responseMap.put("data", null);
                    }
                    
                    try {
                        response = objectMapper.writeValueAsString(responseMap);
                    } catch (JsonProcessingException e) {
                        response = "抱歉，处理响应时出现错误。";
                    }
                    
                    // 清除上下文（任务完成）
                    contextService.clearAllSlots(userId);
                }
            }
            
            // 6. 记录响应
            historyService.addAssistantMessage(userId, response);
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("message", "抱歉，处理您的请求时出现了错误：" + e.getMessage());
            errorMap.put("data", null);
            
            try {
                String errorResponse = objectMapper.writeValueAsString(errorMap);
                historyService.addAssistantMessage(userId, errorResponse);
                return errorResponse;
            } catch (Exception ex) {
                return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
            }
        }
    }
    
    /**
     * 继续之前的对话
     * @param message 用户消息
     * @param userId 用户ID
     * @param currentIntent 当前意图
     * @return 响应
     */
    private String continueConversation(String message, Long userId, String currentIntent) {
        // 重新识别意图，检查用户是否改变了意图
        Intent newIntent = hybridIntentService.detectIntent(message);
        
        System.out.println("=== 继续对话调试 ===");
        System.out.println("当前上下文意图: " + currentIntent);
        System.out.println("新识别的意图: " + newIntent.getType());
        
        // 如果新识别的意图与当前意图不同，清除上下文并使用新意图
        if (!newIntent.getType().equals(currentIntent)) {
            System.out.println("意图已改变，清除上下文并使用新意图");
            contextService.clearAllSlots(userId);
            contextService.setCurrentIntent(userId, newIntent.getType());
            
            // 路由到新意图的工具
            Tool tool = routeToTool(newIntent);
            
            if (tool != null) {
                String missingInfo = checkMissingInfo(newIntent, userId);
                if (missingInfo != null) {
                    return missingInfo;
                }
                
                Result<Map<String, Object>> toolResult = tool.execute(message, userId, newIntent.getParams());
                Map<String, Object> responseMap = new HashMap<>();
                if (toolResult.getCode() == 200) {
                    responseMap.put("message", toolResult.getData() != null ? toolResult.getData().get("message") : toolResult.getMessage());
                    responseMap.put("data", toolResult.getData());
                } else {
                    responseMap.put("message", toolResult.getMessage());
                    responseMap.put("data", null);
                }
                
                String response;
                try {
                    response = objectMapper.writeValueAsString(responseMap);
                } catch (JsonProcessingException e) {
                    response = "抱歉，处理响应时出现错误。";
                }
                
                contextService.clearAllSlots(userId);
                historyService.addAssistantMessage(userId, response);
                return response;
            }
            
            return "抱歉，我无法处理您的请求。";
        }
        
        // 意图未改变，继续填充槽位
        System.out.println("意图未改变，继续填充槽位");
        
        // 将用户输入填充到槽位
        fillSlotsFromMessage(message, userId, currentIntent);
        
        // 检查是否所有必要信息都已收集
        String missingInfo = checkMissingInfo(new Intent(currentIntent, "", contextService.getContext(userId).getSlots(), 1.0), userId);
        
        if (missingInfo != null) {
            // 还需要更多信息
            return missingInfo;
        }
        
        // 信息完整，路由到工具
        Intent intent = new Intent(currentIntent, "", contextService.getContext(userId).getSlots(), 1.0);
        Tool tool = routeToTool(intent);
        
        if (tool != null) {
            Result<Map<String, Object>> toolResult = tool.execute(message, userId, contextService.getContext(userId).getSlots());
                
            Map<String, Object> responseMap = new HashMap<>();
            if (toolResult.getCode() == 200) {
                responseMap.put("message", toolResult.getData() != null ? toolResult.getData().get("message") : toolResult.getMessage());
                responseMap.put("data", toolResult.getData());
            } else {
                responseMap.put("message", toolResult.getMessage());
                responseMap.put("data", null);
            }
            
            String response;
            try {
                response = objectMapper.writeValueAsString(responseMap);
            } catch (JsonProcessingException e) {
                response = "抱歉，处理响应时出现错误。";
            }
            
            // 清除上下文
            contextService.clearAllSlots(userId);
            
            historyService.addAssistantMessage(userId, response);
            return response;
        }
        
        return "抱歉，我无法处理您的请求。";
    }
    
    /**
     * 从消息中填充槽位
     * @param message 用户消息
     * @param userId 用户ID
     * @param intentType 意图类型
     */
    private void fillSlotsFromMessage(String message, Long userId, String intentType) {
        switch (intentType) {
            case "ORDER_QUERY":
                // 尝试提取订单号
                if (message.matches(".*\\d{10,}.*")) {
                    contextService.setSlot(userId, "orderNo", extractOrderNo(message));
                }
                break;
            case "LOGISTICS_QUERY":
                if (message.matches(".*\\d{10,}.*")) {
                    contextService.setSlot(userId, "orderNo", extractOrderNo(message));
                }
                break;
            case "PRODUCT_SEARCH":
                if (!message.trim().isEmpty()) {
                    contextService.setSlot(userId, "keyword", message.trim());
                }
                break;
            case "REFUND_APPLY":
                if (message.matches(".*\\d{10,}.*")) {
                    contextService.setSlot(userId, "orderNo", extractOrderNo(message));
                }
                break;
        }
    }
    
    /**
     * 检查缺失信息
     * @param intent 意图
     * @param userId 用户ID
     * @return 缺失信息提示，如果没有则返回null
     */
    private String checkMissingInfo(Intent intent, Long userId) {
        Map<String, Object> slots = contextService.getContext(userId).getSlots();
        
        switch (intent.getType()) {
            case "ORDER_QUERY":
                // ORDER_QUERY 不需要强制要求 orderNo
                // 如果有 orderNo，查询订单详情；如果没有，查询订单列表
                // 让 OrderTool 自己处理
                break;
            case "LOGISTICS_QUERY":
                // LOGISTICS_QUERY 需要订单号
                if (!slots.containsKey("orderNo") && !intent.getParams().containsKey("orderNo")) {
                    return "请提供订单号，例如：查询订单1234567890的物流信息";
                }
                break;
            case "PRODUCT_SEARCH":
                if (!slots.containsKey("keyword") && !intent.getParams().containsKey("keyword")) {
                    return "请告诉我您想搜索什么商品？";
                }
                break;
            case "REFUND_APPLY":
                if (!slots.containsKey("orderNo") && !intent.getParams().containsKey("orderNo")) {
                    return "请提供要退款的订单号";
                }
                break;
        }
        
        return null;
    }
    
    /**
     * 生成智能回复
     * @param userId 用户ID
     * @param message 用户消息
     * @param intent 意图
     * @return 智能回复
     */
    private String generateSmartResponse(Long userId, String message, Intent intent) {
        try {
            // 获取最近的对话历史
            ConversationHistory.Message[] history = historyService.getRecentMessages(userId, 10);
            
            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            
            // 添加历史对话
            for (ConversationHistory.Message historyMsg : history) {
                if ("user".equals(historyMsg.getRole())) {
                    messages.add(new UserMessage(historyMsg.getContent()));
                } else if ("assistant".equals(historyMsg.getRole())) {
                    messages.add(new AssistantMessage(historyMsg.getContent()));
                }
            }
            
            // 添加当前用户消息
            messages.add(new UserMessage(message));
            
            // 构建系统提示
            String systemPrompt = buildSystemPrompt(intent);
            
            // 创建Prompt
            Prompt prompt = new Prompt(messages);
            
            // 调用LLM
            ChatResponse chatResponse = deepSeekChatModel.call(prompt);
            String result = chatResponse.getResult().getOutput().getText();
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", result);
            responseMap.put("data", null);
            
            try {
                return objectMapper.writeValueAsString(responseMap);
            } catch (JsonProcessingException e) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("message", "抱歉，我没有理解您的需求。您可以尝试询问关于订单、物流、优惠券、积分等问题。");
                errorMap.put("data", null);
                return objectMapper.writeValueAsString(errorMap);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("message", "抱歉，我没有理解您的需求。您可以尝试询问关于订单、物流、优惠券、积分等问题。");
            errorMap.put("data", null);
            
            try {
                return objectMapper.writeValueAsString(errorMap);
            } catch (Exception ex) {
                return "抱歉，我没有理解您的需求。";
            }
        }
    }
    
    /**
     * 构建系统提示
     * @param intent 意图
     * @return 系统提示
     */
    private String buildSystemPrompt(Intent intent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能客服助手，帮助用户解决订单、物流、优惠券、积分等问题。\n\n");
        prompt.append("当前识别的用户意图：").append(intent.getType()).append("\n");
        prompt.append("意图描述：").append(intent.getDescription()).append("\n");
        prompt.append("置信度：").append(intent.getConfidence()).append("\n\n");
        
        prompt.append("请根据对话历史和当前意图，提供准确、友好、个性化的回复。\n");
        prompt.append("如果用户的问题不清楚，可以主动询问更多细节。\n");
        prompt.append("回复要简洁明了，避免冗长。\n");
        
        return prompt.toString();
    }
    
    /**
     * 路由到工具
     * @param intent 意图
     * @return 工具
     */
    private Tool routeToTool(Intent intent) {
        if (intent == null || intent.getType() == null) {
            System.out.println("路由失败：意图为空");
            return null;
        }
        
        System.out.println("开始路由，意图类型: " + intent.getType());
        System.out.println("可用工具数量: " + (tools == null ? "null" : tools.size()));
        
        for (Tool tool : tools) {
            System.out.println("检查工具: " + tool.getToolName() + ", canHandle(" + intent.getType() + "): " + tool.canHandle(intent.getType()));
            if (tool.canHandle(intent.getType())) {
                System.out.println("成功路由到工具: " + tool.getToolName());
                return tool;
            }
        }
        
        System.out.println("路由失败：没有找到能处理意图 " + intent.getType() + " 的工具");
        return null;
    }
    
    /**
     * 提取订单号
     * @param message 消息
     * @return 订单号
     */
    private String extractOrderNo(String message) {
        Pattern pattern = Pattern.compile("\\b([A-Za-z]+\\d{10,}|\\d{10,})\\b");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 获取所有工具
     * @return 工具列表
     */
    public List<Tool> getAllTools() {
        return tools;
    }
}
