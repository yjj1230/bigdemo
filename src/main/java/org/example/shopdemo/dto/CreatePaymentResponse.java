package org.example.shopdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建支付响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建支付响应")
public class CreatePaymentResponse {

    @Schema(description = "预支付交易会话标识")
    private String prepayId;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "时间戳")
    private String timeStamp;

    @Schema(description = "随机字符串")
    private String nonceStr;

    @Schema(description = "订单详情扩展字符串")
    private String packageValue;

    @Schema(description = "签名方式")
    private String signType;

    @Schema(description = "签名")
    private String paySign;

    public static CreatePaymentResponse success(String prepayId) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setPrepayId(prepayId);
        return response;
    }
}
