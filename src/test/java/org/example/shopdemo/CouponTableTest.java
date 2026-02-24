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
public class CouponTableTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testCouponTableExists() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES LIKE 'coupon'");
            System.out.println("Coupon表是否存在: " + !tables.isEmpty());
            
            if (!tables.isEmpty()) {
                List<Map<String, Object>> coupons = jdbcTemplate.queryForList("SELECT * FROM coupon LIMIT 5");
                System.out.println("优惠券数量: " + coupons.size());
                for (Map<String, Object> coupon : coupons) {
                    System.out.println("优惠券: " + coupon.get("name"));
                }
            }
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testUserCouponTableExists() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES LIKE 'user_coupon'");
            System.out.println("UserCoupon表是否存在: " + !tables.isEmpty());
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
