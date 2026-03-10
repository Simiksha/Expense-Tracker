package com.smartspend.expensetracker.controller;

import com.smartspend.expensetracker.dto.notification.NotificationResponse;
import com.smartspend.expensetracker.enums.NotificationType;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtService;
import com.smartspend.expensetracker.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean 
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getMyNotifications_shouldReturnList() throws Exception {
        List<NotificationResponse> responses = List.of(
                new NotificationResponse(
                        1L,
                        "Budget nearing limit",
                        "Your Food budget is at 82%",
                        NotificationType.BUDGET_ALERT,
                        false,
                        LocalDateTime.of(2026, 3, 10, 10, 30)
                ),
                new NotificationResponse(
                        2L,
                        "Budget exceeded",
                        "Your Travel budget exceeded the limit",
                        NotificationType.BUDGET_ALERT,
                        true,
                        LocalDateTime.of(2026, 3, 10, 11, 0)
                )
        );

        given(notificationService.getMyNotifications()).willReturn(responses);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Budget nearing limit"))
                .andExpect(jsonPath("$[1].title").value("Budget exceeded"));
    }

    @Test
    void getMyUnreadNotifications_shouldReturnUnreadList() throws Exception {
        List<NotificationResponse> responses = List.of(
                new NotificationResponse(
                        1L,
                        "Budget nearing limit",
                        "Your Food budget is at 82%",
                        NotificationType.BUDGET_ALERT,
                        false,
                        LocalDateTime.of(2026, 3, 10, 10, 30)
                )
        );

        given(notificationService.getMyUnreadNotifications()).willReturn(responses);

        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].type").value("BUDGET_ALERT"));
    }

    @Test
    void getUnreadCount_shouldReturnCount() throws Exception {
        given(notificationService.getUnreadCount()).willReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    void markAsRead_shouldReturnUpdatedNotification() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L,
                "Budget nearing limit",
                "Your Food budget is at 82%",
                NotificationType.BUDGET_ALERT,
                true,
                LocalDateTime.of(2026, 3, 10, 10, 30)
        );

        given(notificationService.markAsRead(1L)).willReturn(response);

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAllAsRead_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }

    @Test
    void deleteNotification_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));
    }
}