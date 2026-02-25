package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Refund;
import org.example.shopdemo.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退款管理控制器
 * 提供退款相关的API接口，包括：
 * - 用户端：创建退款申请、查询退款状态
 * - 管理端：审核退款、处理退款、查询退款列表
 */
@RestController
@RequestMapping("/api/refund")
@Tag(name = "退款管理", description = "退款退货相关接口")
public class RefundController {

    /**
     * 退款服务
     * 处理退款相关的业务逻辑
     */
    @Autowired
    private RefundService refundService;

    /**
     * 创建退款申请
     * 用户端接口，用户发起退款申请
     * @param refund 退款实体对象，包含订单ID、退款原因、退款金额等信息
     * @return 创建成功的退款对象，包含自动生成的退款单号
     */
    @PostMapping("/create")
    @Operation(summary = "创建退款申请", description = "用户发起退款申请，自动生成退款单号")
    public Result<Refund> createRefund(@RequestBody Refund refund) {
        Refund created = refundService.createRefund(refund);
        return Result.success(created);
    }

    /**
     * 查询退款详情
     * 用户端和管理端通用接口，根据ID查询退款详情
     * @param id 退款记录ID
     * @return 退款详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询退款详情", description = "根据ID查询退款详情")
    public Result<Refund> getRefundById(@PathVariable Long id) {
        Refund refund = refundService.getRefundById(id);
        return Result.success(refund);
    }

    /**
     * 查询订单的退款记录
     * 用户端接口，根据订单ID查询该订单的所有退款记录
     * @param orderId 订单ID
     * @return 退款记录列表
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "查询订单退款", description = "根据订单ID查询退款记录")
    public Result<List<Refund>> getRefundsByOrderId(@PathVariable Long orderId) {
        List<Refund> refunds = refundService.getRefundsByOrderId(orderId);
        return Result.success(refunds);
    }

    /**
     * 查询用户的退款记录
     * 用户端接口，根据用户ID查询该用户的所有退款记录
     * @param userId 用户ID
     * @return 退款记录列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户退款", description = "根据用户ID查询退款记录")
    public Result<List<Refund>> getRefundsByUserId(@PathVariable Long userId) {
        List<Refund> refunds = refundService.getRefundsByUserId(userId);
        return Result.success(refunds);
    }

    /**
     * 根据退款单号查询退款详情
     * 用户端接口，用户通过退款单号查询退款状态
     * @param refundNo 退款单号
     * @return 退款详情
     */
    @GetMapping("/no/{refundNo}")
    @Operation(summary = "根据退款单号查询", description = "根据退款单号查询退款详情")
    public Result<Refund> getRefundByRefundNo(@PathVariable String refundNo) {
        Refund refund = refundService.getRefundByRefundNo(refundNo);
        return Result.success(refund);
    }

    /**
     * 审核通过退款申请
     * 管理端接口，管理员审核通过退款申请
     * @param id 退款记录ID
     * @return 更新后的退款对象
     */
    @PostMapping("/approve/{id}")
    @Operation(summary = "审核通过", description = "管理员审核通过退款申请")
    public Result<Refund> approveRefund(@PathVariable Long id) {
        Refund refund = refundService.approveRefund(id);
        return Result.success(refund);
    }

    /**
     * 审核拒绝退款申请
     * 管理端接口，管理员拒绝退款申请，需要提供拒绝原因
     * @param id 退款记录ID
     * @param rejectReason 拒绝原因
     * @return 更新后的退款对象
     */
    @PostMapping("/reject/{id}")
    @Operation(summary = "审核拒绝", description = "管理员拒绝退款申请")
    public Result<Refund> rejectRefund(@PathVariable Long id, @RequestParam String rejectReason) {
        Refund refund = refundService.rejectRefund(id, rejectReason);
        return Result.success(refund);
    }

    /**
     * 处理退款
     * 管理端接口，财务人员处理退款，设置退款方式和退款账户
     * @param id 退款记录ID
     * @param refundMethod 退款方式：原路退回、银行转账
     * @param refundAccount 退款账户信息
     * @return 更新后的退款对象
     */
    @PostMapping("/process/{id}")
    @Operation(summary = "处理退款", description = "财务处理退款")
    public Result<Refund> processRefund(
            @PathVariable Long id,
            @RequestParam String refundMethod,
            @RequestParam String refundAccount) {
        Refund refund = refundService.processRefund(id, refundMethod, refundAccount);
        return Result.success(refund);
    }

    /**
     * 完成退款
     * 管理端接口，财务完成退款后标记退款完成
     * @param id 退款记录ID
     * @return 更新后的退款对象
     */
    @PostMapping("/complete/{id}")
    @Operation(summary = "完成退款", description = "标记退款完成")
    public Result<Refund> completeRefund(@PathVariable Long id) {
        Refund refund = refundService.completeRefund(id);
        return Result.success(refund);
    }
    
    /**
     * 获取待审核的退款列表
     * 管理端接口，管理员查询所有待审核的退款申请
     * @return 待审核的退款列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审核退款", description = "查询所有待审核的退款申请")
    public Result<List<Refund>> getPendingRefunds() {
        List<Refund> refunds = refundService.getPendingRefunds();
        return Result.success(refunds);
    }
    
    /**
     * 获取审核通过待处理的退款列表
     * 管理端接口，财务人员查询所有审核通过待处理的退款
     * @return 审核通过待处理的退款列表
     */
    @GetMapping("/approved")
    @Operation(summary = "获取待处理退款", description = "查询所有审核通过待处理的退款")
    public Result<List<Refund>> getApprovedRefunds() {
        List<Refund> refunds = refundService.getApprovedRefunds();
        return Result.success(refunds);
    }
    
    /**
     * 获取退款中的退款列表
     * 管理端接口，财务人员查询所有正在处理中的退款
     * @return 退款中的退款列表
     */
    @GetMapping("/processing")
    @Operation(summary = "获取退款中列表", description = "查询所有正在处理中的退款")
    public Result<List<Refund>> getProcessingRefunds() {
        List<Refund> refunds = refundService.getProcessingRefunds();
        return Result.success(refunds);
    }
    
    /**
     * 获取所有退款列表
     * 管理端接口，查询所有退款记录
     * @return 所有退款列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有退款", description = "查询所有退款记录")
    public Result<List<Refund>> getAllRefunds() {
        List<Refund> refunds = refundService.getAllRefunds();
        return Result.success(refunds);
    }
}