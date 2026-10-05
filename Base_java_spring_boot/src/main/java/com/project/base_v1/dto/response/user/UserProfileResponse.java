package com.project.base_v1.dto.response.user;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String fullName,
        String studentCode,
        LocalDate dateOfBirth,
        String gender,
        String phone,
        String identityNumber,
        String faculty,
        String className,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {
}
