package org.example.shopdemo.service;

import org.example.shopdemo.entity.Coupon;
import org.example.shopdemo.entity.UserCoupon;
import org.example.shopdemo.mapper.CouponMapper;
import org.example.shopdemo.mapper.UserCouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务类
 */
@Service
public class CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    /**
     * 获取所有可用优惠券
     * @return 优惠券列表
     */
    public List<Coupon> getAvailableCoupons() {
        couponMapper.updateCouponStatus();
        return couponMapper.selectAvailableCoupons();
    }

    /**
     * 获取用户可领取的优惠券
     * @param userId 用户ID
     * @return 优惠券列表
     */
    public List<Coupon> getAvailableCouponsForUser(Long userId) {
        couponMapper.updateCouponStatus();
        return couponMapper.selectAvailableCouponsForUser(userId);
    }

    /**
     * 用户领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券ID
     */
    @Transactional
    public Long receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        if (coupon.getStatus() != 2) {
            throw new RuntimeException("优惠券不可领取");
        }

        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已领完");
        }

        int userReceivedCount = userCouponMapper.countByUserAndCoupon(userId, couponId);
        if (userReceivedCount >= coupon.getLimitPerUser()) {
            throw new RuntimeException("已达到领取上限");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponName(coupon.getName());
        userCoupon.setType(coupon.getType());
        userCoupon.setDiscountAmount(coupon.getDiscountAmount());
        userCoupon.setDiscountRate(coupon.getDiscountRate());
        userCoupon.setMinAmount(coupon.getMinAmount());
        userCoupon.setMaxDiscount(coupon.getMaxDiscount());
        userCoupon.setStartTime(coupon.getStartTime());
        userCoupon.setEndTime(coupon.getEndTime());

        userCouponMapper.insert(userCoupon);
        couponMapper.incrementReceivedCount(couponId);

        return userCoupon.getId();
    }

    /**
     * 获取用户的所有优惠券
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    public List<UserCoupon> getUserCoupons(Long userId) {
        userCouponMapper.updateExpiredCoupons();
        return userCouponMapper.selectByUserId(userId);
    }

    /**
     * 获取用户可用的优惠券
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    public List<UserCoupon> getAvailableUserCoupons(Long userId) {
        userCouponMapper.updateExpiredCoupons();
        return userCouponMapper.selectAvailableByUserId(userId);
    }

    /**
     * 计算优惠券优惠金额
     * @param userCoupon 用户优惠券
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    public BigDecimal calculateDiscount(UserCoupon userCoupon, BigDecimal orderAmount) {
        BigDecimal minAmount = userCoupon.getMinAmount();
        if (minAmount != null && orderAmount.compareTo(minAmount) < 0) {
            throw new RuntimeException("未达到最低消费金额");
        }

        BigDecimal discount = BigDecimal.ZERO;
        switch (userCoupon.getType()) {
            case 1:
                BigDecimal discountAmount = userCoupon.getDiscountAmount();
                if (discountAmount == null) {
                    throw new RuntimeException("优惠券折扣金额未设置");
                }
                discount = discountAmount;
                break;
            case 2:
                BigDecimal discountRate = userCoupon.getDiscountRate();
                if (discountRate == null) {
                    throw new RuntimeException("优惠券折扣比例未设置");
                }
                discount = orderAmount.multiply(BigDecimal.ONE.subtract(discountRate));
                if (userCoupon.getMaxDiscount() != null && discount.compareTo(userCoupon.getMaxDiscount()) > 0) {
                    discount = userCoupon.getMaxDiscount();
                }
                break;
            case 3:
                discount = new BigDecimal("10.00");
                break;
        }

        return discount;
    }

    /**
     * 使用优惠券
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     */
    @Transactional
    public void useCoupon(Long userCouponId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        if (userCoupon.getStatus() != 1) {
            throw new RuntimeException("优惠券不可用");
        }

        userCouponMapper.useCoupon(userCouponId, orderId);
        couponMapper.incrementUsedCount(userCoupon.getCouponId());
    }

    /**
     * 根据ID查询优惠券
     * @param id 优惠券ID
     * @return 优惠券对象
     */
    public Coupon getCouponById(Long id) {
        return couponMapper.selectById(id);
    }

    /**
     * 根据ID查询用户优惠券
     * @param id 用户优惠券ID
     * @return 用户优惠券对象
     */
    public UserCoupon getUserCouponById(Long id) {
        return userCouponMapper.selectById(id);
    }
}
