package org.example.shopdemo.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * 智能Agent服务
 * 这是统一智能Agent的核心类，  负责处理用户消息、识别意图、路由到合适的工具、执行工具、返回结果、记忆上下文
 * 实现了从单一入口处理多种用户需求的功能
 */
@Service
public class IntelligentAgent {
    @Autowired
    private DeepSeekChatModel deepSeekChatModel;
    
    /**
     * 所有工具类的列表
     * Spring会自动注入所有实现了Tool接口的类
     */
    @Autowired
    private List<Tool> tools;
    
    /**
     * 自然语言处理服务
     * 用于识别用户消息的意图和提取参数
     */
    @Autowired
    private NLPService nlpService;
    
    /**
     * JSON序列化工具
     */
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 对话历史管理服务
     * 用于存储和管理用户的对话历史
     */
    @Autowired
    private ConversationHistoryService historyService;
    
    /**
     * 工具映射表
     * 以工具名称为键，工具实例为值，便于快速查找
     */
    private Map<String, Tool> toolMap = new HashMap<>();
    
    /**
     * 初始化工具映射表
     * 在Spring容器启动时自动调用
     * 将所有工具注册到toolMap中
     */
    @Autowired
    public void initTools() {
        for (Tool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
        }
    }
    
    /**
     * 处理用户消息
     * 这是Agent的入口方法，接收用户消息并返回响应
     * @param message 用户输入的消息
     * @param userId 用户ID
     * @return Agent的响应文本
     */
    public String processUserMessage(String message, Long userId) {
        // 检查消息是否为空
        if (message == null || message.trim().isEmpty()) {
            return "请输入您的问题，我会尽力帮助您！";
        }
        
        try {
            // 1. 记录用户消息到历史
            historyService.addUserMessage(userId, message);
            
            // 2. 识别用户意图
            Intent intent = nlpService.detectIntent(message);
            
            // 3. 路由到合适的工具
            Tool tool = routeToTool(intent);
            
            String response;
            
            // 4. 如果没有找到合适的工具，使用LLM生成回复
            if (tool == null) {
                //基于聊天记录来回答
                response = generateLLMResponse(userId, message);
            } else {
                // 5. 执行工具并返回结果
                Result<Map<String, Object>> toolResult = tool.execute(message, userId, intent.getParams());
                
                // 6. 将Result转换为JSON字符串
                Map<String, Object> responseMap = new HashMap<>();
                if (toolResult.getCode() == 200) {
                    responseMap.put("message", toolResult.getData() != null ? toolResult.getData().get("message") : toolResult.getMessage());
                    responseMap.put("navigationType", toolResult.getData() != null ? toolResult.getData().get("navigationType") : null);
                    responseMap.put("navigationParams", toolResult.getData() != null ? toolResult.getData().get("navigationParams") : null);
                } else {
                    responseMap.put("message", toolResult.getMessage());
                    responseMap.put("navigationType", null);
                    responseMap.put("navigationParams", null);
                }
                
                response = objectMapper.writeValueAsString(responseMap);
            }
            
            // 7. 记录助手响应到历史
            historyService.addAssistantMessage(userId, response);
            
            return response;
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("message", "抱歉，处理您的请求时出现了错误：" + e.getMessage());
            errorMap.put("navigationType", null);
            errorMap.put("navigationParams", null);
            
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
     * 使用LLM生成回复
     * 基于对话历史生成更智能的回复
     * @param userId 用户ID
     * @param message 用户消息
     * @return LLM生成的回复
     */
    private String generateLLMResponse(Long userId, String message) {
        try {
            // 获取最近的对话历史（最近5条）
            ConversationHistory.Message[] history = historyService.getRecentMessages(userId, 8);
            
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
            String systemPrompt = "你是一个智能客服助手，帮助用户解决订单、物流、优惠券、积分等问题。请根据对话历史，提供准确、友好的回复。";
            
            // 创建Prompt
            Prompt prompt = new Prompt(messages);
            
            // 调用LLM
            ChatResponse chatResponse = deepSeekChatModel.call(prompt);
            String result = chatResponse.getResult().getOutput().getText();
            
            return "抱歉，我没有理解您的需求。您可以尝试询问关于订单、物流、优惠券、积分等问题。这是我根据你的问题从DeepSeek模型生成的回复：" + result;
            
        } catch (Exception e) {
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }
    
    /**
     * 路由到合适的工具
     * 根据意图类型找到能够处理该意图的工具
     * @param intent 用户意图
     * @return 能够处理该意图的工具，如果没有则返回null
     */
    private Tool routeToTool(Intent intent) {
        // 检查意图是否有效
        if (intent == null || intent.getType() == null) {
            return null;
        }
        // 遍历所有工具，找到能够处理该意图的工具
        for (Tool tool : tools) {
            if (tool.canHandle(intent.getType())) {
                return tool;
            }
        }
        
        // 没有找到合适的工具
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
