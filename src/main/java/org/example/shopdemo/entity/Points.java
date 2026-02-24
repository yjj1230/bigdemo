package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Points {
    private Long id;
    
    private Long userId;
    
    private Integer points;
    
    private String type;
    
    private String description;
    
    private Long relatedOrderId;
    
    private LocalDateTime createTime;
}