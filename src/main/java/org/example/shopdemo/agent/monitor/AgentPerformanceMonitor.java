package org.example.shopdemo.agent.monitor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent性能监控切面
 * 监控Agent相关方法的执行时间和性能指标
 */
@Aspect
@Component
public class AgentPerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentPerformanceMonitor.class);
    
    /**
     * 意图识别统计
     */
    private final AtomicLong intentRecognitionCount = new AtomicLong(0);
    private final AtomicLong intentRecognitionTotalTime = new AtomicLong(0);
    private final AtomicLong llmIntentCount = new AtomicLong(0);
    private final AtomicLong keywordIntentCount = new AtomicLong(0);
    
    /**
     * 工具执行统计
     */
    private final AtomicLong toolExecutionCount = new AtomicLong(0);
    private final AtomicLong toolExecutionTotalTime = new AtomicLong(0);
    private final AtomicLong toolExecutionFailureCount = new AtomicLong(0);
    
    /**
     * 对话处理统计
     */
    private final AtomicLong conversationCount = new AtomicLong(0);
    private final AtomicLong conversationTotalTime = new AtomicLong(0);
    private final AtomicLong conversationFailureCount = new AtomicLong(0);
    
    /**
     * 监控意图识别方法
     */
    @Around("execution(* org.example.shopdemo.agent.service.LLMIntentService.detectIntent(..))")
    public Object monitorIntentRecognition(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        intentRecognitionCount.incrementAndGet();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;
            intentRecognitionTotalTime.addAndGet(duration);
            llmIntentCount.incrementAndGet();
            
            logger.info("LLM意图识别执行完成，耗时: {} ms", duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("LLM意图识别执行失败，耗时: {} ms，错误: {}", duration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 监控关键词意图识别方法
     */
    @Around("execution(* org.example.shopdemo.agent.service.NLPService.detectIntent(..))")
    public Object monitorKeywordIntentRecognition(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        intentRecognitionCount.incrementAndGet();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;
            intentRecognitionTotalTime.addAndGet(duration);
            keywordIntentCount.incrementAndGet();
            
            logger.info("关键词意图识别执行完成，耗时: {} ms", duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("关键词意图识别执行失败，耗时: {} ms，错误: {}", duration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 监控工具执行方法
     */
    @Around("execution(* org.example.shopdemo.agent.tool.Tool.execute(..))")
    public Object monitorToolExecution(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        toolExecutionCount.incrementAndGet();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;
            toolExecutionTotalTime.addAndGet(duration);
            
            logger.info("工具执行完成，工具: {}，耗时: {} ms", pjp.getTarget().getClass().getSimpleName(), duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            toolExecutionFailureCount.incrementAndGet();
            logger.error("工具执行失败，工具: {}，耗时: {} ms，错误: {}", 
                pjp.getTarget().getClass().getSimpleName(), duration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 监控对话处理方法
     */
    @Around("execution(* org.example.shopdemo.agent.service.OptimizedIntelligentAgent.processUserMessage(..))")
    public Object monitorConversationProcessing(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        conversationCount.incrementAndGet();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;
            conversationTotalTime.addAndGet(duration);
            
            logger.info("对话处理完成，耗时: {} ms", duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            conversationFailureCount.incrementAndGet();
            logger.error("对话处理失败，耗时: {} ms，错误: {}", duration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取意图识别平均耗时
     */
    public double getAverageIntentRecognitionTime() {
        long count = intentRecognitionCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) intentRecognitionTotalTime.get() / count;
    }
    
    /**
     * 获取工具执行平均耗时
     */
    public double getAverageToolExecutionTime() {
        long count = toolExecutionCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) toolExecutionTotalTime.get() / count;
    }
    
    /**
     * 获取对话处理平均耗时
     */
    public double getAverageConversationTime() {
        long count = conversationCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) conversationTotalTime.get() / count;
    }
    
    /**
     * 获取LLM意图识别比例
     */
    public double getLLMIntentRatio() {
        long total = llmIntentCount.get() + keywordIntentCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) llmIntentCount.get() / total;
    }
    
    /**
     * 获取工具执行成功率
     */
    public double getToolExecutionSuccessRate() {
        long total = toolExecutionCount.get();
        if (total == 0) {
            return 0.0;
        }
        long failures = toolExecutionFailureCount.get();
        return (double) (total - failures) / total;
    }
    
    /**
     * 获取对话处理成功率
     */
    public double getConversationSuccessRate() {
        long total = conversationCount.get();
        if (total == 0) {
            return 0.0;
        }
        long failures = conversationFailureCount.get();
        return (double) (total - failures) / total;
    }
    
    /**
     * 获取性能统计信息
     */
    public String getPerformanceStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("\n=== Agent性能统计 ===\n");
        stats.append(String.format("意图识别次数: %d\n", intentRecognitionCount.get()));
        stats.append(String.format("意图识别平均耗时: %.2f ms\n", getAverageIntentRecognitionTime()));
        stats.append(String.format("LLM意图识别次数: %d (%.1f%%)\n", 
            llmIntentCount.get(), getLLMIntentRatio() * 100));
        stats.append(String.format("关键词意图识别次数: %d (%.1f%%)\n", 
            keywordIntentCount.get(), (1 - getLLMIntentRatio()) * 100));
        stats.append(String.format("工具执行次数: %d\n", toolExecutionCount.get()));
        stats.append(String.format("工具执行平均耗时: %.2f ms\n", getAverageToolExecutionTime()));
        stats.append(String.format("工具执行成功率: %.1f%%\n", getToolExecutionSuccessRate() * 100));
        stats.append(String.format("对话处理次数: %d\n", conversationCount.get()));
        stats.append(String.format("对话处理平均耗时: %.2f ms\n", getAverageConversationTime()));
        stats.append(String.format("对话处理成功率: %.1f%%\n", getConversationSuccessRate() * 100));
        stats.append("====================\n");
        
        return stats.toString();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        intentRecognitionCount.set(0);
        intentRecognitionTotalTime.set(0);
        llmIntentCount.set(0);
        keywordIntentCount.set(0);
        toolExecutionCount.set(0);
        toolExecutionTotalTime.set(0);
        toolExecutionFailureCount.set(0);
        conversationCount.set(0);
        conversationTotalTime.set(0);
        conversationFailureCount.set(0);
        
        logger.info("性能统计信息已重置");
    }
}
