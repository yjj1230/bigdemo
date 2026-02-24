package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Points;
import org.example.shopdemo.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@Tag(name = "积分管理", description = "积分相关接口")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @PostMapping("/add")
    @Operation(summary = "添加积分", description = "添加积分记录")
    public Result<Points> addPoints(@RequestBody Points points) {
        Points added = pointsService.addPoints(points);
        return Result.success(added);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询积分详情", description = "根据ID查询积分详情")
    public Result<Points> getById(@PathVariable Long id) {
        Points points = pointsService.getById(id);
        return Result.success(points);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户积分记录", description = "根据用户ID查询积分记录")
    public Result<List<Points>> getByUserId(@PathVariable Long userId) {
        List<Points> pointsList = pointsService.getByUserId(userId);
        return Result.success(pointsList);
    }

    @GetMapping("/total/{userId}")
    @Operation(summary = "查询用户总积分", description = "根据用户ID查询总积分")
    public Result<Map<String, Integer>> getTotalPoints(@PathVariable Long userId) {
        Integer total = pointsService.getTotalPointsByUserId(userId);
        return Result.success(Map.of("totalPoints", total));
    }

    @GetMapping("/recent/{userId}")
    @Operation(summary = "查询最近积分记录", description = "查询用户最近的积分记录")
    public Result<List<Points>> getRecentPoints(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<Points> pointsList = pointsService.getRecentByUserId(userId, limit);
        return Result.success(pointsList);
    }

    @PostMapping("/signIn/{userId}")
    @Operation(summary = "签到获得积分", description = "用户签到获得积分")
    public Result<Points> signIn(@PathVariable Long userId) {
        Points points = pointsService.addSignInPoints(userId);
        return Result.success(points);
    }
}