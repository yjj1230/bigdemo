package org.example.shopdemo.agent.tools;

import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderAssistantTool {

    @Autowired
    private OrderService orderService;

    public Order getOrderByOrderNo(String orderNo) {
        return orderService.getOrderByOrderNo(orderNo);
    }

    public Order getOrderById(Long orderId) {
        return orderService.getOrderById(orderId);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    public String getOrderStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}