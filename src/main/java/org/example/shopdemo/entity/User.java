package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 昵称 */
    private String nickname;
    /** 头像 */
    private String avatar;
    /** 角色 */
    private Integer role;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 获取角色字符串（用于前端显示）
     * @return 角色字符串
     */
    public String getRoleString() {
        if (role == null) {
            return null;
        }
        return role == 1 ? "ADMIN" : "USER";
    }
}
