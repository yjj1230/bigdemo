package org.example.shopdemo.dto;

import lombok.Data;
import org.example.shopdemo.entity.Cart;
import org.example.shopdemo.entity.Product;

/**
 * 购物车DTO
 * 包含购物车和商品信息
 */
@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Product product;
    private String createTime;
    private String updateTime;

    public static CartDTO fromCartAndProduct(Cart cart, Product product) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setProductId(cart.getProductId());
        dto.setQuantity(cart.getQuantity());
        dto.setProduct(product);
        dto.setCreateTime(cart.getCreateTime() != null ? cart.getCreateTime().toString() : null);
        dto.setUpdateTime(cart.getUpdateTime() != null ? cart.getUpdateTime().toString() : null);
        return dto;
    }
}
