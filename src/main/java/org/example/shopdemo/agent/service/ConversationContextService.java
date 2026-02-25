package org.example.shopdemo.agent.service;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话上下文管理服务
 * 管理多轮对话的上下文信息
 * 支持槽位填充和上下文保持
 */
@Service
public class ConversationContextService {
    
    /**
     * 用户上下文映射表
     * 键：用户ID，值：用户的对话上下文
     */
    private static final Map<Long, ConversationContext> userContexts = new HashMap<>();
    
    /**
     * 获取用户的对话上下文
     * @param userId 用户ID
     * @return 对话上下文
     */
    public ConversationContext getContext(Long userId) {
        return userContexts.computeIfAbsent(userId, k -> new ConversationContext());
    }
    
    /**
     * 设置用户的当前意图
     * @param userId 用户ID
     * @param intent 当前意图
     */
    public void setCurrentIntent(Long userId, String intent) {
        ConversationContext context = getContext(userId);
        context.setCurrentIntent(intent);
        context.setLastUpdateTime(System.currentTimeMillis());
    }
    
    /**
     * 获取用户的当前意图
     * @param userId 用户ID
     * @return 当前意图
     */
    public String getCurrentIntent(Long userId) {
        ConversationContext context = getContext(userId);
        return context.getCurrentIntent();
    }
    
    /**
     * 设置槽位值
     * @param userId 用户ID
     * @param slotName 槽位名称
     * @param value 槽位值
     */
    public void setSlot(Long userId, String slotName, Object value) {
        ConversationContext context = getContext(userId);
        context.getSlots().put(slotName, value);
        context.setLastUpdateTime(System.currentTimeMillis());
    }
    
    /**
     * 获取槽位值
     * @param userId 用户ID
     * @param slotName 槽位名称
     * @return 槽位值
     */
    public Object getSlot(Long userId, String slotName) {
        ConversationContext context = getContext(userId);
        return context.getSlots().get(slotName);
    }
    
    /**
     * 检查槽位是否已填充
     * @param userId 用户ID
     * @param slotName 槽位名称
     * @return 是否已填充
     */
    public boolean hasSlot(Long userId, String slotName) {
        ConversationContext context = getContext(userId);
        return context.getSlots().containsKey(slotName);
    }
    
    /**
     * 清除槽位
     * @param userId 用户ID
     * @param slotName 槽位名称
     */
    public void clearSlot(Long userId, String slotName) {
        ConversationContext context = getContext(userId);
        context.getSlots().remove(slotName);
        context.setLastUpdateTime(System.currentTimeMillis());
    }
    
    /**
     * 清除所有槽位
     * @param userId 用户ID
     */
    public void clearAllSlots(Long userId) {
        ConversationContext context = getContext(userId);
        context.getSlots().clear();
        context.setCurrentIntent(null);
        context.setLastUpdateTime(System.currentTimeMillis());
    }
    
    /**
     * 检查上下文是否过期
     * @param userId 用户ID
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否过期
     */
    public boolean isContextExpired(Long userId, long timeoutMs) {
        ConversationContext context = getContext(userId);
        long elapsed = System.currentTimeMillis() - context.getLastUpdateTime();
        return elapsed > timeoutMs;
    }
    
    /**
     * 清除过期上下文
     * @param timeoutMs 超时时间（毫秒）
     */
    public void clearExpiredContexts(long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        userContexts.entrySet().removeIf(entry -> {
            long elapsed = currentTime - entry.getValue().getLastUpdateTime();
            return elapsed > timeoutMs;
        });
    }
    
    /**
     * 对话上下文类
     */
    public static class ConversationContext {
        /**
         * 当前意图
         */
        private String currentIntent;
        
        /**
         * 槽位映射表
         * 存储对话过程中收集的信息
         */
        private Map<String, Object> slots = new HashMap<>();
        
        /**
         * 最后更新时间
         */
        private long lastUpdateTime;
        
        public ConversationContext() {
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public String getCurrentIntent() {
            return currentIntent;
        }
        
        public void setCurrentIntent(String currentIntent) {
            this.currentIntent = currentIntent;
        }
        
        public Map<String, Object> getSlots() {
            return slots;
        }
        
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
        
        public void setLastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }
    }
}
