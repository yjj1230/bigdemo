package org.example.shopdemo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 高级Redis缓存服务
 * 提供分布式锁、缓存击穿、缓存雪崩防护功能
 */
@Slf4j
@Service
public class AdvancedRedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    private static final String NULL_CACHE_PREFIX = "null_cache:";
    private static final String LOCK_PREFIX = "cache_lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30;
    private static final long NULL_CACHE_EXPIRE_TIME = 5;

    /**
     * 获取缓存（带缓存击穿防护）
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 缓存值
     */
    public <T> T getWithCacheBreakdownProtection(String key, Supplier<T> dataLoader, 
                                             long expireTime, TimeUnit timeUnit) {
        Object cachedValue = redisTemplate.opsForValue().get(key);
        
        if (cachedValue != null) {
            if (isNullCacheKey(key, cachedValue)) {
                return null;
            }
            log.debug("缓存命中: {}", key);
            return (T) cachedValue;
        }
        
        String lockKey = LOCK_PREFIX + key;
        return redisDistributedLock.executeWithLock(lockKey, () -> {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                if (isNullCacheKey(key, value)) {
                    return null;
                }
                log.debug("双重检查缓存命中: {}", key);
                return (T) value;
            }
            
            log.debug("缓存未命中，加载数据: {}", key);
            T loadedValue = dataLoader.get();
            
            if (loadedValue != null) {
                redisTemplate.opsForValue().set(key, loadedValue, expireTime, timeUnit);
            } else {
                setNullCache(key);
            }
            
            return loadedValue;
        });
    }

    /**
     * 获取缓存（带随机过期时间，防止缓存雪崩）
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param baseExpireTime 基础过期时间
     * @param timeUnit 时间单位
     * @return 缓存值
     */
    public <T> T getWithCacheAvalancheProtection(String key, Supplier<T> dataLoader,
                                                long baseExpireTime, TimeUnit timeUnit) {
        Object cachedValue = redisTemplate.opsForValue().get(key);
        
        if (cachedValue != null) {
            if (isNullCacheKey(key, cachedValue)) {
                return null;
            }
            log.debug("缓存命中: {}", key);
            return (T) cachedValue;
        }
        
        log.debug("缓存未命中，加载数据: {}", key);
        T loadedValue = dataLoader.get();
        
        if (loadedValue != null) {
            long randomExpireTime = getRandomExpireTime(baseExpireTime);
            redisTemplate.opsForValue().set(key, loadedValue, randomExpireTime, timeUnit);
            log.debug("设置缓存: {}, 过期时间: {} {}", key, randomExpireTime, timeUnit);
        } else {
            setNullCache(key);
        }
        
        return loadedValue;
    }

    /**
     * 获取缓存（完整防护）
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 缓存值
     */
    public <T> T getWithFullProtection(String key, Supplier<T> dataLoader,
                                      long expireTime, TimeUnit timeUnit) {
        return getWithCacheBreakdownProtection(key, () -> 
            getWithCacheAvalancheProtection(key, dataLoader, expireTime, timeUnit)
        , expireTime, timeUnit);
    }

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
    }

    /**
     * 设置缓存并指定过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (value != null) {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("设置缓存: {}, 过期时间: {} {}", key, timeout, timeUnit);
        } else {
            setNullCache(key);
        }
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        deleteNullCache(key);
        log.info("删除缓存: {}", key);
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
     * 设置空缓存，防止缓存穿透
     * @param key 缓存键
     */
    private void setNullCache(String key) {
        String nullKey = NULL_CACHE_PREFIX + key;
        redisTemplate.opsForValue().set(nullKey, "NULL", NULL_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.debug("设置空缓存: {}", nullKey);
    }

    /**
     * 删除空缓存
     * @param key 缓存键
     */
    private void deleteNullCache(String key) {
        String nullKey = NULL_CACHE_PREFIX + key;
        redisTemplate.delete(nullKey);
    }

    /**
     * 判断是否为空缓存
     * @param key 原始键
     * @param value 缓存值
     * @return 是否为空缓存
     */
    private boolean isNullCacheKey(String key, Object value) {
        String nullKey = NULL_CACHE_PREFIX + key;
        return "NULL".equals(value) && nullKey.equals(key);
    }

    /**
     * 获取随机过期时间，防止缓存雪崩
     * @param baseTime 基础时间
     * @return 随机过期时间
     */
    private long getRandomExpireTime(long baseTime) {
        double randomFactor = 0.8 + Math.random() * 0.4;
        return (long) (baseTime * randomFactor);
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
}
