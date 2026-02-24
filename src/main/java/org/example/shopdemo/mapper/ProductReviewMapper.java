package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.ProductReview;

import java.util.List;

@Mapper
public interface ProductReviewMapper {
    int insert(ProductReview review);
    ProductReview findById(Long id);
    List<ProductReview> findByProductId(@Param("productId") Long productId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    List<ProductReview> findByUserId(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    int update(ProductReview review);
    int deleteById(Long id);
    Long countByProductId(Long productId);
    Long countByUserId(Long userId);
    int incrementLikeCount(@Param("id") Long id);
    int replyReview(@Param("id") Long id, @Param("reply") String reply);
    Double getAverageRating(Long productId);
    
    /**
     * 获取商品各星级评价数量
     * @param productId 商品ID
     * @return 各星级评价数量的列表，格式为：[{"rating": 5, "count": 10}, {"rating": 4, "count": 5}, ...]
     */
    List<java.util.Map<String, Object>> getRatingDistribution(Long productId);
    
    /**
     * 检查用户是否已经评价过指定商品
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 如果已评价过返回true，否则返回false
     */
    boolean hasUserReviewedProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
