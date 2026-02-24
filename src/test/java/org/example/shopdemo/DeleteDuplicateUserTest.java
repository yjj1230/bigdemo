package org.example.shopdemo;

import org.example.shopdemo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class DeleteDuplicateUserTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void deleteDuplicateUser() {
        try {
            System.out.println("检查重复邮箱用户...");
            
            // 查找使用重复邮箱的用户
            String duplicateEmail = "2531104959@qq.com";
            org.example.shopdemo.entity.User duplicateUser = userMapper.findByEmail(duplicateEmail);
            
            if (duplicateUser != null) {
                System.out.println("找到重复邮箱用户:");
                System.out.println("  ID: " + duplicateUser.getId());
                System.out.println("  用户名: " + duplicateUser.getUsername());
                System.out.println("  邮箱: " + duplicateUser.getEmail());
                System.out.println("  手机号: " + duplicateUser.getPhone());
                System.out.println("  昵称: " + duplicateUser.getNickname());
                
                // 删除重复用户
                int deletedRows = userMapper.deleteById(duplicateUser.getId());
                System.out.println("✅ 已删除重复用户，影响行数: " + deletedRows);
            } else {
                System.out.println("未找到使用该邮箱的用户");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 删除失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
