package org.example.shopdemo.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    /**
     * 普通用户
     */
    USER(0, "普通用户"),
    
    /**
     * 管理员
     */
    ADMIN(1, "管理员");

    private final Integer code;
    private final String description;

    UserRole(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public Integer getCodeValue() {
        return code;
    }

    /**
     * 根据角色码获取枚举
     * @param code 角色码
     * @return 用户角色枚举
     */
    public static UserRole fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的用户角色码: " + code);
    }

    /**
     * 检查是否为管理员
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 检查是否为普通用户
     * @return 是否为普通用户
     */
    public boolean isUser() {
        return this == USER;
    }
}
