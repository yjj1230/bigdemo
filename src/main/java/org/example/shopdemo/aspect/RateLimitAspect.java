package org.example.shopdemo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.shopdemo.annotation.RateLimiter;
import org.example.shopdemo.exception.RateLimitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 基于Redis实现分布式限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {


    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * 环绕通知，拦截带有@RateLimiter注解的方法
     */
    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        
        String key = buildKey(joinPoint, rateLimiter, methodName);
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        String message = rateLimiter.message();
        
        try {
            boolean allowed = isAllowed(key, time, count);
            if (!allowed) {
                log.warn("限流触发 - key: {}, time: {}s, count: {}", key, time, count);
                throw new RateLimitException(message);
            }
            
            return joinPoint.proceed();
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("限流处理异常", e);
            throw e;
        }
    }

    /**
     * 构建限流key
     */
    private String buildKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter, String methodName) {
        String customKey = rateLimiter.key();
        RateLimiter.LimitType limitType = rateLimiter.limitType();
        
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_PREFIX);
        
        if (limitType == RateLimiter.LimitType.IP) {
            String ip = getClientIp();
            keyBuilder.append("ip:").append(ip);
        } else if (limitType == RateLimiter.LimitType.USER) {
            String userId = getUserId();
            keyBuilder.append("user:").append(userId);
        }
        
        if (!customKey.isEmpty()) {
            keyBuilder.append(":").append(customKey);
        } else {
            keyBuilder.append(":").append(methodName);
        }
        
        return keyBuilder.toString();
    }

    /**
     * 检查是否允许请求
     * 使用Lua脚本保证原子性
     */
    private boolean isAllowed(String key, int time, int count) {
        String luaScript = buildLuaScript();
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        
        Long result = stringRedisTemplate.execute(
            redisScript,
            Collections.singletonList(key),
            String.valueOf(count),
            String.valueOf(time)
        );
        
        return result != null && result == 1;
    }

    /**
     * 构建Lua脚本
     * 实现滑动窗口限流算法
     */
    private String buildLuaScript() {
        return "local key = KEYS[1]\n" +
               "local count = tonumber(ARGV[1])\n" +
               "local time = tonumber(ARGV[2])\n" +
               "local current = redis.call('INCR', key)\n" +
               "if current == 1 then\n" +
               "    redis.call('EXPIRE', key, time)\n" +
               "end\n" +
               "if current > count then\n" +
               "    return 0\n" +
               "else\n" +
               "    return 1\n" +
               "end";
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 获取用户ID
     */
    private String getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "anonymous";
        }
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return "anonymous";
        }
        
        try {
            String tokenValue = token.substring(7);
            Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(tokenValue);
            return String.valueOf(userId);
        } catch (Exception e) {
            return "anonymous";
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
