package org.example.shopdemo;

import org.example.shopdemo.dto.RegisterRequest;
import org.example.shopdemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class RegisterTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegister() {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser123");
            request.setPassword("password123");
            request.setEmail("testuser123@example.com");
            request.setPhone("13800138001");
            request.setNickname("测试用户");
            
            System.out.println("开始注册测试用户...");
            System.out.println("用户名: " + request.getUsername());
            System.out.println("邮箱: " + request.getEmail());
            System.out.println("手机号: " + request.getPhone());
            System.out.println("昵称: " + request.getNickname());
            
            userService.register(request);
            
            System.out.println("✅ 注册成功！");
            
        } catch (Exception e) {
            System.err.println("❌ 注册失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
