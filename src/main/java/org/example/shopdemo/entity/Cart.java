package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
public class Cart {
    /** 购物车ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 商品ID */
    private Long productId;
    /** 商品数量 */
    private Integer quantity;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
