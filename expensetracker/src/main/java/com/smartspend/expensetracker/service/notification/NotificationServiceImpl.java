package com.smartspend.expensetracker.service.notification;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartspend.expensetracker.dto.notification.NotificationResponse;
import com.smartspend.expensetracker.enums.NotificationType;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.model.Notification;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.repository.NotificationRepository;
import com.smartspend.expensetracker.repository.UserRepository;
import com.smartspend.expensetracker.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public boolean createNotification(Long userId, String title, String message, String type, String referenceKey) {
        if (referenceKey != null && notificationRepository.existsByReferenceKey(referenceKey)) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.valueOf(type))
                .read(false)
                .referenceKey(referenceKey)
                .user(user)
                .build();

        notificationRepository.save(notification);
        return true;
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = userService.getCurrentUser();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getMyUnreadNotifications() {
        User currentUser = userService.getCurrentUser();

        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public long getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(currentUser.getId());
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        User currentUser = userService.getCurrentUser();

        Notification notification = notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);

        return mapToResponse(notificationRepository.save(notification));
    }

    @Override
    public void markAllAsRead() {
        User currentUser = userService.getCurrentUser();

        List<Notification> notifications = notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(currentUser.getId());

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotification(Long id) {
        User currentUser = userService.getCurrentUser();

        Notification notification = notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
