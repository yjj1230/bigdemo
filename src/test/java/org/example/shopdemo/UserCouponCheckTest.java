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
public class UserCouponCheckTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void checkUserCouponTable() {
        try {
            // 检查user_coupon表中的数据
            List<Map<String, Object>> userCoupons = jdbcTemplate.queryForList("SELECT * FROM user_coupon");
            System.out.println("用户优惠券总数: " + userCoupons.size());
            
            for (Map<String, Object> userCoupon : userCoupons) {
                System.out.println("ID: " + userCoupon.get("id"));
                System.out.println("用户ID: " + userCoupon.get("user_id"));
                System.out.println("优惠券ID: " + userCoupon.get("coupon_id"));
                System.out.println("优惠券名称: " + userCoupon.get("coupon_name"));
                System.out.println("状态: " + userCoupon.get("status"));
                System.out.println("领取时间: " + userCoupon.get("receive_time"));
                System.out.println("----------------------------------------");
            }
            
            if (userCoupons.isEmpty()) {
                System.out.println("user_coupon表为空，这是正常的");
            }
        } catch (Exception e) {
            System.err.println("检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void checkUserCouponCount() {
        try {
            // 模拟查询用户领取某个优惠券的数量
            Long userId = 1L; // 假设用户ID为1
            Long couponId = 1L; // 假设优惠券ID为1
            
            String sql = "SELECT COUNT(*) FROM user_coupon WHERE user_id = ? AND coupon_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, couponId);
            
            System.out.println("用户ID " + userId + " 领取优惠券ID " + couponId + " 的数量: " + count);
            
            // 检查所有用户的领取情况
            String allUserSql = "SELECT user_id, coupon_id, COUNT(*) as count FROM user_coupon GROUP BY user_id, coupon_id";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(allUserSql);
            System.out.println("所有用户领取情况:");
            for (Map<String, Object> result : results) {
                System.out.println("用户ID: " + result.get("user_id") + 
                        ", 优惠券ID: " + result.get("coupon_id") + 
                        ", 领取数量: " + result.get("count"));
            }
        } catch (Exception e) {
            System.err.println("检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void clearUserCouponTable() {
        try {
            // 清空user_coupon表（测试用）
            int rows = jdbcTemplate.update("DELETE FROM user_coupon");
            System.out.println("清空了 " + rows + " 条用户优惠券记录");
        } catch (Exception e) {
            System.err.println("清空失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
