package org.example.shopdemo.service;

import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.Refund;
import org.example.shopdemo.mapper.OrderMapper;
import org.example.shopdemo.mapper.RefundMapper;
import org.example.shopdemo.websocket.RefundWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * 退款服务类
 * 提供退款相关的业务逻辑处理，包括：
 * - 用户端：创建退款申请、查询退款状态
 * - 管理端：审核退款、处理退款、统计退款数据
 */
@Service
public class RefundService {

    /**
     * 退款数据访问层
     * 用于操作退款相关的数据库操作
     */
    @Autowired
    private RefundMapper refundMapper;
    
    /**
     * 订单数据访问层
     * 用于查询订单信息，获取userId等数据
     */
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 创建退款申请
     * 用户发起退款申请时调用，自动生成退款单号
     * @param refund 退款实体对象，包含订单ID、退款原因、退款金额等信息
     * @return 创建成功的退款对象，包含自动生成的退款单号
     * @throws RuntimeException 如果订单商品已经申请过退款（非拒绝状态）
     */
    public Refund createRefund(Refund refund) {
        // 检查是否重复退款
        if (refund.getOrderItemId() != null) {
            // 检查订单商品是否已申请退款
            if (refundMapper.hasActiveRefundByOrderItemId(refund.getOrderItemId())) {
                throw new RuntimeException("该商品已申请退款，请勿重复申请");
            }
        } else {
            // 检查订单是否已申请整单退款
            if (refundMapper.hasActiveRefundByOrderId(refund.getOrderId())) {
                throw new RuntimeException("该订单已申请退款，请勿重复申请");
            }
        }
        
        // 生成唯一的退款单号：REF+时间戳+8位随机码
        String refundNo = "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        refund.setRefundNo(refundNo);
        
        // 设置初始状态为"待审核"
        refund.setStatus("待审核");
        
        // 从订单中获取userId
        // 如果前端没有传入userId，则从订单表中查询
        if (refund.getUserId() == null && refund.getOrderId() != null) {
            Order order = orderMapper.findById(refund.getOrderId());
            if (order != null) {
                refund.setUserId(order.getUserId());
            }
        }
        
        // 插入数据库
        refundMapper.insert(refund);
        return refund;
    }

    /**
     * 根据ID查询退款详情
     * @param id 退款记录ID
     * @return 退款实体对象，如果不存在返回null
     */
    public Refund getRefundById(Long id) {
        return refundMapper.getById(id);
    }

    /**
     * 根据订单ID查询退款记录
     * 用于查询某个订单的所有退款申请
     * @param orderId 订单ID
     * @return 退款记录列表，按创建时间倒序排列
     */
    public List<Refund> getRefundsByOrderId(Long orderId) {
        return refundMapper.getByOrderId(orderId);
    }

    /**
     * 根据用户ID查询退款记录
     * 用于查询某个用户的所有退款申请
     * @param userId 用户ID
     * @return 退款记录列表，按创建时间倒序排列
     */
    public List<Refund> getRefundsByUserId(Long userId) {
        return refundMapper.getByUserId(userId);
    }

    /**
     * 根据退款单号查询退款详情
     * 退款单号是唯一标识，用于用户查询退款状态
     * @param refundNo 退款单号
     * @return 退款实体对象，如果不存在返回null
     */
    public Refund getRefundByRefundNo(String refundNo) {
        return refundMapper.getByRefundNo(refundNo);
    }

    /**
     * 审核通过退款申请
     * 管理员审核通过后，状态变更为"审核通过"，等待财务处理
     * @param refundId 退款记录ID
     * @return 更新后的退款对象
     */
    public Refund approveRefund(Long refundId) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            System.out.println("审核通过退款 - refundId: " + refundId + ", userId: " + refund.getUserId());
            // 更新状态为"审核通过"
            refund.setStatus("审核通过");
            refundMapper.updateStatus(refund);
            
            // 通过WebSocket通知用户退款状态更新
            RefundWebSocketHandler.sendRefundUpdate(refund.getUserId(), refund);
        }
        return refund;
    }

    /**
     * 审核拒绝退款申请
     * 管理员拒绝退款时，需要提供拒绝原因
     * @param refundId 退款记录ID
     * @param rejectReason 拒绝原因
     * @return 更新后的退款对象
     */
    public Refund rejectRefund(Long refundId, String rejectReason) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            System.out.println("审核拒绝退款 - refundId: " + refundId + ", userId: " + refund.getUserId());
            // 更新状态为"审核拒绝"
            refund.setStatus("审核拒绝");
            refund.setRejectReason(rejectReason);
            refundMapper.updateStatus(refund);
            
            // 通过WebSocket通知用户退款状态更新
            RefundWebSocketHandler.sendRefundUpdate(refund.getUserId(), refund);
        }
        return refund;
    }

    /**
     * 处理退款
     * 财务人员处理退款时，设置退款方式和退款账户
     * @param refundId 退款记录ID
     * @param refundMethod 退款方式：原路退回、银行转账
     * @param refundAccount 退款账户信息
     * @return 更新后的退款对象
     */
    public Refund processRefund(Long refundId, String refundMethod, String refundAccount) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            System.out.println("处理退款 - refundId: " + refundId + ", userId: " + refund.getUserId());
            // 更新状态为"退款中"
            refund.setStatus("退款中");
            refund.setRefundMethod(refundMethod);
            refund.setRefundAccount(refundAccount);
            refundMapper.updateRefundInfo(refund);
            
            // 通过WebSocket通知用户退款状态更新
            RefundWebSocketHandler.sendRefundUpdate(refund.getUserId(), refund);
        }
        return refund;
    }

    /**
     * 完成退款
     * 财务完成退款后，标记退款完成
     * @param refundId 退款记录ID
     * @return 更新后的退款对象
     */
    public Refund completeRefund(Long refundId) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            System.out.println("完成退款 - refundId: " + refundId + ", userId: " + refund.getUserId());
            // 更新状态为"退款完成"
            refund.setStatus("退款完成");
            refundMapper.updateStatus(refund);
            
            // 通过WebSocket通知用户退款状态更新
            RefundWebSocketHandler.sendRefundUpdate(refund.getUserId(), refund);
        }
        return refund;
    }
    
    /**
     * 获取待审核的退款列表
     * 管理员使用，查询所有待审核的退款申请
     * @return 待审核的退款列表
     */
    public List<Refund> getPendingRefunds() {
        return refundMapper.getByStatus("待审核");
    }
    
    /**
     * 获取审核通过待处理的退款列表
     * 财务人员使用，查询所有审核通过待处理的退款
     * @return 审核通过待处理的退款列表
     */
    public List<Refund> getApprovedRefunds() {
        return refundMapper.getByStatus("审核通过");
    }
    
    /**
     * 获取退款中的退款列表
     * 财务人员使用，查询所有正在处理中的退款
     * @return 退款中的退款列表
     */
    public List<Refund> getProcessingRefunds() {
        return refundMapper.getByStatus("退款中");
    }
    
    /**
     * 获取所有退款列表
     * 管理员使用，查询所有退款记录
     * @return 所有退款列表
     */
    public List<Refund> getAllRefunds() {
        return refundMapper.getAll();
    }
}