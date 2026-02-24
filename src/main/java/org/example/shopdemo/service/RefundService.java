package org.example.shopdemo.service;

import org.example.shopdemo.entity.Refund;
import org.example.shopdemo.mapper.RefundMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    @Autowired
    private RefundMapper refundMapper;

    public Refund createRefund(Refund refund) {
        String refundNo = "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        refund.setRefundNo(refundNo);
        refund.setStatus("待审核");
        refundMapper.insert(refund);
        return refund;
    }

    public Refund getRefundById(Long id) {
        return refundMapper.getById(id);
    }

    public List<Refund> getRefundsByOrderId(Long orderId) {
        return refundMapper.getByOrderId(orderId);
    }

    public List<Refund> getRefundsByUserId(Long userId) {
        return refundMapper.getByUserId(userId);
    }

    public Refund getRefundByRefundNo(String refundNo) {
        return refundMapper.getByRefundNo(refundNo);
    }

    public Refund approveRefund(Long refundId) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            refund.setStatus("审核通过");
            refundMapper.updateStatus(refund);
        }
        return refund;
    }

    public Refund rejectRefund(Long refundId, String rejectReason) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            refund.setStatus("审核拒绝");
            refund.setRejectReason(rejectReason);
            refundMapper.updateStatus(refund);
        }
        return refund;
    }

    public Refund processRefund(Long refundId, String refundMethod, String refundAccount) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            refund.setStatus("退款中");
            refund.setRefundMethod(refundMethod);
            refund.setRefundAccount(refundAccount);
            refundMapper.updateRefundInfo(refund);
        }
        return refund;
    }

    public Refund completeRefund(Long refundId) {
        Refund refund = refundMapper.getById(refundId);
        if (refund != null) {
            refund.setStatus("退款完成");
            refundMapper.updateStatus(refund);
        }
        return refund;
    }
}