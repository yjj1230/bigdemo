package org.example.shopdemo.service;

import org.example.shopdemo.entity.Notification;
import org.example.shopdemo.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    public Notification createNotification(Notification notification) {
        notification.setIsRead(false);
        notificationMapper.insert(notification);
        return notification;
    }

    public Notification getById(Long id) {
        return notificationMapper.getById(id);
    }

    public List<Notification> getByUserId(Long userId) {
        return notificationMapper.getByUserId(userId);
    }

    public List<Notification> getUnreadByUserId(Long userId) {
        return notificationMapper.getByUserIdAndReadStatus(userId, false);
    }

    public List<Notification> getReadByUserId(Long userId) {
        return notificationMapper.getByUserIdAndReadStatus(userId, true);
    }

    public Integer getUnreadCountByUserId(Long userId) {
        Integer count = notificationMapper.getUnreadCountByUserId(userId);
        return count != null ? count : 0;
    }

    public Notification markAsRead(Long id) {
        notificationMapper.markAsRead(id);
        return notificationMapper.getById(id);
    }

    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    public void deleteById(Long id) {
        notificationMapper.deleteById(id);
    }

    public Notification createOrderNotification(Long userId, String title, String content, Long orderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("订单通知");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedOrderId(orderId);
        return createNotification(notification);
    }

    public Notification createSystemNotification(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("系统通知");
        notification.setTitle(title);
        notification.setContent(content);
        return createNotification(notification);
    }

    public Notification createMarketingNotification(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("营销通知");
        notification.setTitle(title);
        notification.setContent(content);
        return createNotification(notification);
    }
}