package org.example.shopdemo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),
    
    /**
     * 已支付
     */
    PAID(1, "已支付"),
    
    /**
     * 已发货
     */
    SHIPPED(2, "已发货"),
    
    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),
    
    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    public Integer getCodeValue() {
        return code;
    }

    /**
     * 根据状态码获取枚举
     * @param code 状态码
     * @return 订单状态枚举
     */
    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态码: " + code);
    }

    /**
     * 检查状态是否可以取消
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING_PAYMENT;
    }

    /**
     * 检查状态是否可以支付
     * @return 是否可以支付
     */
    public boolean canPay() {
        return this == PENDING_PAYMENT;
    }

    /**
     * 检查状态是否可以发货
     * @return 是否可以发货
     */
    public boolean canShip() {
        return this == PAID;
    }

    /**
     * 检查状态是否可以完成
     * @return 是否可以完成
     */
    public boolean canFinish() {
        return this == SHIPPED;
    }

    /**
     * 检查状态是否可以评价
     * @return 是否可以评价
     */
    public boolean canReview() {
        return this == COMPLETED;
    }

    /**
     * 检查是否为最终状态
     * @return 是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED;
    }
}
