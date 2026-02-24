package org.example.shopdemo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 商品状态枚举
 */
public enum ProductStatus {
    /**
     * 上架
     */
    ON_SALE(1, "上架"),
    
    /**
     * 下架
     */
    OFF_SALE(0, "下架"),
    
    /**
     * 已删除
     */
    DELETED(-1, "已删除");

    private final Integer code;
    private final String description;

    ProductStatus(Integer code, String description) {
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
     * @return 商品状态枚举
     */
    public static ProductStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus status : ProductStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的商品状态码: " + code);
    }

    /**
     * 检查商品是否可购买
     * @return 是否可购买
     */
    public boolean canBuy() {
        return this == ON_SALE;
    }

    /**
     * 检查是否为上架状态
     * @return 是否为上架状态
     */
    public boolean isOnSale() {
        return this == ON_SALE;
    }

    /**
     * 检查是否为下架状态
     * @return 是否为下架状态
     */
    public boolean isOffSale() {
        return this == OFF_SALE;
    }

    /**
     * 检查是否已删除
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}
