package com.project.base_v1.dto.response.facility;

import com.project.base_v1.enums.BedStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record BedResponse(
        UUID id,
        UUID roomId,
        String roomNumber,
        int floorNumber,
        String buildingCode,
        String buildingName,
        String bedNumber,
        BedStatus status,
        boolean isUsable,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
