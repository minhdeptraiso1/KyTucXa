package com.project.base_v1.dto.response.contract;

import com.project.base_v1.enums.ContractStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        String contractCode,
        UUID userId,
        String studentCode,
        String fullName,
        UUID assignmentId,
        String roomNumber,
        String bedNumber,
        String buildingCode,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal rentalPrice,
        BigDecimal deposit,
        ContractStatus status,
        Instant terminatedAt,
        String terminationReason,
        Instant createdAt
) {}
