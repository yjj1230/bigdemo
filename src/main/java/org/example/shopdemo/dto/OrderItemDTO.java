package org.example.shopdemo.dto;

import lombok.Data;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;

/**
 * 订单项DTO
 * 包含订单项和商品信息
 */
@Data
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private java.math.BigDecimal price;
    private Integer quantity;
    private java.math.BigDecimal totalPrice;
    private java.time.LocalDateTime createTime;
    private Product product;

    public static OrderItemDTO fromOrderItem(OrderItem orderItem, Product product) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrderId());
        dto.setProductId(orderItem.getProductId());
        dto.setProductName(orderItem.getProductName());
        dto.setProductImage(orderItem.getProductImage());
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setTotalPrice(orderItem.getTotalPrice());
        dto.setCreateTime(orderItem.getCreateTime());
        dto.setProduct(product);
        return dto;
    }
}
