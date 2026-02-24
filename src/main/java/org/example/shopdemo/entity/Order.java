package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
public class Order {
    /** 订单ID */
    private Long id;
    /** 订单编号 */
    private String orderNo;
    /** 用户ID */
    private Long userId;
    /** 优惠券ID */
    private Long couponId;
    /** 原始金额 */
    private BigDecimal originalAmount;
    /** 优惠券折扣金额 */
    private BigDecimal couponDiscount;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 实付金额 */
    private BigDecimal payAmount;
    /** 订单状态 */
    private Integer status;
    /** 收货人姓名 */
    private String receiverName;
    /** 收货人电话 */
    private String receiverPhone;
    /** 收货地址 */
    private String receiverAddress;
    /** 订单备注 */
    private String remark;
    /** 支付时间 */
    private LocalDateTime payTime;
    /** 发货时间 */
    private LocalDateTime shipTime;
    /** 完成时间 */
    private LocalDateTime finishTime;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
