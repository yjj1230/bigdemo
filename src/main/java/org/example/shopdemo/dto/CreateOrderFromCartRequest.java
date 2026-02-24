package org.example.shopdemo.dto;

import lombok.Data;

/**
 * 从购物车创建订单请求DTO
 */
@Data
public class CreateOrderFromCartRequest {
    private Long addressId;
    private Long couponId;
    private String remark;
}
