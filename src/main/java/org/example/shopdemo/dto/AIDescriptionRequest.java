package org.example.shopdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * AI商品描述生成请求DTO
 */
@Schema(description = "AI商品描述生成请求")
public class AIDescriptionRequest {

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "iPhone 15 Pro Max")
    private String productName;

    @Schema(description = "商品特点列表", example = "[\"钛金属材质\", \"A17芯片\", \"4800万像素摄像头\"]")
    private List<String> features;

    @Schema(description = "目标用户群体", example = "商务人士、科技爱好者")
    private String targetAudience;

    @Schema(description = "商品卖点", example = "轻薄耐用、性能强劲")
    private String sellingPoints;

    @Schema(description = "描述风格", example = "专业、活泼、简洁", allowableValues = {"专业", "活泼", "简洁"})
    private String style;

    @Schema(description = "是否包含emoji", example = "true")
    private Boolean includeEmoji = true;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getSellingPoints() {
        return sellingPoints;
    }

    public void setSellingPoints(String sellingPoints) {
        this.sellingPoints = sellingPoints;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Boolean getIncludeEmoji() {
        return includeEmoji;
    }

    public void setIncludeEmoji(Boolean includeEmoji) {
        this.includeEmoji = includeEmoji;
    }
}
