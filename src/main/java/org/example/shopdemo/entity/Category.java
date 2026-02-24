package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
public class Category {
    /** 分类ID */
    private Long id;
    /** 分类名称 */
    private String name;
    /** 父分类ID */
    private Long parentId;
    /** 分类层级 */
    private Integer level;
    /** 分类图标 */
    private String icon;
    /** 排序序号 */
    private Integer sortOrder;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
