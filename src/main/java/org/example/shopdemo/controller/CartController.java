package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.dto.CartRequest;
import org.example.shopdemo.entity.Cart;
import org.example.shopdemo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 * 处理购物车相关的HTTP请求
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车管理", description = "购物车商品添加、查询、更新、删除接口")
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 获取用户的购物车
     * @param token JWT token
     * @return 购物车列表
     * @RequestHeader 是 Spring MVC 框架中的一个注解，
     * 主要用于从 HTTP 请求头中提取特定的请求头信息，并将其绑定到控制器方法的参数上。
     */
    @GetMapping("/list")
    @Operation(summary = "获取购物车", description = "获取当前登录用户的购物车商品列表")
    public Result<List<CartDTO>> getUserCart(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        return Result.success(cartService.getUserCart(userId));
    }

    /**
     * 添加商品到购物车
     * @param token JWT token
     * @param request 购物车请求对象
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车", description = "将商品添加到当前用户的购物车")
    public Result<Void> addToCart(@Parameter(hidden = true) @RequestHeader("Authorization") String token, @Valid @RequestBody CartRequest request) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        cartService.addToCart(userId, request);
        return Result.success("添加成功", null);
    }

    /**
     * 更新购物车商品数量
     * @param cartId 购物车ID
     * @param quantity 数量
     * @return 操作结果
     */
    @PutMapping("/update/{cartId}")
    @Operation(summary = "更新购物车商品数量", description = "更新购物车中商品的数量")
    public Result<Void> updateCartItem(@PathVariable Long cartId, @RequestParam Integer quantity) {
        cartService.updateCartItem(cartId, quantity);
        return Result.success("更新成功", null);
    }

    /**
     * 从购物车移除商品
     * @param cartId 购物车ID
     * @return 操作结果
     */
    @DeleteMapping("/{cartId}")
    @Operation(summary = "移除购物车商品", description = "从购物车中移除指定商品")
    public Result<Void> removeFromCart(@PathVariable Long cartId) {
        cartService.removeFromCart(cartId);
        return Result.success("删除成功", null);
    }

    /**
     * 清空购物车
     * @param token JWT token
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车", description = "清空当前用户购物车中的所有商品")
    public Result<Void> clearCart(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        Long userId = Long.parseLong(username);
        cartService.clearCart(userId);
        return Result.success("清空成功", null);
    }
}
