package org.example.shopdemo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.utils.jwtutil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 * 用于拦截请求并验证JWT token
 */
@Slf4j
@Component
public class jwtinterceptor implements HandlerInterceptor {
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USERNAME_ATTRIBUTE = "username";

    /**
     * 前置处理方法
     * 在请求处理之前进行调用
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @return 是否继续执行后续拦截器和处理器
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
            return false;
        }
        
        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (!jwtutil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token验证失败\"}");
            return false;
        }
        
        try {
            // 将用户信息放入request中，方便后续使用
            Long userId = jwtutil.getUserIdFromToken(token);
            String username = jwtutil.getUsernameFromToken(token);
            request.setAttribute(USERNAME_ATTRIBUTE, username);
            request.setAttribute(USER_ID_ATTRIBUTE, userId);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\"}");
            return false;
        }
        
        return true;
    }
}
