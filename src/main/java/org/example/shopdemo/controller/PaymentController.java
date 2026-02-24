package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.config.WeChatPayProperties;
import org.example.shopdemo.dto.CreatePaymentRequest;
import org.example.shopdemo.dto.CreatePaymentResponse;
import org.example.shopdemo.dto.CreateRefundRequest;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.enums.OrderStatus;
import org.example.shopdemo.mapper.OrderMapper;
import org.example.shopdemo.service.WeChatPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * 支付控制器
 * 处理微信支付相关的请求
 */
@Slf4j
@Tag(name = "支付管理", description = "微信支付相关接口")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private WeChatPayService weChatPayService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WeChatPayProperties weChatPayProperties;

    /**
     * 创建支付订单
     * @param request 支付请求
     * @return 支付响应
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付订单", description = "创建微信支付订单并返回支付参数")
    public CreatePaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("创建支付订单 - 订单ID: {}, 金额: {}", request.getOrderId(), request.getAmount());

        Order order = orderMapper.findById(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("订单状态异常，无法支付");
        }

        String prepayId = weChatPayService.createPayment(
                request.getOrderId(),
                request.getDescription(),
                request.getAmount(),
                request.getOpenid()
        );

        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setPrepayId(prepayId);
        response.setAppId(weChatPayProperties.getAppId());
        response.setTimeStamp(String.valueOf(Instant.now().getEpochSecond()));
        response.setNonceStr(generateNonceStr());
        response.setPackageValue("prepay_id=" + prepayId);
        response.setSignType("RSA");

        log.info("支付订单创建成功 - 订单ID: {}, 预支付ID: {}", request.getOrderId(), prepayId);

        return response;
    }

    /**
     * 查询支付状态
     * @param orderId 订单ID
     * @return 支付状态
     */
    @GetMapping("/status/{orderId}")
    @Operation(summary = "查询支付状态", description = "查询微信支付订单的状态")
    public String queryPaymentStatus(@PathVariable Long orderId) {
        log.info("查询支付状态 - 订单ID: {}", orderId);
        return weChatPayService.queryPaymentStatus(orderId.toString());
    }

    /**
     * 申请退款
     * @param request 退款请求
     * @return 退款结果
     */
    @PostMapping("/refund")
    @Operation(summary = "申请退款", description = "申请订单退款")
    public String createRefund(@Valid @RequestBody CreateRefundRequest request) {
        log.info("申请退款 - 订单ID: {}, 退款金额: {}", request.getOrderId(), request.getAmount());

        Order order = orderMapper.findById(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new RuntimeException("订单状态异常，无法退款");
        }

        weChatPayService.createRefund(
                request.getOrderId(),
                request.getRefundId(),
                request.getAmount(),
                request.getReason()
        );

        return "退款申请成功";
    }

    /**
     * 微信支付结果通知
     * @param notifyData 通知数据
     * @return 处理结果
     */
    @PostMapping("/notify")
    @Operation(summary = "支付结果通知", description = "接收微信支付结果通知")
    public String paymentNotify(@RequestBody String notifyData) {
        log.info("收到微信支付通知 - 数据: {}", notifyData);

        try {
            String outTradeNo = extractOutTradeNo(notifyData);
            if (outTradeNo != null) {
                Long orderId = Long.parseLong(outTradeNo);
                Order order = orderMapper.findById(orderId);
                
                if (order != null && order.getStatus() == OrderStatus.PENDING_PAYMENT.getCode()) {
                    order.setStatus(OrderStatus.PAID.getCode());
                    order.setPayTime(java.time.LocalDateTime.now());
                    orderMapper.update(order);
                    log.info("订单支付成功 - 订单ID: {}", orderId);
                }
            }

            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";

        } catch (Exception e) {
            log.error("处理支付通知失败", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    /**
     * 生成随机字符串
     * @return 随机字符串
     */
    private String generateNonceStr() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 从通知数据中提取订单号
     * @param notifyData 通知数据
     * @return 订单号
     */
    private String extractOutTradeNo(String notifyData) {
        try {
            int startIndex = notifyData.indexOf("\"out_trade_no\":\"");
            if (startIndex == -1) return null;
            
            startIndex += "\"out_trade_no\":\"".length();
            int endIndex = notifyData.indexOf("\"", startIndex);
            
            if (endIndex == -1) return null;
            
            return notifyData.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.error("提取订单号失败", e);
            return null;
        }
    }
}
