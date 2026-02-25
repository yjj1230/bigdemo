package org.example.shopdemo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 退款WebSocket处理器
 * 用于向用户推送退款状态更新通知
 */
@Component
public class RefundWebSocketHandler extends TextWebSocketHandler {

    /**
     * 用户会话映射表
     * 键：用户ID，值：WebSocket会话
     */
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 建立连接
     * @param session WebSocket会话
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            System.out.println("用户 " + userId + " 连接到退款WebSocket");
        } else {
            System.err.println("退款WebSocket连接失败：无法获取用户ID，session attributes: " + session.getAttributes());
        }
    }

    /**
     * 接收消息
     * @param session WebSocket会话
     * @param message 接收到的消息
     * @throws Exception 异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到退款WebSocket消息：" + payload);
    }

    /**
     * 连接关闭
     * @param session WebSocket会话
     * @param status 关闭状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("用户 " + userId + " 断开退款WebSocket连接");
        }
    }

    /**
     * 连接出错
     * @param session WebSocket会话
     * @param exception 异常
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            System.err.println("用户 " + userId + " 退款WebSocket连接出错：" + exception.getMessage());
        }
    }

    /**
     * 向指定用户发送退款状态更新通知
     * @param userId 用户ID
     * @param refund 退款信息
     */
    public static void sendRefundUpdate(Long userId, Object refund) {
        System.out.println("尝试发送退款更新通知 - userId: " + userId + ", 当前在线用户数: " + userSessions.size());
        System.out.println("当前在线用户ID列表: " + userSessions.keySet());
        
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String message = mapper.writeValueAsString(Map.of(
                    "type", "refund_update",
                    "data", refund
                ));
                session.sendMessage(new TextMessage(message));
                System.out.println("✓ 成功向用户 " + userId + " 发送退款更新通知");
            } catch (IOException e) {
                System.err.println("✗ 发送退款更新通知失败：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("✗ 用户 " + userId + " 未在线或会话已关闭");
        }
    }

    /**
     * 从会话中获取用户ID
     * 优先从session attributes中获取（由WebSocketInterceptor设置）
     * 如果没有则从URL参数中获取
     * @param session WebSocket会话
     * @return 用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 优先从session attributes中获取（由WebSocketInterceptor设置）
            Object userIdObj = session.getAttributes().get("userId");
            if (userIdObj != null) {
                return Long.parseLong(userIdObj.toString());
            }
            
            // 备用方案：从URL参数中获取
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userIdStr = query.substring(query.indexOf("userId=") + 7);
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            System.err.println("获取用户ID失败：" + e.getMessage());
        }
        return null;
    }
}
