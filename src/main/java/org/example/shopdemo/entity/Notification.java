package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    
    private Long userId;
    
    private String type;
    
    private String title;
    
    private String content;
    
    private Boolean isRead;
    
    private Long relatedOrderId;
    
    private LocalDateTime createTime;
}