package com.smartspend.expensetracker.service.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartspend.expensetracker.dto.auth.AuthResponse;
import com.smartspend.expensetracker.dto.auth.ForgotPasswordRequest;
import com.smartspend.expensetracker.dto.auth.LoginRequest;
import com.smartspend.expensetracker.dto.auth.MessageResponse;
import com.smartspend.expensetracker.dto.auth.RegisterRequest;
import com.smartspend.expensetracker.dto.auth.ResetPasswordRequest;
import com.smartspend.expensetracker.enums.Role;
import com.smartspend.expensetracker.exception.BadRequestException;
import com.smartspend.expensetracker.model.PasswordResetToken;
import com.smartspend.expensetracker.model.User;
import com.smartspend.expensetracker.model.VerificationToken;
import com.smartspend.expensetracker.repository.PasswordResetTokenRepository;
import com.smartspend.expensetracker.repository.UserRepository;
import com.smartspend.expensetracker.repository.VerificationTokenRepository;
import com.smartspend.expensetracker.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
        private final UserRepository userRepository;
        private final VerificationTokenRepository verificationTokenRepository;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;

        @Override
        public MessageResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.email())) {
                        throw new BadRequestException("Email is already registered");
                }

                User user = User.builder()
                                .name(request.name())
                                .email(request.email())
                                .password(passwordEncoder.encode(request.password()))
                                .role(Role.ROLE_USER)
                                .enabled(false)
                                .emailVerified(false)
                                .build();

                User savedUser = userRepository.save(user);

                String token = UUID.randomUUID().toString();

                verificationTokenRepository.deleteByUserId(savedUser.getId());
                verificationTokenRepository.save(
                                VerificationToken.builder()
                                                .token(token)
                                                .user(savedUser)
                                                .expiryDate(LocalDateTime.now().plusHours(24))
                                                .build());

                String verificationLink = "http://localhost:8080/api/auth/verify-email?token=" + token;
                System.out.println("Verification Link: " + verificationLink);

                emailService.sendEmail(
                                savedUser.getEmail(),
                                "Verify your Smart Spend account",
                                "Click the link to verify your account:\n" + verificationLink);

                return new MessageResponse("Registration successful. Please verify your email.");
        }

        @Override
        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.email(),
                                                request.password()));

                User user = userRepository.findByEmail(request.email())
                                .orElseThrow(() -> new BadRequestException("User not found"));

                if (!user.isEmailVerified()) {
                        throw new BadRequestException("Please verify your email before logging in");
                }

                String token = jwtService.generateToken(user);

                return new AuthResponse(
                                token,
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRole().name());
        }

        @Override
        public void verifyEmail(String token) {
                VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

                if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        throw new BadRequestException("Verification token has expired");
                }

                User user = verificationToken.getUser();
                user.setEnabled(true);
                user.setEmailVerified(true);
                userRepository.save(user);

                verificationTokenRepository.delete(verificationToken);
        }

        @Override
        public void forgotPassword(ForgotPasswordRequest request) {
                User user = userRepository.findByEmail(request.email())
                                .orElseThrow(() -> new BadRequestException("User not found with this email"));

                String token = UUID.randomUUID().toString();

                passwordResetTokenRepository.deleteByUserId(user.getId());
                passwordResetTokenRepository.save(
                                PasswordResetToken.builder()
                                                .token(token)
                                                .user(user)
                                                .expiryDate(LocalDateTime.now().plusHours(1))
                                                .build());

                String resetLink = "http://localhost:5173/reset-password?token=" + token;

                emailService.sendEmail(
                                user.getEmail(),
                                "Reset your Smart Spend password",
                                "Click the link to reset your password:\n" + resetLink);
        }

        @Override
        public void resetPassword(ResetPasswordRequest request) {
                PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

                if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        throw new BadRequestException("Password reset token has expired");
                }

                User user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(request.newPassword()));
                userRepository.save(user);

                passwordResetTokenRepository.delete(resetToken);
        }
}