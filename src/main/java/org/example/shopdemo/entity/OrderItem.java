package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情实体类
 */
@Data
public class OrderItem {
    /** 订单详情ID */
    private Long id;
    /** 订单ID */
    private Long orderId;
    /** 商品ID */
    private Long productId;
    /** 商品名称 */
    private String productName;
    /** 商品图片 */
    private String productImage;
    /** 商品单价 */
    private BigDecimal price;
    /** 购买数量 */
    private Integer quantity;
    /** 小计金额 */
    private BigDecimal totalPrice;
    /** 创建时间 */
    private LocalDateTime createTime;
}
