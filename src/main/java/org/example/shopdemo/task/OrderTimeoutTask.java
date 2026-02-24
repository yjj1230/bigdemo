package org.example.shopdemo.task;

import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.enums.OrderStatus;
import org.example.shopdemo.mapper.OrderMapper;
import org.example.shopdemo.mapper.OrderItemMapper;
import org.example.shopdemo.service.StockService;
import org.example.shopdemo.service.WebSocketMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务
 * 定期检查超过15分钟未支付的订单并自动取消
 */
@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    private static final int ORDER_TIMEOUT_MINUTES = 15;

    /**
     * 定时任务：每5分钟执行一次，检查超时订单
     * cron表达式：0 0/5 * * * ? 表示每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    public void cancelTimeoutOrders() {
        log.info("开始执行订单超时检查任务");
        
        try {
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
            List<Order> timeoutOrders = orderMapper.findTimeoutOrders(timeoutTime);
            
            if (timeoutOrders.isEmpty()) {
                log.info("没有超时订单需要处理");
                return;
            }
            
            log.info("发现 {} 个超时订单", timeoutOrders.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (Order order : timeoutOrders) {
                try {
                    cancelOrder(order);
                    successCount++;
                    log.info("订单 {} 超时取消成功", order.getOrderNo());
                } catch (Exception e) {
                    failCount++;
                    log.error("订单 {} 超时取消失败", order.getOrderNo(), e);
                }
            }
            
            log.info("订单超时检查任务完成 - 成功: {}, 失败: {}", successCount, failCount);
            
        } catch (Exception e) {
            log.error("订单超时检查任务执行异常", e);
        }
    }

    /**
     * 取消订单并回滚库存
     * @param order 订单对象
     */
    private void cancelOrder(Order order) {
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.update(order);
        List<org.example.shopdemo.entity.OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        for (org.example.shopdemo.entity.OrderItem item : items) {
            stockService.rollbackStock(item.getProductId(), item.getQuantity());
        }
        
        webSocketMessageService.sendOrderStatusUpdate(order.getUserId(), order.getId(), "超时已取消");
    }

    /**
     * 手动触发超时订单检查（用于测试）
     */
    public void manualCheckTimeoutOrders() {
        log.info("手动触发订单超时检查");
        cancelTimeoutOrders();
    }
}
