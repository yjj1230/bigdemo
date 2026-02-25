package org.example.shopdemo.config;

import org.example.shopdemo.websocket.OrderWebSocketHandler;
import org.example.shopdemo.websocket.RefundWebSocketHandler;
import org.example.shopdemo.websocket.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 配置WebSocket端点和拦截器
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private OrderWebSocketHandler orderWebSocketHandler;

    @Autowired
    private RefundWebSocketHandler refundWebSocketHandler;

    @Autowired
    private WebSocketInterceptor webSocketInterceptor;

    /**
     * 注册WebSocket处理器
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderWebSocketHandler, "/ws/order")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
        registry.addHandler(refundWebSocketHandler, "/ws/refund")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
