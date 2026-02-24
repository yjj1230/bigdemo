package org.example.shopdemo;

import org.example.shopdemo.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class CouponReceiveSimulationTest {

    @Autowired
    private CouponService couponService;

    @Test
    public void simulateCouponReceive() {
        try {
            // 模拟用户ID为1领取优惠券ID为1
            Long userId = 1L;
            Long couponId = 1L;
            
            System.out.println("模拟用户 " + userId + " 领取优惠券 " + couponId);
            
            try {
                Long userCouponId = couponService.receiveCoupon(userId, couponId);
                System.out.println("✅ 领取成功！用户优惠券ID: " + userCouponId);
            } catch (RuntimeException e) {
                System.out.println("❌ 领取失败: " + e.getMessage());
            }
            
            // 尝试再次领取同一张优惠券
            System.out.println("\n尝试再次领取同一张优惠券...");
            try {
                Long userCouponId2 = couponService.receiveCoupon(userId, couponId);
                System.out.println("✅ 第二次领取成功！用户优惠券ID: " + userCouponId2);
            } catch (RuntimeException e) {
                System.out.println("❌ 第二次领取失败: " + e.getMessage());
            }
            
            // 尝试领取其他优惠券
            System.out.println("\n尝试领取其他优惠券...");
            Long[] otherCouponIds = new Long[]{2L, 3L, 4L};
            for (Long otherCouponId : otherCouponIds) {
                try {
                    Long userCouponId3 = couponService.receiveCoupon(userId, otherCouponId);
                    System.out.println("✅ 领取优惠券 " + otherCouponId + " 成功！用户优惠券ID: " + userCouponId3);
                } catch (RuntimeException e) {
                    System.out.println("❌ 领取优惠券 " + otherCouponId + " 失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
