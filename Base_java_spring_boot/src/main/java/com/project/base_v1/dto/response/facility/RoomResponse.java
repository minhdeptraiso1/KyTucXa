package com.project.base_v1.dto.response.facility;

import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record RoomResponse(
        UUID id,
        UUID floorId,
        int floorNumber,
        String floorName,
        UUID buildingId,
        String buildingCode,
        String buildingName,
        String roomNumber,
        RoomType roomType,
        int capacity,
        int currentOccupancy,
        int availableBeds,
        GenderType genderType,
        RoomStatus status,
        BigDecimal areaSqm,
        UUID pricePolicyId,
        String pricePolicyName,
        BigDecimal pricePerMonth,
        String imageUrl,
        String description,
        String notes,
        long bedCount,
        Instant createdAt,
        Instant updatedAt
) {}
