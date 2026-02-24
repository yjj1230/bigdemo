package org.example.shopdemo.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * 用于标记需要管理员权限才能访问的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
    
    /**
     * 权限提示信息
     * 默认为"需要管理员权限"
     */
    String message() default "需要管理员权限";
}
