package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;
// 物流信息
@Data
public class Logistics {
    private Long id;
    // 订单ID
    private Long orderId;
    // 物流单号
    private String logisticsNo;
    // 物流公司
    private String logisticsCompany;
    // 状态
    private String status;
    // 描述
    private String description;
    //
    private String location;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}