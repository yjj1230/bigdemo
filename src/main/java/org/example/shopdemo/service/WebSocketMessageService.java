package org.example.shopdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.enums.OrderStatus;
import org.example.shopdemo.websocket.OrderWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * WebSocket消息服务
 * 统一管理WebSocket消息的发送
 */
@Slf4j
@Service
public class WebSocketMessageService {

    //注入OrderWebSocketHandler处理器
    @Autowired
    private OrderWebSocketHandler orderWebSocketHandler;

    /**
     * 发送订单状态更新通知
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param status 订单状态
     */
    public void sendOrderStatusUpdate(String userId, Long orderId, String status) {
        try {
            orderWebSocketHandler.sendOrderStatusUpdate(userId, orderId, status);
            log.info("订单状态更新通知已发送 - 用户ID: {}, 订单ID: {}, 状态: {}", 
                    userId, orderId, status);
        } catch (Exception e) {
            log.error("订单状态更新通知发送失败 - 用户ID: {}, 订单ID: {}, 错误: {}", 
                    userId, orderId, e.getMessage(), e);
        }
    }

    /**
     * 发送订单状态更新通知（使用Long类型的userId）
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param status 订单状态
     */
    public void sendOrderStatusUpdate(Long userId, Long orderId, String status) {
        sendOrderStatusUpdate(String.valueOf(userId), orderId, status);
    }

    /**
     * 发送自定义消息给指定用户
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendMessageToUser(String userId, String message) {
        try {
            orderWebSocketHandler.sendMessageToUser(userId, message);
            log.info("自定义消息已发送 - 用户ID: {}, 消息: {}", userId, message);
        } catch (Exception e) {
            log.error("自定义消息发送失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 发送自定义消息给指定用户（使用Long类型的userId）
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendMessageToUser(Long userId, String message) {
        sendMessageToUser(String.valueOf(userId), message);
    }

    /**
     * 发送订单状态更新通知（直接发送，不使用消息队列）
     * @param userId 用户ID
     * @param order 订单对象
     */
    public void sendOrderStatusUpdate(Long userId, Order order) {
        try {
            OrderStatus status = OrderStatus.fromCode(order.getStatus());
            String statusText = status != null ? status.getDescription() : "未知状态";
            orderWebSocketHandler.sendOrderStatusUpdate(
                    String.valueOf(userId), 
                    order.getId(), 
                    statusText
            );
            log.info("订单状态更新通知已发送 - 用户ID: {}, 订单ID: {}, 状态: {}", 
                    userId, order.getId(), statusText);
        } catch (Exception e) {
            log.error("订单状态更新通知发送失败 - 用户ID: {}, 订单ID: {}, 错误: {}", 
                    userId, order.getId(), e.getMessage(), e);
        }
    }
}
