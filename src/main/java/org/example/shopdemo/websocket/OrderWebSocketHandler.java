package org.example.shopdemo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 * 处理订单状态更新的实时推送
 */
@Slf4j
@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket连接建立 - 用户ID: {}, Session ID: {}", userId, session.getId());
            
            sendMessageToUser(userId, "连接成功，可以接收订单状态更新");
        }
    }

    /**
     * 收到消息后调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserIdFromSession(session);
        log.info("收到WebSocket消息 - 用户ID: {}, 消息: {}", userId, message.getPayload());
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket连接关闭 - 用户ID: {}, Session ID: {}, 状态: {}", 
                    userId, session.getId(), status);
        }
    }

    /**
     * 传输错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserIdFromSession(session);
        log.error("WebSocket传输错误 - 用户ID: {}, 错误: {}", userId, exception.getMessage(), exception);
        
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 向指定用户发送消息
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("WebSocket消息发送成功 - 用户ID: {}, 消息: {}", userId, message);
            } catch (IOException e) {
                log.error("WebSocket消息发送失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
                sessions.remove(userId);
            }
        } else {
            log.warn("WebSocket会话不存在或已关闭 - 用户ID: {}", userId);
        }
    }

    /**
     * 向指定用户发送订单状态更新消息
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param status 订单状态
     */
    public void sendOrderStatusUpdate(String userId, Long orderId, String status) {
        try {
            String message = objectMapper.writeValueAsString(new OrderStatusMessage(orderId, status));
            sendMessageToUser(userId, message);
        } catch (Exception e) {
            log.error("订单状态更新消息发送失败 - 用户ID: {}, 订单ID: {}, 错误: {}", 
                    userId, orderId, e.getMessage(), e);
        }
    }

    /**
     * 从会话中获取用户ID
     * @param session WebSocket会话
     * @return 用户ID
     */
    private String getUserIdFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("userId");
    }

    /**
     * 订单状态消息类
     */
    public static class OrderStatusMessage {
        private Long orderId;
        private String status;
        private String message;

        public OrderStatusMessage(Long orderId, String status) {
            this.orderId = orderId;
            this.status = status;
            this.message = "订单状态已更新";
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
