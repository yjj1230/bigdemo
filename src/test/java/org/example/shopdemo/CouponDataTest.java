package org.example.shopdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class CouponDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testCouponData() {
        try {
            List<Map<String, Object>> coupons = jdbcTemplate.queryForList("SELECT * FROM coupon");
            System.out.println("优惠券总数: " + coupons.size());
            
            for (Map<String, Object> coupon : coupons) {
                System.out.println("ID: " + coupon.get("id"));
                System.out.println("名称: " + coupon.get("name"));
                System.out.println("类型: " + coupon.get("type"));
                System.out.println("状态: " + coupon.get("status"));
                System.out.println("开始时间: " + coupon.get("start_time"));
                System.out.println("结束时间: " + coupon.get("end_time"));
                System.out.println("已领取: " + coupon.get("received_count"));
                System.out.println("总数: " + coupon.get("total_count"));
                System.out.println("限领: " + coupon.get("limit_per_user"));
                System.out.println("折扣金额: " + coupon.get("discount_amount"));
                System.out.println("折扣率: " + coupon.get("discount_rate"));
                System.out.println("最低金额: " + coupon.get("min_amount"));
                System.out.println("最高优惠: " + coupon.get("max_discount"));
                System.out.println("----------------------------------------");
            }
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateCouponTime() {
        try {
            // 更新优惠券时间到当前有效期内
            String sql = "UPDATE coupon SET start_time = NOW() - INTERVAL 1 DAY, end_time = NOW() + INTERVAL 30 DAY WHERE status = 2";
            int rows = jdbcTemplate.update(sql);
            System.out.println("更新了 " + rows + " 条优惠券的时间");
        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
