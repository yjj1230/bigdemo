package org.example.shopdemo.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent配置类
 * 管理Agent的各种配置参数
 */
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentConfig {
    
    /**
     * 意图识别配置
     */
    private IntentRecognitionConfig intentRecognition = new IntentRecognitionConfig();
    
    /**
     * 对话上下文配置
     */
    private ConversationConfig conversation = new ConversationConfig();
    
    /**
     * 性能优化配置
     */
    private PerformanceConfig performance = new PerformanceConfig();
    
    /**
     * LLM配置
     */
    private LLMConfig llm = new LLMConfig();
    
    public IntentRecognitionConfig getIntentRecognition() {
        return intentRecognition;
    }
    
    public void setIntentRecognition(IntentRecognitionConfig intentRecognition) {
        this.intentRecognition = intentRecognition;
    }
    
    public ConversationConfig getConversation() {
        return conversation;
    }
    
    public void setConversation(ConversationConfig conversation) {
        this.conversation = conversation;
    }
    
    public PerformanceConfig getPerformance() {
        return performance;
    }
    
    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }
    
    public LLMConfig getLlm() {
        return llm;
    }
    
    public void setLlm(LLMConfig llm) {
        this.llm = llm;
    }
    
    /**
     * 意图识别配置
     */
    public static class IntentRecognitionConfig {
        /**
         * 意图识别方法：keyword, llm, hybrid
         */
        private String method = "hybrid";
        
        /**
         * 置信度阈值（0-1）
         */
        private double confidenceThreshold = 0.7;
        
        /**
         * 是否启用LLM意图识别
         */
        private boolean enableLLM = true;
        
        /**
         * 是否启用关键词意图识别
         */
        private boolean enableKeyword = true;
        
        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public double getConfidenceThreshold() {
            return confidenceThreshold;
        }
        
        public void setConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
        }
        
        public boolean isEnableLLM() {
            return enableLLM;
        }
        
        public void setEnableLLM(boolean enableLLM) {
            this.enableLLM = enableLLM;
        }
        
        public boolean isEnableKeyword() {
            return enableKeyword;
        }
        
        public void setEnableKeyword(boolean enableKeyword) {
            this.enableKeyword = enableKeyword;
        }
    }
    
    /**
     * 对话上下文配置
     */
    public static class ConversationConfig {
        /**
         * 上下文超时时间（毫秒）
         */
        private long contextTimeout = 300000; // 5分钟
        
        /**
         * 最大历史记录数
         */
        private int maxHistory = 10;
        
        /**
         * 是否启用上下文管理
         */
        private boolean enableContext = true;
        
        /**
         * 是否启用对话历史
         */
        private boolean enableHistory = true;
        
        public long getContextTimeout() {
            return contextTimeout;
        }
        
        public void setContextTimeout(long contextTimeout) {
            this.contextTimeout = contextTimeout;
        }
        
        public int getMaxHistory() {
            return maxHistory;
        }
        
        public void setMaxHistory(int maxHistory) {
            this.maxHistory = maxHistory;
        }
        
        public boolean isEnableContext() {
            return enableContext;
        }
        
        public void setEnableContext(boolean enableContext) {
            this.enableContext = enableContext;
        }
        
        public boolean isEnableHistory() {
            return enableHistory;
        }
        
        public void setEnableHistory(boolean enableHistory) {
            this.enableHistory = enableHistory;
        }
    }
    
    /**
     * 性能优化配置
     */
    public static class PerformanceConfig {
        /**
         * 是否启用缓存
         */
        private boolean enableCache = true;
        
        /**
         * 是否启用异步处理
         */
        private boolean enableAsync = false;
        
        /**
         * 最大重试次数
         */
        private int maxRetries = 3;
        
        /**
         * 重试延迟（毫秒）
         */
        private long retryDelay = 1000;
        
        /**
         * 是否启用性能监控
         */
        private boolean enableMonitoring = true;
        
        public boolean isEnableCache() {
            return enableCache;
        }
        
        public void setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
        }
        
        public boolean isEnableAsync() {
            return enableAsync;
        }
        
        public void setEnableAsync(boolean enableAsync) {
            this.enableAsync = enableAsync;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public long getRetryDelay() {
            return retryDelay;
        }
        
        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }
        
        public boolean isEnableMonitoring() {
            return enableMonitoring;
        }
        
        public void setEnableMonitoring(boolean enableMonitoring) {
            this.enableMonitoring = enableMonitoring;
        }
    }
    
    /**
     * LLM配置
     */
    public static class LLMConfig {
        /**
         * LLM模型名称
         */
        private String modelName = "deepseek-chat";
        
        /**
         * 温度参数（0-2）
         */
        private double temperature = 0.7;
        
        /**
         * 最大token数
         */
        private int maxTokens = 1000;
        
        /**
         * 超时时间（毫秒）
         */
        private long timeout = 30000; // 30秒
        
        public String getModelName() {
            return modelName;
        }
        
        public void setModelName(String modelName) {
            this.modelName = modelName;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public long getTimeout() {
            return timeout;
        }
        
        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
}
