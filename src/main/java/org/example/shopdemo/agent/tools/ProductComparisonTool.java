package org.example.shopdemo.agent.tools;

import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductComparisonTool {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    public Product getProductById(Long productId) {
        return productService.getProductById(productId);
    }

    public Map<Long, Integer> getUserPurchaseCount(Long userId) {
        List<Order> userOrders = orderService.getUserOrders(userId);
        Map<Long, Integer> purchaseCount = new HashMap<>();
        
        for (Order order : userOrders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                purchaseCount.put(item.getProductId(), 
                        purchaseCount.getOrDefault(item.getProductId(), 0) + item.getQuantity());
            }
        }
        
        return purchaseCount;
    }

    public String getCategoryName(Long categoryId) {
        if (categoryId == null) return "未分类";
        return productService.getCategoryName(categoryId);
    }
}