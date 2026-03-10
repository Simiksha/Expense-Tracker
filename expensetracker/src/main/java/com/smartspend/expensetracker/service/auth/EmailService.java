package com.smartspend.expensetracker.service.auth;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
