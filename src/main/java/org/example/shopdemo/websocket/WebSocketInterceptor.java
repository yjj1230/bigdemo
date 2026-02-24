package org.example.shopdemo.websocket;

import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.utils.jwtutil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket拦截器
 * 在握手时验证用户身份
 */
@Component
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    /**
     * 握手前调用，用于验证用户身份
     * @param request 请求
     * @param response 响应
     * @param wsHandler WebSocket处理器
     * @param attributes 属性
     * @return 是否允许握手
     * @throws Exception 异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket握手失败：缺少token参数");
                return false;
            }
            
            try {
                Long userId = jwtutil.getUserIdFromToken(token);
                if (userId != null) {
                    attributes.put("userId", String.valueOf(userId));
                    log.info("WebSocket握手成功 - 用户ID: {}", userId);
                    return true;
                } else {
                    log.warn("WebSocket握手失败：token无效");
                    return false;
                }
            } catch (Exception e) {
                log.error("WebSocket握手失败：token验证异常 - {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * 握手后调用
     * @param request 请求
     * @param response 响应
     * @param wsHandler WebSocket处理器
     * @param exception 异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手后异常 - {}", exception.getMessage());
        }
    }
}
