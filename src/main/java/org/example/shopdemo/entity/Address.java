package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户地址实体类
 */
@Data
public class Address {
    /** 地址ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 收货人姓名 */
    private String receiverName;
    /** 收货人电话 */
    private String receiverPhone;
    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区县 */
    private String district;
    /** 详细地址 */
    private String detailAddress;
    /** 是否默认地址 */
    private Integer isDefault;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
