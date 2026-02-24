package org.example.shopdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.exception.BusinessException;
import org.example.shopdemo.mapper.ProductMapper;
import org.example.shopdemo.utils.RedisDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 库存服务类
 * 处理库存相关的业务逻辑，防止超卖
 */
@Slf4j
@Service
public class StockService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    private static final String STOCK_LOCK_PREFIX = "stock_lock:";
    private static final String STOCK_CACHE_PREFIX = "stock_cache:";
    private static final long LOCK_WAIT_TIME = 5;
    private static final long LOCK_LEASE_TIME = 10;

    /**
     * 扣减库存（使用分布式锁）
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    @Transactional
    public boolean deductStock(Long productId, Integer quantity) {
        String lockKey = STOCK_LOCK_PREFIX + productId;
        
        return redisDistributedLock.executeWithLock(lockKey, LOCK_WAIT_TIME * 1000, LOCK_LEASE_TIME * 1000, () -> {
            int affectedRows = productMapper.updateStock(productId, quantity);
            if (affectedRows == 0) {
                log.warn("库存扣减失败 - productId: {}, quantity: {}", productId, quantity);
                throw new BusinessException("库存不足，请稍后再试");
            }
            
            log.info("库存扣减成功 - productId: {}, quantity: {}", productId, quantity);
            clearStockCache(productId);
            return true;
        });
    }

    /**
     * 回滚库存（使用分布式锁）
     * @param productId 商品ID
     * @param quantity 回滚数量
     */
    @Transactional
    public void rollbackStock(Long productId, Integer quantity) {
        String lockKey = STOCK_LOCK_PREFIX + productId;
        redisDistributedLock.executeWithLock(lockKey, LOCK_WAIT_TIME * 1000, LOCK_LEASE_TIME * 1000, () -> {
            productMapper.increaseStock(productId, quantity);
            log.info("库存回滚成功 - productId: {}, quantity: {}", productId, quantity);
            //清除缓存
            clearStockCache(productId);
            return null;
        });
    }

    /**
     * 增加库存（使用分布式锁）
     * @param productId 商品ID
     * @param quantity 增加数量
     */
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        String lockKey = STOCK_LOCK_PREFIX + productId;
        redisDistributedLock.executeWithLock(lockKey, LOCK_WAIT_TIME * 1000, LOCK_LEASE_TIME * 1000, () -> {
            productMapper.increaseStock(productId, quantity);
            log.info("库存增加成功 - productId: {}, quantity: {}", productId, quantity);
            //清除缓存
            clearStockCache(productId);
            return null;
        });
    }

    /**
     * 预扣库存（使用乐观锁）
     * @param productId 商品ID
     * @param quantity 预扣数量
     * @return 是否成功
     */
    @Transactional
    public boolean preDeductStock(Long productId, Integer quantity) {
        String lockKey = STOCK_LOCK_PREFIX + productId;
        
        return redisDistributedLock.executeWithLock(lockKey, LOCK_WAIT_TIME * 1000, LOCK_LEASE_TIME * 1000, () -> {
            int affectedRows = productMapper.updateStock(productId, quantity);
            if (affectedRows == 0) {
                log.warn("库存预扣失败 - productId: {}, quantity: {}", productId, quantity);
                return false;
            }
            
            log.info("库存预扣成功 - productId: {}, quantity: {}", productId, quantity);
            clearStockCache(productId);
            return true;
        });
    }

    /**
     * 获取商品库存（带缓存）
     * @param productId 商品ID
     * @return 库存数量
     */
    public Integer getStock(Long productId) {
        String cacheKey = STOCK_CACHE_PREFIX + productId;
        Object cachedStock = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedStock != null) {
            return (Integer) cachedStock;
        }
        
        Integer stock = productMapper.getStockById(productId);
        if (stock != null) {
            redisTemplate.opsForValue().set(cacheKey, stock, 1, TimeUnit.MINUTES);
        }
        
        return stock;
    }

    /**
     * 清除库存缓存
     * @param productId 商品ID
     */
    private void clearStockCache(Long productId) {
        String cacheKey = STOCK_CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 批量扣减库存（使用分布式锁）
     * @param productItems 商品项列表
     * @return 是否全部成功
     */
    @Transactional
    public boolean batchDeductStock(java.util.List<StockItem> productItems) {
        for (StockItem item : productItems) {
            String lockKey = STOCK_LOCK_PREFIX + item.getProductId();
            boolean success = redisDistributedLock.executeWithLock(lockKey, LOCK_WAIT_TIME * 1000, LOCK_LEASE_TIME * 1000, () -> {
                //执行任务
                int affectedRows = productMapper.updateStock(item.getProductId(), item.getQuantity());
                if (affectedRows == 0) {
                    throw new BusinessException("商品库存不足：" + item.getProductId());
                }
                //取消订单并且回滚
                clearStockCache(item.getProductId());
                return true;
            });
            
            if (!success) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 库存项
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class StockItem {
        private Long productId;
        private Integer quantity;
    }
}
