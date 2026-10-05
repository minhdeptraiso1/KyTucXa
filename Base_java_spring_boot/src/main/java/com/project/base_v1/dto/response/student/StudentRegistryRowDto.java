package com.project.base_v1.dto.response.student;

import com.project.base_v1.enums.StudentRegistryStatus;
import lombok.Builder;

@Builder
public record StudentRegistryRowDto(
        int rowNumber,
        String studentCode,
        String email,
        String phone,
        String fullName,
        String className,
        StudentRegistryStatus status,
        boolean isValid,
        boolean isDuplicate,
        boolean isExisting,
        String error
) {
}
