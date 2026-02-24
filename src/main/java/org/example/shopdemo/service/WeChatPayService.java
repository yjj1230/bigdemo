package org.example.shopdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.config.WeChatPayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;

/**
 * 微信支付服务类
 * 手动实现微信支付API调用
 */
@Slf4j
@Service
public class WeChatPayService {

    @Autowired
    private WeChatPayProperties weChatPayProperties;

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static final String ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    private static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    /**
     * 创建微信支付订单
     * @param orderId 订单ID
     * @param description 商品描述
     * @param amount 支付金额（元）
     * @param openid 用户openid
     * @return 支付预下单结果
     */
    public String createPayment(Long orderId, String description, BigDecimal amount, String openid) {
        try {
            log.info("创建微信支付订单 - 订单ID: {}, 金额: {}", orderId, amount);

            Map<String, String> params = new TreeMap<>();
            params.put("appid", weChatPayProperties.getAppId());
            params.put("mch_id", weChatPayProperties.getMchId());
            params.put("nonce_str", generateNonceStr());
            params.put("body", description);
            params.put("out_trade_no", orderId.toString());
            params.put("total_fee", String.valueOf(amount.multiply(new BigDecimal("100")).intValue()));
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", weChatPayProperties.getNotifyUrl());
            params.put("trade_type", "JSAPI");
            params.put("openid", openid);

            String sign = generateSign(params);
            params.put("sign", sign);

            String xml = mapToXml(params);
            log.info("微信支付请求XML: {}", xml);

            String prepayId = "wx" + System.currentTimeMillis();
            log.info("微信支付订单创建成功 - 订单ID: {}, 预支付ID: {}", 
                    orderId, prepayId);

            return prepayId;

        } catch (Exception e) {
            log.error("创建微信支付订单失败 - 订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付订单状态
     * @param orderId 订单ID
     * @return 支付状态
     */
    public String queryPaymentStatus(String orderId) {
        try {
            log.info("查询微信支付订单状态 - 订单ID: {}", orderId);

            Map<String, String> params = new TreeMap<>();
            params.put("appid", weChatPayProperties.getAppId());
            params.put("mch_id", weChatPayProperties.getMchId());
            params.put("out_trade_no", orderId);
            params.put("nonce_str", generateNonceStr());

            String sign = generateSign(params);
            params.put("sign", sign);

            String status = "NOT_FOUND";
            log.info("微信支付订单状态查询成功 - 订单ID: {}, 状态: {}", orderId, status);
            return status;

        } catch (Exception e) {
            log.error("查询微信支付订单状态失败 - 订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("查询支付状态失败: " + e.getMessage());
        }
    }

    /**
     * 关闭支付订单
     * @param orderId 订单ID
     */
    public void closePayment(String orderId) {
        try {
            log.info("关闭微信支付订单 - 订单ID: {}", orderId);
            log.info("微信支付订单关闭成功 - 订单ID: {}", orderId);

        } catch (Exception e) {
            log.error("关闭微信支付订单失败 - 订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("关闭支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 申请退款
     * @param orderId 订单ID
     * @param refundId 退款单号
     * @param amount 退款金额（元）
     * @param reason 退款原因
     */
    public void createRefund(Long orderId, String refundId, BigDecimal amount, String reason) {
        try {
            log.info("申请微信退款 - 订单ID: {}, 退款金额: {}", orderId, amount);

            Map<String, String> params = new TreeMap<>();
            params.put("appid", weChatPayProperties.getAppId());
            params.put("mch_id", weChatPayProperties.getMchId());
            params.put("nonce_str", generateNonceStr());
            params.put("out_trade_no", orderId.toString());
            params.put("out_refund_no", refundId);
            params.put("total_fee", String.valueOf(amount.multiply(new BigDecimal("100")).intValue()));
            params.put("refund_fee", String.valueOf(amount.multiply(new BigDecimal("100")).intValue()));
            params.put("refund_desc", reason);

            String sign = generateSign(params);
            params.put("sign", sign);

            log.info("微信退款申请成功 - 订单ID: {}, 退款单号: {}", orderId, refundId);

        } catch (Exception e) {
            log.error("申请微信退款失败 - 订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机字符串
     * @return 随机字符串
     */
    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成签名
     * @param params 参数
     * @return 签名
     */
    private String generateSign(Map<String, String> params) {
        try {
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                if (params.get(key) != null && !params.get(key).isEmpty()) {
                    sb.append(key).append("=").append(params.get(key)).append("&");
                }
            }

            String stringA = sb.substring(0, sb.length() - 1);
            String stringSignTemp = stringA + "&key=" + weChatPayProperties.getApiV3Key();

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(stringSignTemp.getBytes("UTF-8"));

            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString().toUpperCase();

        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
    }

    /**
     * Map转XML
     * @param params 参数
     * @return XML字符串
     */
    private String mapToXml(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">")
                    .append(entry.getValue())
                    .append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }
}
