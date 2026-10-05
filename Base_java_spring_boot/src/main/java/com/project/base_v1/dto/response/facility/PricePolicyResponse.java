package com.project.base_v1.dto.response.facility;

import com.project.base_v1.enums.RoomType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record PricePolicyResponse(
        UUID id,
        String name,
        RoomType roomType,
        BigDecimal pricePerMonth,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean isActive,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
