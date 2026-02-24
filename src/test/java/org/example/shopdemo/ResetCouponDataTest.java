package org.example.shopdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class ResetCouponDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void resetCouponReceivedCount() {
        try {
            // 重置所有优惠券的已领取数量为0
            String sql = "UPDATE coupon SET received_count = 0, used_count = 0";
            int rows = jdbcTemplate.update(sql);
            System.out.println("重置了 " + rows + " 条优惠券的领取数量");
            
            // 验证重置结果
            String verifySql = "SELECT id, name, received_count, total_count FROM coupon";
            var coupons = jdbcTemplate.queryForList(verifySql);
            System.out.println("重置后的优惠券状态:");
            for (var coupon : coupons) {
                System.out.println("ID: " + coupon.get("id") + 
                        ", 名称: " + coupon.get("name") + 
                        ", 已领取: " + coupon.get("received_count") + 
                        ", 总数: " + coupon.get("total_count"));
            }
        } catch (Exception e) {
            System.err.println("重置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
