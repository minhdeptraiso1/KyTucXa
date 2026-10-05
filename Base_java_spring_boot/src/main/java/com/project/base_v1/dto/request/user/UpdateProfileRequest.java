package com.project.base_v1.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        LocalDate dateOfBirth,
        @Size(max = 20) String gender,
        @Pattern(regexp = "^$|^[0-9+() .-]{8,20}$", message = "Số điện thoại không hợp lệ") String phone,
        @Size(max = 500) String address,
        @Size(max = 150) String emergencyContactName,
        @Pattern(regexp = "^$|^[0-9+() .-]{8,20}$", message = "Số điện thoại liên hệ không hợp lệ") String emergencyContactPhone
) {
}
