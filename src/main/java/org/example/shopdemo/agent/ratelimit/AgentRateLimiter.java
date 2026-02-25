package org.example.shopdemo.agent.ratelimit;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent速率限制器
 * 防止用户频繁请求，保护系统稳定性
 */
@Component
public class AgentRateLimiter {
    
    /**
     * 用户请求计数器
     * Key: userId, Value: 请求计数
     */
    private final ConcurrentHashMap<Long, AtomicInteger> requestCounters = new ConcurrentHashMap<>();
    
    /**
     * 用户最后请求时间
     * Key: userId, Value: 最后请求时间戳
     */
    private final ConcurrentHashMap<Long, Long> lastRequestTimes = new ConcurrentHashMap<>();
    
    /**
     * 时间窗口（毫秒）
     */
    private static final long TIME_WINDOW = 60000; // 1分钟
    
    /**
     * 最大请求数
     */
    private static final int MAX_REQUESTS = 10;
    
    /**
     * 检查是否允许请求
     * @param userId 用户ID
     * @return 是否允许
     */
    public boolean allowRequest(Long userId) {
        long currentTime = System.currentTimeMillis();
        Long lastRequestTime = lastRequestTimes.get(userId);
        
        // 如果是第一次请求或时间窗口已过，重置计数器
        if (lastRequestTime == null || (currentTime - lastRequestTime) >= TIME_WINDOW) {
            requestCounters.put(userId, new AtomicInteger(1));
            lastRequestTimes.put(userId, currentTime);
            return true;
        }
        
        // 检查请求数是否超过限制
        AtomicInteger counter = requestCounters.get(userId);
        if (counter == null) {
            counter = new AtomicInteger(0);
            requestCounters.put(userId, counter);
        }
        
        int currentCount = counter.incrementAndGet();
        
        if (currentCount > MAX_REQUESTS) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取剩余请求次数
     * @param userId 用户ID
     * @return 剩余次数
     */
    public int getRemainingRequests(Long userId) {
        AtomicInteger counter = requestCounters.get(userId);
        if (counter == null) {
            return MAX_REQUESTS;
        }
        return Math.max(0, MAX_REQUESTS - counter.get());
    }
    
    /**
     * 获取重置时间（秒）
     * @param userId 用户ID
     * @return 重置时间（秒）
     */
    public long getResetTime(Long userId) {
        Long lastRequestTime = lastRequestTimes.get(userId);
        if (lastRequestTime == null) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - lastRequestTime;
        long remaining = TIME_WINDOW - elapsed;
        
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * 重置用户计数器
     * @param userId 用户ID
     */
    public void resetUser(Long userId) {
        requestCounters.remove(userId);
        lastRequestTimes.remove(userId);
    }
    
    /**
     * 清理过期计数器
     */
    public void cleanupExpiredCounters() {
        long currentTime = System.currentTimeMillis();
        
        lastRequestTimes.entrySet().removeIf(entry -> {
            long elapsed = currentTime - entry.getValue();
            return elapsed >= TIME_WINDOW;
        });
        
        requestCounters.entrySet().removeIf(entry -> {
            Long lastRequestTime = lastRequestTimes.get(entry.getKey());
            return lastRequestTime == null || (currentTime - lastRequestTime) >= TIME_WINDOW;
        });
    }
}
