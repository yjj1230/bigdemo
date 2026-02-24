package org.example.shopdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class FixCouponTimeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void fixCouponTimes() {
        try {
            // 更新优惠券时间到当前有效期内
            String sql = "UPDATE coupon SET " +
                    "start_time = NOW() - INTERVAL 1 DAY, " +
                    "end_time = NOW() + INTERVAL 30 DAY, " +
                    "status = 2 " +
                    "WHERE status = 3";
            
            int rows = jdbcTemplate.update(sql);
            System.out.println("更新了 " + rows + " 条优惠券的时间和状态");
            
            // 验证更新结果
            String verifySql = "SELECT * FROM coupon";
            var coupons = jdbcTemplate.queryForList(verifySql);
            System.out.println("当前优惠券状态:");
            for (var coupon : coupons) {
                System.out.println("ID: " + coupon.get("id") + 
                        ", 名称: " + coupon.get("name") + 
                        ", 状态: " + coupon.get("status") + 
                        ", 开始时间: " + coupon.get("start_time") + 
                        ", 结束时间: " + coupon.get("end_time"));
            }
        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
