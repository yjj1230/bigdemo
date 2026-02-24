package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 */
@Data
public class Coupon {
    /** 优惠券ID */
    private Long id;
    /** 优惠券名称 */
    private String name;
    /** 优惠券描述 */
    private String description;
    /** 优惠券类型：1-满减券，2-折扣券，3-免运费券 */
    private Integer type;
    /** 满减金额 */
    private BigDecimal discountAmount;
    /** 折扣比例（如0.8表示8折） */
    private BigDecimal discountRate;
    /** 最低消费金额 */
    private BigDecimal minAmount;
    /** 最高优惠金额 */
    private BigDecimal maxDiscount;
    /** 总发行数量 */
    private Integer totalCount;
    /** 已领取数量 */
    private Integer receivedCount;
    /** 已使用数量 */
    private Integer usedCount;
    /** 每人限领数量 */
    private Integer limitPerUser;
    /** 有效期开始时间 */
    private LocalDateTime startTime;
    /** 有效期结束时间 */
    private LocalDateTime endTime;
    /** 状态：1-未开始，2-进行中，3-已结束 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
