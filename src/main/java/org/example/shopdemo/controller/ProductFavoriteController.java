package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品收藏控制器
 * 处理商品收藏相关的API请求
 */
@Tag(name = "商品收藏", description = "商品收藏管理接口")
@RestController
@RequestMapping("/api/favorites")
public class ProductFavoriteController {

    @Autowired
    private ProductFavoriteService productFavoriteService;

    /**
     * 添加收藏
     * @param productId 商品ID
     * @param token JWT token
     * @return 操作结果
     */
    @PostMapping("/{productId}")
    @Operation(summary = "添加收藏", description = "用户收藏指定商品")
    public Result<Void> addFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        boolean success = productFavoriteService.addFavorite(userId, productId);
        if (success) {
            return Result.success("收藏成功");
        } else {
            return Result.error("商品已收藏");
        }
    }

    /**
     * 取消收藏
     * @param productId 商品ID
     * @param token JWT token
     * @return 操作结果
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "取消收藏", description = "用户取消收藏指定商品")
    public Result<Void> removeFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        boolean success = productFavoriteService.removeFavorite(userId, productId);
        if (success) {
            return Result.success("取消收藏成功");
        } else {
            return Result.error("收藏不存在");
        }
    }

    /**
     * 检查是否已收藏
     * @param productId 商品ID
     * @param token JWT token
     * @return 是否已收藏
     */
    @GetMapping("/check/{productId}")
    @Operation(summary = "检查是否已收藏", description = "检查用户是否已收藏指定商品")
    public Result<Boolean> isFavorited(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        boolean favorited = productFavoriteService.isFavorited(userId, productId);
        return Result.success(favorited);
    }

    /**
     * 获取用户收藏列表
     * @param token JWT token
     * @param pageRequest 分页请求
     * @return 商品列表
     */
    @GetMapping
    @Operation(summary = "获取用户收藏列表", description = "分页获取当前用户的收藏商品列表")
    public Result<PageResponse<Product>> getUserFavorites(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Valid PageRequest pageRequest) {
        Long userId = getUserIdFromToken(token);
        PageResponse<Product> response = productFavoriteService.getUserFavorites(userId, pageRequest);
        return Result.success(response);
    }

    /**
     * 删除收藏
     * @param id 收藏ID
     * @param token JWT token
     * @return 操作结果
     */
    @DeleteMapping("/id/{id}")
    @Operation(summary = "删除收藏", description = "根据收藏ID删除收藏")
    public Result<Void> deleteFavorite(
            @Parameter(description = "收藏ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        productFavoriteService.deleteFavorite(id);
        return Result.success("删除成功");
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
