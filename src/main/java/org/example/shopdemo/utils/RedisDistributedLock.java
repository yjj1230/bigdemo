package org.example.shopdemo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Redis分布式锁工具类
 * 用于解决分布式环境下的并发问题
 */
@Slf4j
@Component
public class RedisDistributedLock {
/**
 * 分布式锁的原理说明：
 * 
 * 分布式锁是一种在分布式系统中实现资源互斥访问的机制，主要用于解决多节点并发访问共享资源的问题。
 * 
 * 核心原理：
 * 1. 原子性操作：利用Redis的SETNX（SET if Not eXists）命令实现原子性的加锁操作
 * 2. 锁标识（锁值）：每个锁都有唯一标识（UUID），防止误删其他客户端的锁
 * 3. 过期时间：设置锁的过期时间，避免死锁问题
 * 4. 本地锁保护：使用ReentrantLock保护Redis操作，防止同一JVM内多线程竞争
 * 
 * 工作流程：
 * 1. 加锁：客户端生成唯一标识，通过SETNX命令尝试获取锁
 * 2. 续期：持有锁的客户端需要定期续期（本实现未包含自动续期）
 * 3. 解锁：验证锁标识后删除对应的Redis键
 * 4. 等待机制：获取锁失败时进行轮询等待
 * 
 * 关键特性：
 * - 互斥性：同一时刻只有一个客户端能获得锁
 * - 安全性：锁只能被持有者释放
 * - 容错性：网络分区或节点故障时不会永久阻塞
 * - 可重入性：支持同一线程多次获取同一把锁（本实现为基础版本）
 *
 * 工作流程：
 * 客户端A:
 * 1. 生成锁值: uuid-A
 * 2. 获取锁: SET lock:key uuid-A EX 30 NX  ← 成功
 * 3. 执行业务逻辑
 * 4. 释放锁: DEL lock:key (验证uuid-A) ← 成功
 *
 * 客户端B:
 * 1. 生成锁值: uuid-B
 * 2. 获取锁: SET lock:key uuid-B EX 30 NX  ← 失败（锁已被占用）
 * 3. 等待重试...
 * 4. 释放锁: DEL lock:key (验证uuid-B) ← 失败（不是自己的锁）
 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ReentrantLock是一个可重入的互斥锁，用于保护本地代码块的线程安全
    // 在分布式锁场景中，它确保同一JVM内的多个线程不会同时访问Redis操作
    private final Lock localLock = new ReentrantLock();
    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_WAIT_TIME = 3000;

    /**
     * 默认锁过期时间（毫秒）
     * 用于防止死锁，确保锁在一定时间后自动释放
     */
    private static final long DEFAULT_EXPIRE_TIME = 30000;

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的键
     * @param value 锁的值
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String value, long expireTime, TimeUnit timeUnit) {
//        这段代码的作用是尝试获取分布式锁。首先，它使用 `localLock.tryLock(100, TimeUnit.MILLISECONDS)` 尝试获取本地锁，
//        超时时间为100毫秒。如果成功获取到本地锁，则继续执行以下操作：
//        1. 构造完整的锁键（`fullKey`），通过 `getFullKey(lockKey)` 方法生成。
//        2. 使用 Redis 的 `setIfAbsent` 方法尝试在 Redis 中设置键值对，如果键不存在则设置成功并返回 `true`，
//        否则返回 `false`。同时设置了过期时间以防止死锁。
//        3. 记录日志，输出尝试获取锁的结果。
//        4. 最后，在 `finally` 块中释放本地锁，确保即使发生异常也能正确释放锁。
//        如果未能获取到本地锁，则直接返回 `false`。
        try {
            if (localLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    String fullKey = getFullKey(lockKey);
                    Boolean result = redisTemplate.opsForValue()
                            .setIfAbsent(fullKey, value, expireTime, timeUnit);
                    log.debug("尝试获取锁: {}, 结果: {}", fullKey, result);
                    return Boolean.TRUE.equals(result);
                } finally {
                    localLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断: {}", lockKey, e);
        }
        return false;
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的键
     * @param value 锁的值
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String value) {
        try {
            if (localLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    //获取完整的锁键
                    String fullKey = getFullKey(lockKey);
                    Object currentValue = redisTemplate.opsForValue().get(fullKey);
                    if (value.equals(currentValue)) {
                        redisTemplate.delete(fullKey);
                        log.debug("释放锁: {}", fullKey);
                        return true;
                    }
                } finally {
                    // 无论是否成功释放锁，都要释放本地锁
                    localLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            // 处理线程中断异常
            Thread.currentThread().interrupt();
            log.error("释放锁被中断: {}", lockKey, e);
        }
        return false;
    }

    /**
     * 等待获取分布式锁
     * @param lockKey 锁的键
     * @param value 锁的值
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @param waitTime 等待时间
     * @return 是否获取成功
     */
    public boolean lock(String lockKey, String value, long expireTime, TimeUnit timeUnit, long waitTime) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < waitTime) {
            //尝试获取锁
            if (tryLock(lockKey, value, expireTime, timeUnit)) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待锁被中断: {}", lockKey, e);
                return false;
            }
        }
        log.warn("等待锁超时: {}", lockKey);
        return false;
    }

    /**
     * 生成唯一的锁值
     * @return 唯一的锁值
     */
    public String generateLockValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取完整的锁键
     * @param lockKey 锁的键
     * @return 完整的锁键
     */
    private String getFullKey(String lockKey) {
        return LOCK_PREFIX + lockKey;
    }

    /**
     * 使用分布式锁执行任务
     * @param lockKey 锁的键
     * @param task 要执行的任务
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        //生成锁值
        String lockValue = generateLockValue();
        try {
            //获取分布式锁
            if (lock(lockKey, lockValue, DEFAULT_EXPIRE_TIME, TimeUnit.MILLISECONDS, DEFAULT_WAIT_TIME)) {
                log.debug("获取锁成功，开始执行任务: {}", lockKey);
                return task.get();
            } else {
                log.warn("获取锁失败: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 使用分布式锁执行任务（带等待时间和租约时间）
     * @param lockKey 锁的键
     * @param waitTime 等待时间（毫秒）
     * @param leaseTime 租约时间（毫秒）
     * @param task 要执行的任务
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, java.util.function.Supplier<T> task) {
        String lockValue = generateLockValue();
        try {
            if (lock(lockKey, lockValue, leaseTime, TimeUnit.MILLISECONDS, waitTime)) {
                log.debug("获取锁成功，开始执行任务: {}", lockKey);
                return task.get();
            } else {
                log.warn("获取锁失败: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
        } finally {
            unlock(lockKey, lockValue);
        }
    }
}
