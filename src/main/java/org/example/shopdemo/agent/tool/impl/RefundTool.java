package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Refund;
import org.example.shopdemo.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款工具
 * 负责处理退款相关的查询请求
 * 可以查询退款进度和退款状态
 */
@Component
public class RefundTool implements Tool {
    
    /**
     * 退款服务
     * 用于查询退款信息
     */
    @Autowired
    private RefundService refundService;
    
    /**
     * 执行退款查询功能
     * 根据参数决定查询退款列表还是退款详情
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result execute(String message, Long userId, Map<String, Object> params) {
        // 从参数中获取退款ID
        Long refundId = params.get("refundId") != null ? 
            Long.parseLong(params.get("refundId").toString()) : null;
        
        // 如果有退款ID，查询退款详情
        if (refundId != null) {
            return getRefundDetail(refundId, userId);
        } else {
            // 否则查询退款列表
            return getRefundList(userId);
        }
    }
    
    /**
     * 查询退款详情
     * @param refundId 退款ID
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result getRefundDetail(Long refundId, Long userId) {
        try {
            // 通过退款ID查询退款详情
            Refund refund = refundService.getRefundById(refundId);
            
            // 检查退款是否存在
            if (refund == null) {
                return Result.error("抱歉，没有找到ID为 " + refundId + " 的退款记录。");
            }
            
            // 检查用户是否有权限查看该退款
            if (!refund.getUserId().equals(userId)) {
                return Result.error("抱歉，您无权查看该退款记录。");
            }
            
            // 构建退款详情的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("💰 退款详情\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("退款ID：").append(refund.getId()).append("\n");
            sb.append("退款单号：").append(refund.getRefundNo()).append("\n");
            sb.append("订单ID：").append(refund.getOrderId()).append("\n");
            sb.append("退款金额：¥").append(refund.getRefundAmount()).append("\n");
            sb.append("退款原因：").append(refund.getReason()).append("\n");
            sb.append("退款状态：").append(getRefundStatusText(refund.getStatus())).append("\n");
            sb.append("申请时间：").append(refund.getCreateTime()).append("\n");
            
            if (refund.getRejectReason() != null) {
                sb.append("拒绝原因：").append(refund.getRejectReason()).append("\n");
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            responseData.put("refundId", refundId);
            responseData.put("orderId", refund.getOrderId());
            responseData.put("status", refund.getStatus());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            return Result.error("查询退款详情时出现错误：" + e.getMessage());
        }
    }
    
    /**
     * 查询退款列表
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result getRefundList(Long userId) {
        try {
            // 查询用户的所有退款记录
            List<Refund> refunds = refundService.getRefundsByUserId(userId);
            
            // 检查是否有退款记录
            if (refunds == null || refunds.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "您还没有任何退款记录。");
                return Result.success(responseData);
            }
            
            // 构建退款列表的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("💰 我的退款\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            for (Refund refund : refunds) {
                sb.append("订单ID：").append(refund.getOrderId()).append("\n");
                sb.append("退款单号：").append(refund.getRefundNo()).append("\n");
                sb.append("退款金额：¥").append(refund.getRefundAmount()).append("\n");
                sb.append("退款状态：").append(getRefundStatusText(refund.getStatus())).append("\n");
                sb.append("申请时间：").append(refund.getCreateTime()).append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            }
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            responseData.put("count", refunds.size());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            return Result.error("查询退款列表时出现错误：" + e.getMessage());
        }
    }
    
    /**
     * 获取退款状态文本
     * @param status 状态
     * @return 状态文本
     */
    private String getRefundStatusText(String status) {
        switch (status) {
            case "待审核":
                return "待审核";
            case "审核通过":
                return "审核通过";
            case "审核拒绝":
                return "审核拒绝";
            case "退款中":
                return "退款中";
            case "退款完成":
                return "退款完成";
            case "退款失败":
                return "退款失败";
            default:
                return status;
        }
    }
    
    @Override
    public String getToolName() {
        return "退款工具";
    }
    
    @Override
    public String getDescription() {
        return "查询退款进度、退款状态、退款记录";
    }
    
    @Override
    public String[] getKeywords() {
        return new String[]{"退款", "退货", "售后", "退款进度", "退款状态", "退款查询"};
    }
    
    @Override
    public boolean canHandle(String message) {
        return message.equals("REFUND_APPLY") || message.equals("REFUND_QUERY");
    }
}
