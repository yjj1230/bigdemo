package org.example.shopdemo.dto;

import lombok.Data;

/**
 * 订单商品请求DTO
 */
@Data
public class OrderItemRequest {
    /** 商品ID */
    private Long productId;
    /** 购买数量 */
    private Integer quantity;
}
