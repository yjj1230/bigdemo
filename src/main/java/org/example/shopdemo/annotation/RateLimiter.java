package org.example.shopdemo.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 用于标记需要进行请求频率限制的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    
    /**
     * 限流key
     * 支持SpEL表达式，如：#userId, #request.remoteAddr
     * 默认为空，表示使用方法全限定名作为key
     */
    String key() default "";
    
    /**
     * 限流时间窗口（秒）
     * 默认60秒
     */
    int time() default 60;
    
    /**
     * 时间窗口内最大请求数
     * 默认100次
     */
    int count() default 100;
    
    /**
     * 限流提示信息
     * 默认为"请求过于频繁，请稍后再试"
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流类型
     * DEFAULT: 默认限流（基于方法）
     * IP: 基于IP限流
     * USER: 基于用户限流
     */
    LimitType limitType() default LimitType.DEFAULT;
    
    /**
     * 限流类型枚举
     */
    enum LimitType {
        DEFAULT,
        IP,
        USER
    }
}
