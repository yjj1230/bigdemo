package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.Refund;
import java.util.List;

@Mapper
public interface RefundMapper {
    
    @Insert("INSERT INTO refund (order_id, order_item_id, refund_no, reason, refund_amount, status, refund_method, refund_account, create_time, update_time) " +
            "VALUES (#{orderId}, #{orderItemId}, #{refundNo}, #{reason}, #{refundAmount}, #{status}, #{refundMethod}, #{refundAccount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Refund refund);
    
    @Select("SELECT * FROM refund WHERE id = #{id}")
    Refund getById(Long id);
    
    @Select("SELECT * FROM refund WHERE order_id = #{orderId} ORDER BY create_time DESC")
    List<Refund> getByOrderId(Long orderId);
    
    @Select("SELECT * FROM refund WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Refund> getByUserId(Long userId);
    
    @Select("SELECT * FROM refund WHERE refund_no = #{refundNo}")
    Refund getByRefundNo(String refundNo);
    
    @Update("UPDATE refund SET status = #{status}, reject_reason = #{rejectReason}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(Refund refund);
    
    @Update("UPDATE refund SET refund_method = #{refundMethod}, refund_account = #{refundAccount}, update_time = NOW() WHERE id = #{id}")
    int updateRefundInfo(Refund refund);
}