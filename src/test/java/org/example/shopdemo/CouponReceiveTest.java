package org.example.shopdemo;

import org.example.shopdemo.entity.Coupon;
import org.example.shopdemo.mapper.CouponMapper;
import org.example.shopdemo.mapper.UserCouponMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class CouponReceiveTest {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Test
    public void testCouponReceiveLogic() {
        try {
            // 获取第一个优惠券
            Coupon coupon = couponMapper.selectById(1L);
            System.out.println("优惠券信息:");
            System.out.println("ID: " + coupon.getId());
            System.out.println("名称: " + coupon.getName());
            System.out.println("状态: " + coupon.getStatus());
            System.out.println("已领取: " + coupon.getReceivedCount());
            System.out.println("总数: " + coupon.getTotalCount());
            System.out.println("每人限领: " + coupon.getLimitPerUser());
            
            // 检查用户领取数量
            Long userId = 1L;
            int userReceivedCount = userCouponMapper.countByUserAndCoupon(userId, coupon.getId());
            System.out.println("用户ID " + userId + " 已领取数量: " + userReceivedCount);
            
            // 检查是否可以领取
            if (coupon.getStatus() != 2) {
                System.out.println("❌ 优惠券不可领取，状态: " + coupon.getStatus());
            } else if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
                System.out.println("❌ 优惠券已领完");
            } else if (userReceivedCount >= coupon.getLimitPerUser()) {
                System.out.println("❌ 已达到领取上限，已领: " + userReceivedCount + "，限领: " + coupon.getLimitPerUser());
            } else {
                System.out.println("✅ 可以领取优惠券");
            }
            
            // 检查所有优惠券的限领数量
            List<Coupon> allCoupons = couponMapper.selectAvailableCoupons();
            System.out.println("\n所有可用优惠券:");
            for (Coupon c : allCoupons) {
                System.out.println("ID: " + c.getId() + 
                        ", 名称: " + c.getName() + 
                        ", 限领: " + c.getLimitPerUser());
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
