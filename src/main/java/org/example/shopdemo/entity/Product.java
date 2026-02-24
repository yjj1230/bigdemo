package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
public class Product {
    /** 商品ID */
    private Long id;
    /** 商品名称 */
    private String name;
    /** 商品描述 */
    private String description;
    /** 分类ID */
    private Long categoryId;
    /** 商品价格 */
    private BigDecimal price;
    /** 原价 */
    private BigDecimal originalPrice;
    /** 库存数量 */
    private Integer stock;
    /** 销量 */
    private Integer sales;
    /** 主图URL */
    private String mainImage;
    /** 商品图片 */
    private String images;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
