package com.smartspend.expensetracker.service.auth;

import com.smartspend.expensetracker.dto.auth.AuthResponse;
import com.smartspend.expensetracker.dto.auth.ForgotPasswordRequest;
import com.smartspend.expensetracker.dto.auth.LoginRequest;
import com.smartspend.expensetracker.dto.auth.MessageResponse;
import com.smartspend.expensetracker.dto.auth.RegisterRequest;
import com.smartspend.expensetracker.dto.auth.ResetPasswordRequest;

public interface AuthService {
    MessageResponse  register(RegisterRequest request);
    AuthResponse login(LoginRequest request);  

    void verifyEmail(String token);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
} 
