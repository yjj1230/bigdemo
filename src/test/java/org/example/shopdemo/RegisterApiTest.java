package org.example.shopdemo;

import org.example.shopdemo.dto.RegisterRequest;
import org.example.shopdemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class RegisterApiTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterApi() {
        try {
            // 模拟前端发送的请求
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser2024");
            request.setPassword("password123");
            request.setEmail("newuser2024@example.com");
            request.setPhone("13900139000");
            request.setNickname("新用户2024");
            
            System.out.println("测试注册API...");
            System.out.println("发送的注册数据:");
            System.out.println("  用户名: " + request.getUsername());
            System.out.println("  密码: " + request.getPassword());
            System.out.println("  邮箱: " + request.getEmail());
            System.out.println("  手机号: " + request.getPhone());
            System.out.println("  昵称: " + request.getNickname());
            
            userService.register(request);
            
            System.out.println("✅ 注册API测试成功！");
            
        } catch (Exception e) {
            System.err.println("❌ 注册API测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
