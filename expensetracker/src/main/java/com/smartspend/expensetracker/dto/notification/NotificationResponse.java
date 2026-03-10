package com.smartspend.expensetracker.dto.notification;

import java.time.LocalDateTime;

import com.smartspend.expensetracker.enums.NotificationType;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    NotificationType type,
    boolean read,
    LocalDateTime createdAt
) {
    
}
