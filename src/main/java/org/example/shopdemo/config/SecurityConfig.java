package org.example.shopdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置类
 * 配置密码加密器等安全相关的Bean
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置BCrypt密码编码器
     * 用于用户密码的加密和验证
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // 创建BCryptPasswordEncoder实例，用于密码加密
        // BCrypt是一种强哈希算法，适合用于存储用户密码
        // 默认强度为10，可以根据需要调整
        return new BCryptPasswordEncoder(12); // 使用强度12，提供更好的安全性
    }
}
