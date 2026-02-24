package org.example.shopdemo;

import org.example.shopdemo.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class UpdateOrderTableTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void updateOrderTable() {
        try {
            System.out.println("开始更新订单表结构...");
            
            // 执行SQL更新语句
            String[] sqlStatements = {
                "ALTER TABLE `order` ADD COLUMN `coupon_id` BIGINT DEFAULT NULL COMMENT '优惠券ID' AFTER `user_id`",
                "ALTER TABLE `order` ADD COLUMN `original_amount` DECIMAL(10, 2) DEFAULT NULL COMMENT '原始金额' AFTER `coupon_id`",
                "ALTER TABLE `order` ADD COLUMN `coupon_discount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠券折扣金额' AFTER `original_amount`",
                "ALTER TABLE `order` ADD INDEX `idx_coupon_id` (`coupon_id`)"
            };
            
            for (String sql : sqlStatements) {
                try {
                    orderMapper.executeSql(sql);
                    System.out.println("✅ 执行成功: " + sql);
                } catch (Exception e) {
                    System.out.println("⚠️  执行失败（可能字段已存在）: " + sql);
                    System.out.println("   错误信息: " + e.getMessage());
                }
            }
            
            System.out.println("\n========== 更新完成 ==========");
            
        } catch (Exception e) {
            System.err.println("❌ 更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
