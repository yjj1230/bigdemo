package org.example.shopdemo.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 添加评价请求DTO
 */
@Data
public class AddReviewRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private String images;

    private Integer isAnonymous;
}
