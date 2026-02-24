package org.example.shopdemo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    /**
     * 正常
     */
    NORMAL(1, "正常"),
    
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    
    /**
     * 已删除
     */
    DELETED(-1, "已删除");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
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
     * @return 用户状态枚举
     */
    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的用户状态码: " + code);
    }

    /**
     * 检查用户是否可以登录
     * @return 是否可以登录
     */
    public boolean canLogin() {
        return this == NORMAL;
    }

    /**
     * 检查用户是否正常
     * @return 是否正常
     */
    public boolean isNormal() {
        return this == NORMAL;
    }

    /**
     * 检查用户是否被禁用
     * @return 是否被禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

    /**
     * 检查用户是否已删除
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}
