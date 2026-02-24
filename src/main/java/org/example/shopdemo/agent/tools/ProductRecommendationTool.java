package org.example.shopdemo.agent.tools;

import org.example.shopdemo.dto.AIRecommendRequest;
import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.CartService;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductRecommendationTool {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    public List<Product> searchProducts(String keyword) {
        return productService.searchProducts(keyword);
    }

    public Product getProductById(Long productId) {
        return productService.getProductById(productId);
    }

    public List<Product> getProductsByIds(List<Long> productIds) {
        return productService.getProductsByIds(productIds);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderService.getUserOrders(userId);
    }

    public List<CartDTO> getUserCart(Long userId) {
        return cartService.getUserCart(userId);
    }

    public Map<String, Long> getUserPurchaseHistory(Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        List<Long> allProductIds = new java.util.ArrayList<>();
        
        for (Order order : orders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                allProductIds.add(item.getProductId());
            }
        }
        
        List<Product> allProducts = productService.getProductsByIds(allProductIds);
        Map<Long, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        Map<String, Long> categoryCount = new java.util.HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            for (OrderItem item : items) {
                Product product = productMap.get(item.getProductId());
                if (product != null) {
                    String categoryName = getCategoryName(product.getCategoryId());
                    categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0L) + 1);
                }
            }
        }
        
        return categoryCount;
    }

    public String getCategoryName(Long categoryId) {
        if (categoryId == null) return "未分类";
        return productService.getCategoryName(categoryId);
    }
}