package com.project.base_v1.dto.response.facility;

import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PublicRoomSummaryResponse(
        UUID id,
        String buildingCode,
        String buildingName,
        int floorNumber,
        String roomNumber,
        RoomType roomType,
        int capacity,
        int availableBeds,
        GenderType genderType,
        RoomStatus status,
        BigDecimal areaSqm,
        BigDecimal pricePerMonth,
        String imageUrl,
        String description
) {}
