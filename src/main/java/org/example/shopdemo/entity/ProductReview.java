package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品评价实体类
 */
@Data
public class ProductReview {
    /** 评价ID */
    private Long id;
    /** 商品ID */
    private Long productId;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 评分（1-5星） */
    private Integer rating;
    /** 评价内容 */
    private String content;
    /** 评价图片 */
    private String images;
    /** 是否匿名评价 */
    private Integer isAnonymous;
    /** 商家回复 */
    private String reply;
    /** 回复时间 */
    private LocalDateTime replyTime;
    /** 点赞数 */
    private Integer likeCount;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
