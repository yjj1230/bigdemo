package org.example.shopdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建退款请求DTO
 */
@Data
@Schema(description = "创建退款请求")
public class CreateRefundRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", required = true)
    private Long orderId;

    @NotBlank(message = "退款单号不能为空")
    @Schema(description = "退款单号", required = true)
    private String refundId;

    @NotNull(message = "退款金额不能为空")
    @Schema(description = "退款金额（元）", required = true, example = "0.01")
    private BigDecimal amount;

    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因", required = true, example = "用户申请退款")
    private String reason;
}
