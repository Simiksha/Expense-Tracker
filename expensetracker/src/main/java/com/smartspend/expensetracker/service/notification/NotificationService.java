package com.smartspend.expensetracker.service.notification;

import java.util.List;

import com.smartspend.expensetracker.dto.notification.NotificationResponse;

public interface NotificationService {
    
    boolean createNotification(Long userId, String title, String message, String type, String referenceKey);

    List<NotificationResponse> getMyNotifications();

    List<NotificationResponse> getMyUnreadNotifications();

    long getUnreadCount();

    NotificationResponse markAsRead(Long id);

    void markAllAsRead();

    void deleteNotification(Long id);
}
