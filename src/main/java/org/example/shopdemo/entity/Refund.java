package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Refund {
    private Long id;
    
    private Long orderId;
    
    private Long orderItemId;
    
    private String refundNo;
    
    private String reason;
    
    private BigDecimal refundAmount;
    
    private String status;
    
    private String rejectReason;
    
    private String refundMethod;
    
    private String refundAccount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}