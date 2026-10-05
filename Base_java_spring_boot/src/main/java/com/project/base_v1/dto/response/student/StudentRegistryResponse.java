package com.project.base_v1.dto.response.student;

import com.project.base_v1.enums.StudentRegistryStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record StudentRegistryResponse(
        UUID id,
        String studentCode,
        String fullName,
        String email,
        String phone,
        String className,
        StudentRegistryStatus status,
        boolean hasRegisteredAccount,
        Instant createdAt,
        Instant updatedAt
) {
}
