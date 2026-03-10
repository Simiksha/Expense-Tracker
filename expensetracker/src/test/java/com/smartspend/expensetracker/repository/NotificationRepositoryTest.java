package com.smartspend.expensetracker.repository;

import com.smartspend.expensetracker.model.Notification;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.enums.NotificationType;
import com.smartspend.expensetracker.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should count unread notifications")
    void countByUserIdAndReadFalse_shouldReturnCount() {
        User user = userRepository.save(buildUser("testuser@gmail.com"));

        notificationRepository.save(Notification.builder()
                .title("One")
                .message("First")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .referenceKey("KEY_1")
                .user(user)
                .build());

        notificationRepository.save(Notification.builder()
                .title("Two")
                .message("Second")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .referenceKey("KEY_2")
                .user(user)
                .build());

        long count = notificationRepository.countByUserIdAndReadFalse(user.getId());

        assertEquals(2L, count);
    }

    @Test
    @DisplayName("should detect existing reference key")
    void existsByReferenceKey_shouldReturnTrue() {
        User user = userRepository.save(buildUser("testuser2@gmail.com"));

        notificationRepository.save(Notification.builder()
                .title("One")
                .message("First")
                .type(NotificationType.BUDGET_ALERT)
                .read(false)
                .referenceKey("KEY_123")
                .user(user)
                .build());

        assertTrue(notificationRepository.existsByReferenceKey("KEY_123"));
    }

    private User buildUser(String email) {
        return User.builder()
                .name("testuser")
                .email(email)
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .build();
    }
}
