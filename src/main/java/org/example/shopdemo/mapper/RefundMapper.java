package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.Refund;
import java.util.List;

/**
 * 退款数据访问层接口
 * 提供退款相关的数据库操作方法
 * 使用MyBatis注解方式定义SQL语句
 */
@Mapper
public interface RefundMapper {
    
    /**
     * 插入新的退款记录
     * 创建退款申请时调用，自动生成主键ID
     * @param refund 退款实体对象，包含退款信息
     * @return 影响的行数，成功返回1
     */
    @Insert("INSERT INTO refund (user_id, order_id, order_item_id, refund_no, reason, refund_amount, status, refund_method, refund_account, create_time, update_time) " +
            "VALUES (#{userId}, #{orderId}, #{orderItemId}, #{refundNo}, #{reason}, #{refundAmount}, #{status}, #{refundMethod}, #{refundAccount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Refund refund);
    
    /**
     * 根据ID查询退款记录
     * @param id 退款记录ID
     * @return 退款实体对象，如果不存在返回null
     */
    @Select("SELECT * FROM refund WHERE id = #{id}")
    Refund getById(Long id);
    
    /**
     * 根据订单ID查询退款记录
     * 用于查询某个订单的所有退款申请
     * @param orderId 订单ID
     * @return 退款记录列表，按创建时间倒序排列
     */
    @Select("SELECT * FROM refund WHERE order_id = #{orderId} ORDER BY create_time DESC")
    List<Refund> getByOrderId(Long orderId);
    
    /**
     * 根据用户ID查询退款记录
     * 用于查询某个用户的所有退款申请
     * @param userId 用户ID
     * @return 退款记录列表，按创建时间倒序排列
     */
    @Select("SELECT * FROM refund WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Refund> getByUserId(Long userId);
    
    /**
     * 根据退款单号查询退款记录
     * 退款单号是唯一标识，用于用户查询退款状态
     * @param refundNo 退款单号
     * @return 退款实体对象，如果不存在返回null
     */
    @Select("SELECT * FROM refund WHERE refund_no = #{refundNo}")
    Refund getByRefundNo(String refundNo);
    
    /**
     * 更新退款状态和拒绝原因
     * 用于管理员审核退款时更新状态
     * @param refund 退款实体对象，包含新的状态和拒绝原因
     * @return 影响的行数，成功返回1
     */
    @Update("UPDATE refund SET status = #{status}, reject_reason = #{rejectReason}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(Refund refund);
    
    /**
     * 更新退款方式和退款账户
     * 用于财务处理退款时更新退款信息
     * @param refund 退款实体对象，包含退款方式和账户信息
     * @return 影响的行数，成功返回1
     */
    @Update("UPDATE refund SET refund_method = #{refundMethod}, refund_account = #{refundAccount}, update_time = NOW() WHERE id = #{id}")
    int updateRefundInfo(Refund refund);
    
    /**
     * 根据状态查询退款记录
     * 用于管理员查询特定状态的退款列表
     * @param status 退款状态
     * @return 退款记录列表，按创建时间倒序排列
     */
    @Select("SELECT * FROM refund WHERE status = #{status} ORDER BY create_time DESC")
    List<Refund> getByStatus(String status);
    
    /**
     * 查询所有退款记录
     * 用于管理员查询所有退款数据
     * @return 所有退款记录列表，按创建时间倒序排列
     */
    @Select("SELECT * FROM refund ORDER BY create_time DESC")
    List<Refund> getAll();
    
    /**
     * 检查订单商品是否已申请退款（非拒绝状态）
     * 用于防止同一商品重复申请退款
     * @param orderItemId 订单商品ID
     * @return 如果存在未拒绝的退款记录返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM refund WHERE order_item_id = #{orderItemId} AND status != '审核拒绝'")
    boolean hasActiveRefundByOrderItemId(Long orderItemId);
    
    /**
     * 检查订单是否已申请退款（非拒绝状态）
     * 用于防止同一订单重复申请退款
     * @param orderId 订单ID
     * @return 如果存在未拒绝的退款记录返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM refund WHERE order_id = #{orderId} AND order_item_id IS NULL AND status != '审核拒绝'")
    boolean hasActiveRefundByOrderId(Long orderId);
}