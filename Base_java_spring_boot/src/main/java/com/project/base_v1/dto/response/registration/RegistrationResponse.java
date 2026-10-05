package com.project.base_v1.dto.response.registration;

import com.project.base_v1.enums.*;
import java.time.*;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UUID userId,
        String studentCode,
        String fullName,
        String email,
        RoomType requestedRoomType,
        GenderType requestedGenderType,
        LocalDate preferredStartDate,
        LocalDate preferredEndDate,
        String reason,
        RegistrationStatus status,
        String rejectionReason,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt
) {}
