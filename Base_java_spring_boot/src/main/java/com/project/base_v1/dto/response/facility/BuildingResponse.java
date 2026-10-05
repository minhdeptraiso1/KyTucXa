package com.project.base_v1.dto.response.facility;

import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record BuildingResponse(
        UUID id,
        String code,
        String name,
        String address,
        GenderType genderType,
        int totalFloors,
        FacilityStatus status,
        String description,
        long floorCount,
        long roomCount,
        long totalBeds,
        long availableBeds,
        long occupiedBeds,
        Instant createdAt,
        Instant updatedAt
) {}
