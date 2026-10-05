package com.project.base_v1.dto.response.billing;

import com.project.base_v1.enums.UtilityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TariffResponse(UUID id, UtilityType utilityType, BigDecimal unitPrice,
                             LocalDate effectiveFrom, LocalDate effectiveTo, boolean active) {}

