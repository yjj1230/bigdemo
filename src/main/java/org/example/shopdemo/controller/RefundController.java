package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Refund;
import org.example.shopdemo.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refund")
@Tag(name = "退款管理", description = "退款退货相关接口")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/create")
    @Operation(summary = "创建退款申请", description = "用户发起退款申请")
    public Result<Refund> createRefund(@RequestBody Refund refund) {
        Refund created = refundService.createRefund(refund);
        return Result.success(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询退款详情", description = "根据ID查询退款详情")
    public Result<Refund> getRefundById(@PathVariable Long id) {
        Refund refund = refundService.getRefundById(id);
        return Result.success(refund);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "查询订单退款", description = "根据订单ID查询退款记录")
    public Result<List<Refund>> getRefundsByOrderId(@PathVariable Long orderId) {
        List<Refund> refunds = refundService.getRefundsByOrderId(orderId);
        return Result.success(refunds);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户退款", description = "根据用户ID查询退款记录")
    public Result<List<Refund>> getRefundsByUserId(@PathVariable Long userId) {
        List<Refund> refunds = refundService.getRefundsByUserId(userId);
        return Result.success(refunds);
    }

    @GetMapping("/no/{refundNo}")
    @Operation(summary = "根据退款单号查询", description = "根据退款单号查询退款详情")
    public Result<Refund> getRefundByRefundNo(@PathVariable String refundNo) {
        Refund refund = refundService.getRefundByRefundNo(refundNo);
        return Result.success(refund);
    }

    @PostMapping("/approve/{id}")
    @Operation(summary = "审核通过", description = "管理员审核通过退款申请")
    public Result<Refund> approveRefund(@PathVariable Long id) {
        Refund refund = refundService.approveRefund(id);
        return Result.success(refund);
    }

    @PostMapping("/reject/{id}")
    @Operation(summary = "审核拒绝", description = "管理员拒绝退款申请")
    public Result<Refund> rejectRefund(@PathVariable Long id, @RequestParam String rejectReason) {
        Refund refund = refundService.rejectRefund(id, rejectReason);
        return Result.success(refund);
    }

    @PostMapping("/process/{id}")
    @Operation(summary = "处理退款", description = "财务处理退款")
    public Result<Refund> processRefund(
            @PathVariable Long id,
            @RequestParam String refundMethod,
            @RequestParam String refundAccount) {
        Refund refund = refundService.processRefund(id, refundMethod, refundAccount);
        return Result.success(refund);
    }

    @PostMapping("/complete/{id}")
    @Operation(summary = "完成退款", description = "标记退款完成")
    public Result<Refund> completeRefund(@PathVariable Long id) {
        Refund refund = refundService.completeRefund(id);
        return Result.success(refund);
    }
}