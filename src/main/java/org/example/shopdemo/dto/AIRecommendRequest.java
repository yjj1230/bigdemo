package org.example.shopdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * AI商品推荐请求DTO
 */
@Schema(description = "AI商品推荐请求")
public class AIRecommendRequest {

    @NotBlank(message = "用户偏好不能为空")
    @Schema(description = "用户偏好描述", example = "喜欢性价比高、轻便的电子产品")
    private String userPreferences;

    @NotBlank(message = "商品分类不能为空")
    @Schema(description = "商品分类", example = "手机")
    private String category;

    @Schema(description = "价格范围", example = "3000-5000")
    private String priceRange;

    @Schema(description = "品牌偏好", example = "苹果、华为")
    private String brandPreferences;

    @Schema(description = "其他要求", example = "需要5G功能")
    private String otherRequirements;

    public String getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getBrandPreferences() {
        return brandPreferences;
    }

    public void setBrandPreferences(String brandPreferences) {
        this.brandPreferences = brandPreferences;
    }

    public String getOtherRequirements() {
        return otherRequirements;
    }

    public void setOtherRequirements(String otherRequirements) {
        this.otherRequirements = otherRequirements;
    }
}
