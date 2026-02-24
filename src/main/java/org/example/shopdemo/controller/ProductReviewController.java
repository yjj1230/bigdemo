package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.AddReviewRequest;
import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.ProductReview;
import org.example.shopdemo.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品评价控制器
 * 处理商品评价相关的API请求
 */
@Tag(name = "商品评价", description = "商品评价管理接口")
@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * 添加评价
     * @param request 请求对象
     * @param token JWT token
     * @return 评价ID
     */
    @PostMapping
    @Operation(summary = "添加评价", description = "用户对商品进行评价")
    public Result<Long> addReview(
            @Valid @RequestBody AddReviewRequest request,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(token);
            
            ProductReview review = new ProductReview();
            review.setProductId(request.getProductId());
            review.setUserId(userId);
            review.setUsername(httpRequest.getAttribute("username").toString());
            review.setRating(request.getRating());
            review.setContent(request.getContent());
            review.setImages(request.getImages());
            review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : 0);
            review.setLikeCount(0);
            
            Long reviewId = productReviewService.addReview(review);
            return Result.success(reviewId);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 检查用户是否购买过指定商品
     * @param productId 商品ID
     * @param token JWT token
     * @return 是否购买过
     */
    @GetMapping("/can-review/{productId}")
    @Operation(summary = "检查是否可以评价", description = "检查用户是否购买过指定商品，只有购买过的用户才能评价")
    public Result<Boolean> canReview(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean canReview = productReviewService.hasUserPurchasedProduct(userId, productId);
            return Result.success(canReview);
        } catch (Exception e) {
            return Result.success(false);
        }
    }

    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @param pageRequest 分页请求
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评价列表", description = "分页获取指定商品的评价列表")
    public Result<PageResponse<ProductReview>> getProductReviews(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Valid PageRequest pageRequest) {
        PageResponse<ProductReview> response = productReviewService.getProductReviews(productId, pageRequest);
        return Result.success(response);
    }

    /**
     * 获取用户评价列表
     * @param token JWT token
     * @param pageRequest 分页请求
     * @return 评价列表
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户评价列表", description = "分页获取当前用户的评价列表")
    public Result<PageResponse<ProductReview>> getUserReviews(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Valid PageRequest pageRequest) {
        Long userId = getUserIdFromToken(token);
        PageResponse<ProductReview> response = productReviewService.getUserReviews(userId, pageRequest);
        return Result.success(response);
    }

    /**
     * 更新评价
     * @param id 评价ID
     * @param request 请求对象
     * @param token JWT token
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新评价", description = "用户更新自己的评价")
    public Result<Void> updateReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Valid @RequestBody AddReviewRequest request,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        
        ProductReview review = productReviewService.getReviewById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            return Result.error("无权修改此评价");
        }
        
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        review.setIsAnonymous(request.getIsAnonymous());
        
        productReviewService.updateReview(review);
        return Result.success("更新成功");
    }

    /**
     * 删除评价
     * @param id 评价ID
     * @param token JWT token
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价", description = "用户删除自己的评价")
    public Result<Void> deleteReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        
        ProductReview review = productReviewService.getReviewById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            return Result.error("无权删除此评价");
        }
        
        productReviewService.deleteReview(id);
        return Result.success("删除成功");
    }

    /**
     * 点赞评价
     * @param id 评价ID
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评价", description = "用户对评价进行点赞")
    public Result<Void> likeReview(
            @Parameter(description = "评价ID") @PathVariable Long id) {
        productReviewService.likeReview(id);
        return Result.success("点赞成功");
    }

    /**
     * 商家回复评价
     * @param id 评价ID
     * @param reply 回复内容
     * @return 操作结果
     */
    @PostMapping("/{id}/reply")
    @RequireAdmin
    @Operation(summary = "商家回复评价", description = "商家回复用户评价")
    public Result<Void> replyReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Parameter(description = "回复内容") @RequestParam String reply) {
        productReviewService.replyReview(id, reply);
        return Result.success("回复成功");
    }

    /**
     * 获取商品平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    @GetMapping("/rating/{productId}")
    @Operation(summary = "获取商品平均评分", description = "获取指定商品的平均评分")
    public Result<Double> getAverageRating(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Double rating = productReviewService.getAverageRating(productId);
        return Result.success(rating != null ? rating : 0.0);
    }
    
    /**
     * 获取商品评价统计数据
     * @param productId 商品ID
     * @return 评价统计数据
     */
    @GetMapping("/statistics/{productId}")
    @Operation(summary = "获取商品评价统计数据", description = "获取指定商品的评价统计数据，包含总评价数、平均评分和各星级评价分布")
    public Result<java.util.Map<String, Object>> getReviewStatistics(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        java.util.Map<String, Object> statistics = productReviewService.getReviewStatistics(productId);
        return Result.success(statistics);
    }

    /**
     * 从token中获取用户ID
     * @param token JWT token
     * @return 用户ID
     */
    private Long getUserIdFromToken(String token) {
        String tokenValue = token.replace("Bearer ", "");
        return org.example.shopdemo.utils.jwtutil.getUserIdFromToken(tokenValue);
    }
}
