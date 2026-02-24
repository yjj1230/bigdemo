package org.example.shopdemo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 支付金额 */
    private BigDecimal payAmount;
    /** 优惠券ID */
    private Long couponId;
    /** 收货人姓名 */
    private String receiverName;
    /** 收货人电话 */
    private String receiverPhone;
    /** 收货地址 */
    private String receiverAddress;
    /** 订单备注 */
    private String remark;
    /** 订单商品列表 */
    private List<OrderItemRequest> items;
}
