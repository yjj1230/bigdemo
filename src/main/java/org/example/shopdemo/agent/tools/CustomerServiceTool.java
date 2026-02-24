package org.example.shopdemo.agent.tools;

import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.User;
import org.example.shopdemo.service.CartService;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerServiceTool {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    public User getUserInfo(Long userId) {
        return userService.getUserInfoById(userId);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderService.getUserOrders(userId);
    }

    public List<CartDTO> getUserCart(Long userId) {
        return cartService.getUserCart(userId);
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