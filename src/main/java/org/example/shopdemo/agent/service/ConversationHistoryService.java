package org.example.shopdemo.agent.service;

import org.example.shopdemo.agent.model.ConversationHistory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话历史管理服务
 * 负责存储和管理用户的对话历史
 * 使用内存存储，每个用户维护独立的对话历史
 */
@Service
public class ConversationHistoryService {
    
    /**
     * 对话历史映射表
     * 键：用户ID，值：该用户的对话历史
     */
    private Map<Long, ConversationHistory> historyMap = new ConcurrentHashMap<>();
    
    /**
     * 获取用户的对话历史
     * @param userId 用户ID
     * @return 对话历史对象
     * 保证每个用户单独的对话历史
     */
    public ConversationHistory getHistory(Long userId) {
        return historyMap.computeIfAbsent(userId, ConversationHistory::new);
    }
    
    /**
     * 添加用户消息
     * @param userId 用户ID
     * @param message 用户消息
     */
    public void addUserMessage(Long userId, String message) {
        ConversationHistory history = getHistory(userId);
        history.addUserMessage(message);
    }
    
    /**
     * 添加助手消息
     * @param userId 用户ID
     * @param message 助手消息
     */
    public void addAssistantMessage(Long userId, String message) {
        ConversationHistory history = getHistory(userId);
        history.addAssistantMessage(message);
    }
    
    /**
     * 获取用户最近的对话历史
     * @param userId 用户ID
     * @param count 消息数量
     * @return 最近的对话消息列表
     */
    public ConversationHistory.Message[] getRecentMessages(Long userId, int count) {
        ConversationHistory history = getHistory(userId);
        return history.getRecentMessages(count).toArray(new ConversationHistory.Message[0]);
    }
    
    /**
     * 获取用户的所有对话历史
     * @param userId 用户ID
     * @return 所有对话消息列表
     */
    public ConversationHistory.Message[] getAllMessages(Long userId) {
        ConversationHistory history = getHistory(userId);
        return history.getAllMessages().toArray(new ConversationHistory.Message[0]);
    }
    
    /**
     * 清空用户的对话历史
     * @param userId 用户ID
     */
    public void clearHistory(Long userId) {
        ConversationHistory history = historyMap.get(userId);
        if (history != null) {
            history.clear();
        }
    }
    
    /**
     * 清空所有对话历史
     */
    public void clearAll() {
        historyMap.clear();
    }
}
