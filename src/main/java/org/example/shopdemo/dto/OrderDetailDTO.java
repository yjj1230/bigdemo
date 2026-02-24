package org.example.shopdemo.dto;

import lombok.Data;
import org.example.shopdemo.entity.Order;

import java.util.List;

/**
 * 订单详情DTO
 * 包含订单信息和订单项列表
 */
@Data
public class OrderDetailDTO {
    private Long id;
    private String orderNo;
    private Long userId;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal payAmount;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private java.time.LocalDateTime payTime;
    private java.time.LocalDateTime shipTime;
    private java.time.LocalDateTime finishTime;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
    private List<OrderItemDTO> items;

    public static OrderDetailDTO fromOrder(Order order, List<OrderItemDTO> items) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setStatus(order.getStatus());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setRemark(order.getRemark());
        dto.setPayTime(order.getPayTime());
        dto.setShipTime(order.getShipTime());
        dto.setFinishTime(order.getFinishTime());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        dto.setItems(items);
        return dto;
    }
}
