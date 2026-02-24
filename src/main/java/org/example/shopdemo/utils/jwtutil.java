package org.example.shopdemo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 用于生成和验证JWT token
 */
@Component
public class jwtutil {
    
    private static final String SECRET_KEY = "your-secret-key-must-be-at-least-256-bits-long-for-hs512-algorithm"; 
    private static final long EXPIRATION_TIME = 86400000; // 24小时，单位毫秒
    private static final String USER_ID_CLAIM = "userId";
    private static final String USERNAME_CLAIM = "username";
    private static final String ROLE_CLAIM = "role";
    
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成JWT token
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT token
     */
    public static String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId.toString())
                .claim(USERNAME_CLAIM, username)
                .claim(ROLE_CLAIM, 0)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 生成JWT token（包含角色信息）
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT token
     */
    public static String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId.toString())
                .claim(USERNAME_CLAIM, username)
                .claim(ROLE_CLAIM, role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 验证JWT token
     * @param token JWT token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JWT token中获取用户ID
     * @param token JWT token
     * @return 用户ID
     * @throws IllegalArgumentException 如果token中没有userId claim或userId为null
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String userIdStr = claims.get(USER_ID_CLAIM, String.class);
        if (userIdStr == null) {
            throw new IllegalArgumentException("Token中缺少userId信息，请重新登录");
        }
        return Long.parseLong(userIdStr);
    }

    /**
     * 从JWT token中获取用户名
     * @param token JWT token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get(USERNAME_CLAIM, String.class);
    }

    /**
     * 从JWT token中获取用户角色
     * @param token JWT token
     * @return 用户角色
     */
    public static Integer getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get(ROLE_CLAIM, Integer.class);
    }

    /**
     * 从Authorization头中提取token
     * @param authorization Authorization头
     * @return 纯净的token
     */
    public static String extractToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
