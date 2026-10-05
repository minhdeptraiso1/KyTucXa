package com.project.base_v1.service;

import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.request.auth.RegisterRequest;
import com.project.base_v1.dto.request.auth.ChangePasswordRequest;
import com.project.base_v1.dto.request.auth.ForgotPasswordRequest;
import com.project.base_v1.dto.request.auth.ResetPasswordRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;
import com.project.base_v1.dto.response.core.MessageResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    MessageResponse register(RegisterRequest request);

    MessageResponse verifyEmail(String token);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse changePassword(String username, ChangePasswordRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String accessToken, String refreshToken);
}

