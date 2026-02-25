package org.example.shopdemo.agent.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Agent输入验证器
 * 验证用户输入的安全性和有效性
 */
@Component
public class AgentInputValidator {
    
    /**
     * 最大消息长度
     */
    private static final int MAX_MESSAGE_LENGTH = 1000;
    
    /**
     * 最小消息长度
     */
    private static final int MIN_MESSAGE_LENGTH = 1;
    
    /**
     * 恶意内容模式
     */
    private static final Pattern[] MALICIOUS_PATTERNS = {
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("document\\.", Pattern.CASE_INSENSITIVE),
        Pattern.compile("window\\.", Pattern.CASE_INSENSITIVE)
    };
    
    /**
     * 验证用户消息
     * @param message 用户消息
     * @return 验证结果
     */
    public ValidationResult validateMessage(String message) {
        if (message == null) {
            return ValidationResult.error("消息不能为空");
        }
        
        // 检查消息长度
        if (message.length() < MIN_MESSAGE_LENGTH) {
            return ValidationResult.error("消息不能为空");
        }
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.error("消息过长，请缩短后重试（最多" + MAX_MESSAGE_LENGTH + "个字符）");
        }
        
        // 检查是否只包含空白字符
        if (message.trim().isEmpty()) {
            return ValidationResult.error("消息不能为空");
        }
        
        // 检查恶意内容
        for (Pattern pattern : MALICIOUS_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return ValidationResult.error("消息包含不当内容，请重新输入");
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证订单号格式
     * @param orderNo 订单号
     * @return 验证结果
     */
    public ValidationResult validateOrderNo(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return ValidationResult.error("订单号不能为空");
        }
        
        // 订单号应该是字母加数字，至少10位
        if (!orderNo.matches("^[A-Za-z]*\\d{10,}$")) {
            return ValidationResult.error("订单号格式不正确");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证搜索关键词
     * @param keyword 关键词
     * @return 验证结果
     */
    public ValidationResult validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ValidationResult.error("搜索关键词不能为空");
        }
        
        if (keyword.length() > 100) {
            return ValidationResult.error("搜索关键词过长");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
