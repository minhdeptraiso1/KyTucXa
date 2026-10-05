package com.project.base_v1.controller;

import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.request.auth.ChangePasswordRequest;
import com.project.base_v1.dto.request.auth.ForgotPasswordRequest;
import com.project.base_v1.dto.request.auth.RegisterRequest;
import com.project.base_v1.dto.request.auth.ResetPasswordRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.core.MessageResponse;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Auth",
        description = "API xác thực, đăng nhập, làm mới token và đăng xuất"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    // ===================== LOGIN =====================
    @Operation(
            summary = "Đăng nhập",
            description = """
                    Đăng nhập bằng email và mật khẩu.
                    Nếu thông tin hợp lệ, hệ thống trả về access token và refresh token.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu gửi lên không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng"),
            @ApiResponse(responseCode = "403", description = "Tài khoản đã bị khóa"),
            @ApiResponse(responseCode = "429", description = "Đăng nhập sai quá nhiều lần"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping("/login")
    public ApiResponseSever<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đăng nhập",
                    required = true
            )
            @RequestBody @Valid LoginRequest request
    ) {
        return ApiResponseSever.ok(authService.login(request));
    }

    @Operation(summary = "Đăng ký tài khoản USER")
    @PostMapping("/register")
    public ApiResponseSever<MessageResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponseSever.ok(authService.register(request));
    }

    @Operation(summary = "Xác minh email")
    @PostMapping("/verify-email")
    public ApiResponseSever<MessageResponse> verifyEmail(@RequestParam String token) {
        return ApiResponseSever.ok(authService.verifyEmail(token));
    }

    @Operation(summary = "Yêu cầu đặt lại mật khẩu")
    @PostMapping("/forgot-password")
    public ApiResponseSever<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        return ApiResponseSever.ok(authService.forgotPassword(request));
    }

    @Operation(summary = "Đặt lại mật khẩu")
    @PostMapping("/reset-password")
    public ApiResponseSever<MessageResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ApiResponseSever.ok(authService.resetPassword(request));
    }

    @Operation(summary = "Đổi mật khẩu")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ApiResponseSever<MessageResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return ApiResponseSever.ok(authService.changePassword(CurrentUser.username(), request));
    }

    // ===================== REFRESH TOKEN =====================
    @Operation(
            summary = "Làm mới access token",
            description = """
                    Cấp access token mới dựa trên refresh token còn hiệu lực.
                    Refresh token phải chưa hết hạn và chưa bị thu hồi.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Làm mới access token thành công"),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ, hết hạn hoặc đã bị thu hồi"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping("/refresh")
    public ApiResponseSever<AuthResponse> refresh(
            @Parameter(
                    description = "Refresh token được cấp khi đăng nhập",
                    required = true,
                    example = "eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestParam String refreshToken
    ) {
        return ApiResponseSever.ok(authService.refresh(refreshToken));
    }

    // ===================== LOGOUT =====================
    @Operation(
            summary = "Đăng xuất",
            description = """
                    Đăng xuất khỏi hệ thống.
                    Access token hiện tại sẽ được đưa vào blacklist, refresh token sẽ bị thu hồi.
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PostMapping("/logout")
    public ApiResponseSever<Void> logout(
            @Parameter(
                    description = "Access token theo định dạng: Bearer <access_token>",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestHeader("Authorization") String authHeader,

            @Parameter(
                    description = "Refresh token cần thu hồi",
                    required = true,
                    example = "eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestParam String refreshToken
    ) {
        String accessToken = authHeader.substring(7);

        authService.logout(accessToken, refreshToken);

        return ApiResponseSever.ok(null);
    }
}
