package org.example.shopdemo.service;

import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.ProductReview;
import org.example.shopdemo.mapper.OrderItemMapper;
import org.example.shopdemo.mapper.ProductReviewMapper;
import org.example.shopdemo.utils.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品评价服务类
 * 处理商品评价相关的业务逻辑
 */
@Service
public class ProductReviewService {

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    private static final String REVIEW_CACHE_PREFIX = "product_review";
    private static final String REVIEW_COUNT_CACHE_PREFIX = "review_count";
    private static final String RATING_CACHE_PREFIX = "product_rating";

    /**
     * 添加评价
     * @param review 评价对象
     * @return 评价ID
     */
    @Transactional
    public Long addReview(ProductReview review) {
        if (!hasUserPurchasedProduct(review.getUserId(), review.getProductId())) {
            throw new RuntimeException("只有购买过该商品的用户才能评价");
        }
        productReviewMapper.insert(review);
        clearReviewCache(review.getProductId());
        return review.getId();
    }

    /**
     * 检查用户是否购买过指定商品
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 如果购买过返回true，否则返回false
     */
    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        return orderItemMapper.hasUserPurchasedProduct(userId, productId);
    }

    /**
     * 根据ID获取评价
     * @param id 评价ID
     * @return 评价对象
     */
    public ProductReview getReviewById(Long id) {
        return productReviewMapper.findById(id);
    }

    /**
     * 获取商品评价列表（分页）
     * @param productId 商品ID
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<ProductReview> getProductReviews(Long productId, PageRequest pageRequest) {
        // 构建缓存键，包含商品ID、页码和每页大小
        String cacheKey = RedisCacheService.generateKey(REVIEW_CACHE_PREFIX, productId.toString(), 
                                                      pageRequest.getPageNum().toString(), 
                                                      pageRequest.getPageSize().toString());
        
        // 尝试从缓存获取
        Object cachedResponse = redisCacheService.get(cacheKey);
        if (cachedResponse != null && cachedResponse instanceof PageResponse) {
            return (PageResponse<ProductReview>) cachedResponse;
        }
        
        // 缓存未命中，从数据库获取
        List<ProductReview> reviews = productReviewMapper.findByProductId(productId, pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productReviewMapper.countByProductId(productId);
        PageResponse<ProductReview> response = PageResponse.of(reviews, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
        
        // 存入缓存，设置过期时间为15分钟
        redisCacheService.set(cacheKey, response, 15, java.util.concurrent.TimeUnit.MINUTES);
        
        return response;
    }

    /**
     * 获取用户评价列表（分页）
     * @param userId 用户ID
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<ProductReview> getUserReviews(Long userId, PageRequest pageRequest) {
        List<ProductReview> reviews = productReviewMapper.findByUserId(userId, pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productReviewMapper.countByUserId(userId);
        return PageResponse.of(reviews, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
    }

    /**
     * 更新评价
     * @param review 评价对象
     */
    @Transactional
    public void updateReview(ProductReview review) {
        productReviewMapper.update(review);
        ProductReview existingReview = productReviewMapper.findById(review.getId());
        if (existingReview != null) {
            clearReviewCache(existingReview.getProductId());
        }
    }

    /**
     * 删除评价
     * @param id 评价ID
     */
    @Transactional
    public void deleteReview(Long id) {
        ProductReview review = productReviewMapper.findById(id);
        if (review != null) {
            productReviewMapper.deleteById(id);
            clearReviewCache(review.getProductId());
        }
    }

    /**
     * 点赞评价
     * @param id 评价ID
     */
    public void likeReview(Long id) {
        productReviewMapper.incrementLikeCount(id);
    }

    /**
     * 商家回复评价
     * @param id 评价ID
     * @param reply 回复内容
     */
    @Transactional
    public void replyReview(Long id, String reply) {
        productReviewMapper.replyReview(id, reply);
        ProductReview review = productReviewMapper.findById(id);
        if (review != null) {
            clearReviewCache(review.getProductId());
        }
    }

    /**
     * 获取商品平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    public Double getAverageRating(Long productId) {
        String cacheKey = RedisCacheService.generateKey(RATING_CACHE_PREFIX, productId.toString());
        Object cachedRating = redisCacheService.get(cacheKey);
        if (cachedRating != null) {
            return (Double) cachedRating;
        }

        Double rating = productReviewMapper.getAverageRating(productId);
        if (rating != null) {
            redisCacheService.set(cacheKey, rating, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        return rating;
    }
    
    /**
     * 获取商品评价统计数据
     * @param productId 商品ID
     * @return 评价统计数据，包含总评价数、平均评分和各星级评价分布
     */
    public java.util.Map<String, Object> getReviewStatistics(Long productId) {
        String cacheKey = RedisCacheService.generateKey("review_statistics", productId.toString());
        
        // 尝试从缓存获取
        Object cachedStats = redisCacheService.get(cacheKey);
        if (cachedStats != null && cachedStats instanceof java.util.Map) {
            return (java.util.Map<String, Object>) cachedStats;
        }
        
        // 缓存未命中，从数据库获取
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        
        // 获取总评价数
        Long totalCount = getReviewCount(productId);
        statistics.put("totalCount", totalCount);
        
        // 获取平均评分
        Double averageRating = getAverageRating(productId);
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        
        // 获取各星级评价分布
        List<java.util.Map<String, Object>> ratingDistribution = productReviewMapper.getRatingDistribution(productId);
        statistics.put("ratingDistribution", ratingDistribution);
        
        // 存入缓存，设置过期时间为1小时
        redisCacheService.set(cacheKey, statistics, 1, java.util.concurrent.TimeUnit.HOURS);
        
        return statistics;
    }

    /**
     * 获取商品评价数量
     * @param productId 商品ID
     * @return 评价数量
     */
    public Long getReviewCount(Long productId) {
        String cacheKey = RedisCacheService.generateKey(REVIEW_COUNT_CACHE_PREFIX, productId.toString());
        Object cachedCount = redisCacheService.get(cacheKey);
        if (cachedCount != null) {
            return (Long) cachedCount;
        }

        Long count = productReviewMapper.countByProductId(productId);
        redisCacheService.set(cacheKey, count, 30, java.util.concurrent.TimeUnit.MINUTES);
        return count;
    }

    /**
     * 清除评价缓存
     * @param productId 商品ID
     */
    private void clearReviewCache(Long productId) {
        String ratingCacheKey = RedisCacheService.generateKey(RATING_CACHE_PREFIX, productId.toString());
        String countCacheKey = RedisCacheService.generateKey(REVIEW_COUNT_CACHE_PREFIX, productId.toString());
        String statisticsCacheKey = RedisCacheService.generateKey("review_statistics", productId.toString());
        
        // 清除评分、计数和统计数据缓存
        redisCacheService.delete(ratingCacheKey);
        redisCacheService.delete(countCacheKey);
        redisCacheService.delete(statisticsCacheKey);
        
        // 清除评价列表缓存（使用通配符）
        String reviewListPattern = RedisCacheService.generateKey(REVIEW_CACHE_PREFIX, productId.toString(), "*", "*");
        redisCacheService.deleteByPattern(reviewListPattern);
    }
}
