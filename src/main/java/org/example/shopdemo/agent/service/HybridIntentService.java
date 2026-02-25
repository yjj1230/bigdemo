package org.example.shopdemo.agent.service;

import org.example.shopdemo.agent.config.AgentConfig;
import org.example.shopdemo.agent.model.Intent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 混合意图识别服务
 * 结合关键词匹配和LLM识别的优势
 * 先用关键词快速匹配，置信度低时使用LLM重新识别
 */
@Service
public class HybridIntentService {
    
    @Autowired
    private NLPService nlpService;
    
    @Autowired
    private LLMIntentService llmIntentService;
    
    @Autowired
    private AgentConfig agentConfig;
    
    /**
     * 检测意图（混合模式）
     * 先用关键词快速匹配，如果置信度低则使用LLM重新识别
     * @param message 用户消息
     * @return 识别的意图
     */
    public Intent detectIntent(String message) {
        AgentConfig.IntentRecognitionConfig config = agentConfig.getIntentRecognition();
        
        // 如果只启用LLM
        if (config.isEnableLLM() && !config.isEnableKeyword()) {
            return llmIntentService.detectIntent(message);
        }
        
        // 如果只启用关键词
        if (config.isEnableKeyword() && !config.isEnableLLM()) {
            return nlpService.detectIntent(message);
        }
        
        // 混合模式：先用关键词匹配
        Intent keywordIntent = nlpService.detectIntent(message);
        
        // 如果关键词匹配的置信度低于阈值，使用LLM重新识别
        if (keywordIntent.getConfidence() < config.getConfidenceThreshold()) {
            Intent llmIntent = llmIntentService.detectIntent(message);
            
            // 记录日志
            System.out.println("关键词置信度低于阈值(" + config.getConfidenceThreshold() + ")，使用LLM重新识别");
            System.out.println("关键词识别: " + keywordIntent.getType() + " (置信度: " + keywordIntent.getConfidence() + ")");
            System.out.println("LLM识别: " + llmIntent.getType() + " (置信度: " + llmIntent.getConfidence() + ")");
            
            return llmIntent;
        }
        
        return keywordIntent;
    }
    
    /**
     * 检测意图（带回退）
     * 优先使用LLM，失败时回退到关键词匹配
     * @param message 用户消息
     * @return 识别的意图
     */
    public Intent detectIntentWithFallback(String message) {
        AgentConfig.IntentRecognitionConfig config = agentConfig.getIntentRecognition();
        
        // 优先使用LLM
        if (config.isEnableLLM()) {
            try {
                Intent llmIntent = llmIntentService.detectIntent(message);
                
                // 如果LLM识别成功且置信度高，直接返回
                if (llmIntent.getConfidence() >= config.getConfidenceThreshold()) {
                    return llmIntent;
                }
            } catch (Exception e) {
                System.err.println("LLM意图识别失败，回退到关键词匹配：" + e.getMessage());
            }
        }
        
        // 回退到关键词匹配
        if (config.isEnableKeyword()) {
            return nlpService.detectIntent(message);
        }
        
        // 都不可用，返回未知意图
        return new Intent("UNKNOWN", "未知意图", new java.util.HashMap<>(), 0.0);
    }
}
