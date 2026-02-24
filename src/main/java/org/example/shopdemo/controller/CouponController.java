package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Coupon;
import org.example.shopdemo.entity.UserCoupon;
import org.example.shopdemo.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券控制器
 * 处理优惠券相关的API请求
 */
@Tag(name = "优惠券", description = "优惠券管理接口")
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private org.example.shopdemo.mapper.CouponMapper couponMapper;

    /**
     * 获取所有可用优惠券
     * @return 优惠券列表
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用优惠券", description = "获取所有当前可领取的优惠券")
    public Result<List<Coupon>> getAvailableCoupons() {
        List<Coupon> coupons = couponService.getAvailableCoupons();
        return Result.success(coupons);
    }

    /**
     * 获取用户可领取的优惠券
     * @param token JWT token
     * @return 优惠券列表
     */
    @GetMapping("/available-for-user")
    @Operation(summary = "获取用户可领取的优惠券", description = "获取当前用户可领取的优惠券")
    public Result<List<Coupon>> getAvailableCouponsForUser(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<Coupon> coupons = couponService.getAvailableCouponsForUser(userId);
        return Result.success(coupons);
    }

    /**
     * 用户领取优惠券
     * @param couponId 优惠券ID
     * @param token JWT token
     * @return 用户优惠券ID
     */
    @PostMapping("/receive/{couponId}")
    @Operation(summary = "领取优惠券", description = "用户领取指定优惠券")
    public Result<Long> receiveCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            Long userCouponId = couponService.receiveCoupon(userId, couponId);
            return Result.success(userCouponId);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取用户的所有优惠券
     * @param token JWT token
     * @return 用户优惠券列表
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户优惠券", description = "获取当前用户的所有优惠券")
    public Result<List<UserCoupon>> getUserCoupons(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<UserCoupon> userCoupons = couponService.getUserCoupons(userId);
        return Result.success(userCoupons);
    }

    /**
     * 获取用户可用的优惠券
     * @param token JWT token
     * @return 用户优惠券列表
     */
    @GetMapping("/user/available")
    @Operation(summary = "获取用户可用优惠券", description = "获取当前用户可用的优惠券")
    public Result<List<UserCoupon>> getAvailableUserCoupons(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<UserCoupon> userCoupons = couponService.getAvailableUserCoupons(userId);
        return Result.success(userCoupons);
    }

    /**
     * 计算优惠券优惠金额
     * @param userCouponId 用户优惠券ID
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    @GetMapping("/calculate-discount")
    @Operation(summary = "计算优惠金额", description = "计算使用指定优惠券可以获得的优惠金额")
    public Result<BigDecimal> calculateDiscount(
            @Parameter(description = "用户优惠券ID") @RequestParam Long userCouponId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount) {
        try {
            UserCoupon userCoupon = couponService.getUserCouponById(userCouponId);
            BigDecimal discount = couponService.calculateDiscount(userCoupon, orderAmount);
            return Result.success(discount);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 管理员创建优惠券
     * @param coupon 优惠券对象
     * @return 优惠券ID
     */
    @PostMapping
    @RequireAdmin
    @Operation(summary = "创建优惠券", description = "管理员创建新优惠券")
    public Result<Long> createCoupon(@RequestBody Coupon coupon) {
        coupon.setReceivedCount(0);
        coupon.setUsedCount(0);
        coupon.setStatus(1);
        couponMapper.insert(coupon);
        return Result.success(coupon.getId());
    }

    /**
     * 管理员更新优惠券
     * @param id 优惠券ID
     * @param coupon 优惠券对象
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "更新优惠券", description = "管理员更新优惠券信息")
    public Result<Void> updateCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long id,
            @RequestBody Coupon coupon) {
        coupon.setId(id);
        couponMapper.update(coupon);
        return Result.success("更新成功");
    }

    /**
     * 管理员删除优惠券
     * @param id 优惠券ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "删除优惠券", description = "管理员删除优惠券")
    public Result<Void> deleteCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long id) {
        couponMapper.deleteById(id);
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
