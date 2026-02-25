package org.example.shopdemo.agent.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话历史记录
 * 用于存储用户的对话历史，实现agent的记忆功能
 */
@Data
public class ConversationHistory {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 对话消息列表
     */
    private List<Message> messages;
    
    /**
     * 最大历史记录数量
     * 超过这个数量时，删除最旧的记录
     */
    private static final int MAX_HISTORY_SIZE = 10;
    
    /**
     * 构造函数
     * @param userId 用户ID
     */
    public ConversationHistory(Long userId) {
        this.userId = userId;
        this.messages = new ArrayList<>();
    }
    
    /**
     * 添加用户消息
     * @param message 用户消息
     */
    public void addUserMessage(String message) {
        messages.add(new Message("user", message));
        trimHistory();
    }
    
    /**
     * 添加助手消息
     * @param message 助手消息
     */
    public void addAssistantMessage(String message) {
        messages.add(new Message("assistant", message));
        trimHistory();
    }
    
    /**
     * 获取最近的N条消息
     * @param count 消息数量
     * @return 最近的消息列表
     */
    public List<Message> getRecentMessages(int count) {
        int fromIndex = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(fromIndex, messages.size()));
    }
    
    /**
     * 获取所有消息
     * @return 所有消息列表
     */
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * 清空历史记录
     */
    public void clear() {
        messages.clear();
    }
    
    /**
     * 修剪历史记录
     * 如果消息数量超过最大值，删除最旧的记录
     */
    private void trimHistory() {
        while (messages.size() > MAX_HISTORY_SIZE) {
            messages.remove(0);
        }
    }
    
    /**
     * 对话消息
     */
    @Data
    public static class Message {
        /**
         * 消息角色：user或assistant
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 构造函数
         * @param role 消息角色
         * @param content 消息内容
         */
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
