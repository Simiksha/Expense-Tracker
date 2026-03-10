package com.smartspend.expensetracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.expensetracker.dto.auth.AuthResponse;
import com.smartspend.expensetracker.dto.auth.LoginRequest;
import com.smartspend.expensetracker.dto.auth.MessageResponse;
import com.smartspend.expensetracker.dto.auth.RegisterRequest;
import com.smartspend.expensetracker.exception.GlobalExceptionHandler;
import com.smartspend.expensetracker.security.jwt.JwtAuthenticationFilter;
import com.smartspend.expensetracker.security.jwt.JwtService;
import com.smartspend.expensetracker.service.auth.AuthService;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void register_shouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "testuser",
                "testuser@gmail.com",
                "password123"
        );

        MessageResponse response = new MessageResponse("Registration successful. Please verify your email.");

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful. Please verify your email."));
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest(
                "testuser@gmail.com",
                "password123"
        );

        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "testuser",
                "testuser@gmail.com",
                "ROLE_USER"
        );

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void verifyEmail_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "sample-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    void register_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                "bad-email",
                "123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}