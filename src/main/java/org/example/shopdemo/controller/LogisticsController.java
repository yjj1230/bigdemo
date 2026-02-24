package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Logistics;
import org.example.shopdemo.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics")
@Tag(name = "物流管理", description = "物流追踪相关接口")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @PostMapping("/create")
    @Operation(summary = "创建物流信息", description = "为订单创建物流信息")
    public Result<Logistics> createLogistics(@RequestBody Logistics logistics) {
        Logistics created = logisticsService.createLogistics(logistics);
        return Result.success(created);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "查询订单物流", description = "根据订单ID查询物流信息")
    public Result<List<Logistics>> getLogisticsByOrderId(@PathVariable Long orderId) {
        List<Logistics> logisticsList = logisticsService.getLogisticsByOrderId(orderId);
        return Result.success(logisticsList);
    }

    @GetMapping("/track/{logisticsNo}")
    @Operation(summary = "追踪物流", description = "根据物流单号追踪物流")
    public Result<List<Logistics>> trackLogistics(@PathVariable String logisticsNo) {
        List<Logistics> logisticsList = logisticsService.getLogisticsByLogisticsNo(logisticsNo);
        return Result.success(logisticsList);
    }

    @PutMapping("/update")
    @Operation(summary = "更新物流信息", description = "更新物流状态和位置")
    public Result<Logistics> updateLogistics(@RequestBody Logistics logistics) {
        Logistics updated = logisticsService.updateLogistics(logistics);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物流信息", description = "删除物流记录")
    public Result<Void> deleteLogistics(@PathVariable Long id) {
        logisticsService.deleteLogistics(id);
        return Result.success("删除成功");
    }
}