package org.example.shopdemo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 * 提供Redis缓存操作功能
 */
@Service
public class RedisCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final long DEFAULT_EXPIRE_TIME = 30;
    
    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
    }
    
    /**
     * 设置缓存并指定过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
    
    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 判断缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置缓存（永不过期）
     * @param key 缓存键
     * @param value 缓存值
     */
    public void setForever(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 生成缓存键
     * @param prefix 前缀
     * @param key 键
     * @return 完整的缓存键
     */
    public static String generateKey(String prefix, String key) {
        return prefix + ":" + key;
    }
    
    /**
     * 生成缓存键（支持多个参数）
     * @param parts 键的各个部分
     * @return 完整的缓存键
     */
    public static String generateKey(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
    
    /**
     * 根据模式删除缓存
     * @param pattern 缓存键模式
     */
    public void deleteByPattern(String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
    }
}
