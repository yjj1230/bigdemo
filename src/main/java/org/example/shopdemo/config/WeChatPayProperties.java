package org.example.shopdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性类
 * 从application.yml中读取微信支付相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WeChatPayProperties {

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户API私钥路径
     */
    private String privateKeyPath;

    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 商户API v3密钥
     */
    private String apiV3Key;

    /**
     * 微信支付平台证书路径
     */
    private String platformCertPath;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 通知URL
     */
    private String notifyUrl;

    /**
     * 支付成功跳转URL
     */
    private String returnUrl;
}
