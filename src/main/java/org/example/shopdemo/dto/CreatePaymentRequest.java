package org.example.shopdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付请求DTO
 */
@Data
@Schema(description = "创建支付请求")
public class CreatePaymentRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", required = true)
    private Long orderId;

    @NotBlank(message = "商品描述不能为空")
    @Schema(description = "商品描述", required = true, example = "测试商品")
    private String description;

    @NotNull(message = "支付金额不能为空")
    @Schema(description = "支付金额（元）", required = true, example = "0.01")
    private BigDecimal amount;

    @NotBlank(message = "用户openid不能为空")
    @Schema(description = "用户openid", required = true)
    private String openid;
}
