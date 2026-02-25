package org.example.shopdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款实体类
 * 用于存储用户的退款申请信息，包括退款状态、金额、原因等
 * 支持整单退款和部分退款（通过orderItemId区分）
 */
@Data
public class Refund {
    /**
     * 退款记录主键ID
     * 数据库自增主键
     */
    private Long id;
    
    /**
     * 用户ID
     * 申请退款的用户标识，关联用户表
     */
    private Long userId;
    
    /**
     * 订单ID
     * 关联的订单ID，用于追溯原订单信息
     */
    private Long orderId;
    
    /**
     * 订单商品ID
     * 部分退款时使用，指定具体退款的商品项
     * 如果为null，表示整单退款
     */
    private Long orderItemId;
    
    /**
     * 退款单号
     * 唯一标识退款申请的编号，格式：REF+时间戳+随机码
     * 例如：REF1700000000000A1B2C3D4
     */
    private String refundNo;
    
    /**
     * 退款原因
     * 用户申请退款的具体原因说明
     * 例如：商品质量问题、不想要了、发错货等
     */
    private String reason;
    
    /**
     * 退款金额
     * 用户申请退款的金额，单位：元
     * 不能超过订单实际支付金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款状态
     * 退款申请的当前状态，可选值：
     * - 待审核：用户刚提交申请，等待管理员审核
     * - 审核通过：管理员审核通过，等待财务处理
     * - 审核拒绝：管理员拒绝退款申请
     * - 退款中：财务正在处理退款
     * - 退款完成：退款已成功到账
     */
    private String status;
    
    /**
     * 拒绝原因
     * 管理员拒绝退款时的原因说明
     * 仅在状态为"审核拒绝"时有值
     */
    private String rejectReason;
    
    /**
     * 退款方式
     * 财务处理退款时的退款方式
     * 可选值：原路退回、银行转账
     */
    private String refundMethod;
    
    /**
     * 退款账户
     * 财务处理退款时的收款账户信息
     * 例如：支付宝账号、银行卡号等
     */
    private String refundAccount;
    
    /**
     * 创建时间
     * 退款申请的提交时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 退款记录的最后更新时间
     * 每次状态变更或信息修改时自动更新
     */
    private LocalDateTime updateTime;
}