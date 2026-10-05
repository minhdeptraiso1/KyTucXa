package com.project.base_v1.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Size(min = 3, max = 100) String username,
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") @Size(max = 255) String email,
        @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") String password,
        String confirmPassword,
        @NotBlank(message = "Họ và tên không được để trống") @Size(max = 150) String fullName,
        @NotBlank(message = "Mã sinh viên không được để trống") @Size(max = 50) String studentCode,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9+() .-]{8,20}$", message = "Số điện thoại không hợp lệ") String phone
) {
}
