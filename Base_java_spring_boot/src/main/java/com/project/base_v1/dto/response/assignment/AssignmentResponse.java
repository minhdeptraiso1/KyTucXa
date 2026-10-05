package com.project.base_v1.dto.response.assignment;

import com.project.base_v1.enums.AssignmentStatus;
import java.time.*;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID registrationId,
        UUID userId,
        String studentCode,
        String fullName,
        UUID bedId,
        String bedNumber,
        String roomNumber,
        String buildingCode,
        int floorNumber,
        LocalDate startDate,
        LocalDate endDate,
        AssignmentStatus status,
        UUID assignedBy,
        Instant createdAt
) {}
