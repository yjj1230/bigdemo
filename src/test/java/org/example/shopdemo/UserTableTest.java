package org.example.shopdemo;

import org.example.shopdemo.entity.User;
import org.example.shopdemo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class UserTableTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testUserTable() {
        try {
            System.out.println("检查用户表...");
            
            List<User> users = userMapper.findAll();
            System.out.println("当前用户数量: " + users.size());
            
            if (!users.isEmpty()) {
                System.out.println("用户列表:");
                for (User user : users) {
                    System.out.println("ID: " + user.getId() + 
                            ", 用户名: " + user.getUsername() + 
                            ", 邮箱: " + user.getEmail() + 
                            ", 手机号: " + user.getPhone() + 
                            ", 昵称: " + user.getNickname() + 
                            ", 角色: " + user.getRole() + 
                            ", 状态: " + user.getStatus());
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
