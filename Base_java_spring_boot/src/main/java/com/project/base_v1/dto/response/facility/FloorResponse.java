package com.project.base_v1.dto.response.facility;

import com.project.base_v1.enums.FacilityStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FloorResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        String buildingName,
        int floorNumber,
        String name,
        FacilityStatus status,
        long roomCount,
        long totalBeds,
        long availableBeds,
        long occupiedBeds,
        Instant createdAt,
        Instant updatedAt
) {}
