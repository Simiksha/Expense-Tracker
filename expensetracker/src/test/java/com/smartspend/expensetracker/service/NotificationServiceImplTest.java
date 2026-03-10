package com.smartspend.expensetracker.service;

import com.smartspend.expensetracker.dto.notification.NotificationResponse;
import com.smartspend.expensetracker.model.Notification;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.enums.NotificationType;
import com.smartspend.expensetracker.enums.Role;
import com.smartspend.expensetracker.exception.ResourceNotFoundException;
import com.smartspend.expensetracker.repository.NotificationRepository;
import com.smartspend.expensetracker.repository.UserRepository;
import com.smartspend.expensetracker.service.notification.NotificationServiceImpl;
import com.smartspend.expensetracker.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_shouldCreateSuccessfully_whenReferenceKeyDoesNotExist() {
        User user = buildUser();

        given(notificationRepository.existsByReferenceKey("BUDGET_1_3_2026_NEARING_LIMIT")).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        boolean created = notificationService.createNotification(
                1L,
                "Budget nearing limit",
                "Your budget is at 85%",
                "BUDGET_ALERT",
                "BUDGET_1_3_2026_NEARING_LIMIT"
        );

        assertTrue(created);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldNotCreate_whenReferenceKeyAlreadyExists() {
        given(notificationRepository.existsByReferenceKey("BUDGET_1_3_2026_NEARING_LIMIT")).willReturn(true);

        boolean created = notificationService.createNotification(
                1L,
                "Budget nearing limit",
                "Your budget is at 85%",
                "BUDGET_ALERT",
                "BUDGET_1_3_2026_NEARING_LIMIT"
        );

        assertFalse(created);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getMyNotifications_shouldReturnNotifications() {
        User user = buildUser();

        Notification notification = Notification.builder()
                .id(1L)
                .title("Budget exceeded")
                .message("Food budget exceeded")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .referenceKey("BUDGET_1_3_2026_EXCEEDED")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getMyNotifications();

        assertEquals(1, responses.size());
        assertEquals("Budget exceeded", responses.get(0).title());
        assertFalse(responses.get(0).read());
    }

    @Test
    void getMyUnreadNotifications_shouldReturnOnlyUnread() {
        User user = buildUser();

        Notification notification = Notification.builder()
                .id(1L)
                .title("Budget nearing limit")
                .message("Food budget is at 82%")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .referenceKey("BUDGET_1_3_2026_NEARING_LIMIT")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .willReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getMyUnreadNotifications();

        assertEquals(1, responses.size());
        assertEquals("Budget nearing limit", responses.get(0).title());
    }

    @Test
    void getUnreadCount_shouldReturnCount() {
        User user = buildUser();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.countByUserIdAndReadFalse(1L)).willReturn(3L);

        long count = notificationService.getUnreadCount();

        assertEquals(3L, count);
    }

    @Test
    void markAsRead_shouldUpdateNotification() {
        User user = buildUser();

        Notification notification = Notification.builder()
                .id(1L)
                .title("Budget nearing limit")
                .message("Food budget is at 82%")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .referenceKey("BUDGET_1_3_2026_NEARING_LIMIT")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = Notification.builder()
                .id(1L)
                .title("Budget nearing limit")
                .message("Food budget is at 82%")
                .type(NotificationType.BUDGET_ALERT)
                .read(true)
                .referenceKey("BUDGET_1_3_2026_NEARING_LIMIT")
                .user(user)
                .createdAt(notification.getCreatedAt())
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(notification));
        given(notificationRepository.save(any(Notification.class))).willReturn(savedNotification);

        NotificationResponse response = notificationService.markAsRead(1L);

        assertTrue(response.read());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAllAsRead_shouldUpdateAllUnreadNotifications() {
        User user = buildUser();

        Notification n1 = Notification.builder()
                .id(1L)
                .title("One")
                .message("First")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        Notification n2 = Notification.builder()
                .id(2L)
                .title("Two")
                .message("Second")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .willReturn(List.of(n1, n2));

        notificationService.markAllAsRead();

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    @Test
    void deleteNotification_shouldDeleteSuccessfully() {
        User user = buildUser();

        Notification notification = Notification.builder()
                .id(1L)
                .title("Budget exceeded")
                .message("Food budget exceeded")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(notification));

        notificationService.deleteNotification(1L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void markAsRead_shouldThrowException_whenNotificationNotFound() {
        User user = buildUser();

        given(userService.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByIdAndUserId(99L, 1L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(99L));
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .name("testuser")
                .email("testuser@gmail.com")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .build();
    }
}