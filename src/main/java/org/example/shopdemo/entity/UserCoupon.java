package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
public class UserCoupon {
    /** 用户优惠券ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 优惠券ID */
    private Long couponId;
    /** 优惠券名称 */
    private String couponName;
    /** 优惠券类型：1-满减券，2-折扣券，3-免运费券 */
    private Integer type;
    /** 满减金额 */
    private java.math.BigDecimal discountAmount;
    /** 折扣比例 */
    private java.math.BigDecimal discountRate;
    /** 最低消费金额 */
    private java.math.BigDecimal minAmount;
    /** 最高优惠金额 */
    private java.math.BigDecimal maxDiscount;
    /** 有效期开始时间 */
    private LocalDateTime startTime;
    /** 有效期结束时间 */
    private LocalDateTime endTime;
    /** 状态：1-未使用，2-已使用，3-已过期 */
    private Integer status;
    /** 使用时间 */
    private LocalDateTime usedTime;
    /** 领取时间 */
    private LocalDateTime receiveTime;
    /** 订单ID */
    private Long orderId;
}
