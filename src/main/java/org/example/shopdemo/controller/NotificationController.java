package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Notification;
import org.example.shopdemo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "通知管理", description = "消息通知相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create")
    @Operation(summary = "创建通知", description = "创建新的通知")
    public Result<Notification> createNotification(@RequestBody Notification notification) {
        Notification created = notificationService.createNotification(notification);
        return Result.success(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询通知详情", description = "根据ID查询通知详情")
    public Result<Notification> getById(@PathVariable Long id) {
        Notification notification = notificationService.getById(id);
        return Result.success(notification);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户通知", description = "根据用户ID查询所有通知")
    public Result<List<Notification>> getByUserId(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getByUserId(userId);
        return Result.success(notifications);
    }

    @GetMapping("/unread/{userId}")
    @Operation(summary = "查询未读通知", description = "根据用户ID查询未读通知")
    public Result<List<Notification>> getUnreadByUserId(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadByUserId(userId);
        return Result.success(notifications);
    }

    @GetMapping("/count/unread/{userId}")
    @Operation(summary = "查询未读数量", description = "查询用户未读通知数量")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        Integer count = notificationService.getUnreadCountByUserId(userId);
        return Result.success(count);
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "标记已读", description = "将通知标记为已读")
    public Result<Notification> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        return Result.success(notification);
    }

    @PostMapping("/readAll/{userId}")
    @Operation(summary = "全部标记已读", description = "将用户所有通知标记为已读")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记已读");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除通知")
    public Result<Void> deleteById(@PathVariable Long id) {
        notificationService.deleteById(id);
        return Result.success("删除成功");
    }
}