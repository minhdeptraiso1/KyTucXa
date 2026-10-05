package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.auth.ChangePasswordRequest;
import com.project.base_v1.dto.request.auth.ForgotPasswordRequest;
import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.request.auth.RegisterRequest;
import com.project.base_v1.dto.request.auth.ResetPasswordRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;
import com.project.base_v1.dto.response.core.MessageResponse;
import com.project.base_v1.entity.EmailVerificationToken;
import com.project.base_v1.entity.PasswordResetToken;
import com.project.base_v1.entity.TokenSession;
import com.project.base_v1.entity.User;
import com.project.base_v1.entity.UserProfile;
import com.project.base_v1.enums.AccountStatus;
import com.project.base_v1.enums.AuditAction;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.EmailVerificationTokenRepository;
import com.project.base_v1.repository.PasswordResetTokenRepository;
import com.project.base_v1.repository.TokenSessionRepository;
import com.project.base_v1.repository.UserProfileRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.security.JwtTokenProvider;
import com.project.base_v1.security.LoginRateLimiter;
import com.project.base_v1.security.TokenBlacklistService;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.AuthService;
import com.project.base_v1.service.TokenDeliveryService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenSessionRepository tokenSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginRateLimiter loginRateLimiter;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final TokenDeliveryService tokenDeliveryService;
    private final com.project.base_v1.service.StudentRegistryService studentRegistryService;
    private final com.project.base_v1.repository.StudentRegistryRepository studentRegistryRepository;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Bước 1: Validate request
        if (request.confirmPassword() != null && !request.confirmPassword().isBlank()
                && !request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        String studentCode = request.studentCode() != null ? request.studentCode().trim() : "";
        String email = request.email().trim().toLowerCase();
        String phone = request.phone() != null ? request.phone().trim() : "";
        String fullName = request.fullName().trim();

        // Bước 2, 3, 4, 5: Tìm Student Registry, kiểm tra status, đối chiếu thông tin, kiểm tra đã liên kết
        com.project.base_v1.entity.StudentRegistry student = studentRegistryService.validateForRegistration(
                studentCode, email, phone, fullName
        );

        String username = (request.username() != null && !request.username().isBlank())
                ? request.username().trim()
                : student.getStudentCode().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Bước 6: Tạo User và liên kết với student_registry
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .status(AccountStatus.PENDING)
                .studentRegistryId(student.getId())
                .build();
        userRepository.save(user);

        userProfileRepository.save(UserProfile.builder()
                .userId(user.getId())
                .fullName(student.getFullName())
                .studentCode(student.getStudentCode())
                .phone(student.getPhone())
                .className(student.getClassName())
                .build());

        auditLogService.log(user.getId(), AuditAction.REGISTER.name());

        String rawToken = randomToken();
        emailVerificationTokenRepository.deleteByUserId(user.getId());
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .id(UUID.randomUUID()).userId(user.getId()).tokenHash(hash(rawToken))
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(24, ChronoUnit.HOURS)).build());
        tokenDeliveryService.deliverEmailVerification(email, rawToken);
        return new MessageResponse("Đăng ký thành công. Vui lòng xác minh email để kích hoạt tài khoản.");
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        EmailVerificationToken verification = emailVerificationTokenRepository
                .findByTokenHashAndUsedAtIsNull(hash(token))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        if (verification.getExpiresAt().isBefore(Instant.now())) throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == AccountStatus.LOCKED || user.getStatus() == AccountStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        user.setStatus(AccountStatus.ACTIVE);
        verification.setUsedAt(Instant.now());
        return new MessageResponse("Email đã được xác minh. Bạn có thể đăng nhập.");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        String rateKey = "login:" + email;
        loginRateLimiter.check(rateKey);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (user.getStatus() == AccountStatus.LOCKED || user.getStatus() == AccountStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (user.getStatus() != AccountStatus.ACTIVE) throw new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        loginRateLimiter.reset(rateKey);
        TokenSession session = TokenSession.builder().id(UUID.randomUUID()).userId(user.getId())
                .revoked(false).expiredAt(Instant.now().plus(7, ChronoUnit.DAYS)).build();
        String refreshToken = jwtTokenProvider.generateRefreshToken(session.getId());
        session.setRefreshToken(refreshToken);
        tokenSessionRepository.save(session);
        auditLogService.log(user.getId(), AuditAction.LOGIN.name());
        return new AuthResponse(accessToken(user), refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        TokenSession session = tokenSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_EXPIRED));
        if (session.isRevoked()) throw new BusinessException(ErrorCode.TOKEN_REVOKED);
        if (session.getExpiredAt().isBefore(Instant.now())) throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != AccountStatus.ACTIVE) throw new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        return new AuthResponse(accessToken(user), refreshToken);
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email().trim().toLowerCase()).ifPresent(user -> {
            String rawToken = randomToken();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .id(UUID.randomUUID()).userId(user.getId()).tokenHash(hash(rawToken))
                    .createdAt(Instant.now()).expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES)).build());
            tokenDeliveryService.deliverPasswordReset(user.getEmail(), rawToken);
        });
        return new MessageResponse("Nếu email tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(hash(request.token()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        if (resetToken.getExpiresAt().isBefore(Instant.now())) throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(Instant.now());
        revokeSessions(user.getId());
        return new MessageResponse("Mật khẩu đã được đặt lại.");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        revokeSessions(user.getId());
        return new MessageResponse("Mật khẩu đã được thay đổi. Vui lòng đăng nhập lại trên các thiết bị khác.");
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        tokenBlacklistService.revoke(accessToken, Duration.ofMinutes(15));
        tokenSessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
            session.setRevoked(true);
            tokenSessionRepository.save(session);
        });
        try {
            auditLogService.log(jwtTokenProvider.getUserId(accessToken), AuditAction.LOGOUT.name());
        } catch (ExpiredJwtException ignored) {
        }
    }

    private String accessToken(User user) {
        Set<String> permissions = user.getRole().permissions().stream().map(Enum::name).collect(Collectors.toSet());
        String fullName = userProfileRepository.findById(user.getId())
                .map(UserProfile::getFullName)
                .filter(name -> name != null && !name.isBlank())
                .orElseGet(() -> {
                    if (user.getStudentRegistryId() != null) {
                        return studentRegistryRepository.findById(user.getStudentRegistryId())
                                .map(com.project.base_v1.entity.StudentRegistry::getFullName)
                                .orElse(null);
                    }
                    return studentRegistryRepository.findByStudentCodeIgnoreCase(user.getUsername())
                            .map(com.project.base_v1.entity.StudentRegistry::getFullName)
                            .orElse(null);
                });
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name(), permissions, fullName);
    }

    private void revokeSessions(UUID userId) {
        tokenSessionRepository.findAllByUserId(userId).forEach(session -> session.setRevoked(true));
    }

    private static String randomToken() { return UUID.randomUUID() + "" + UUID.randomUUID(); }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
