package com.project.base_v1.dto.request.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTariffRequest(
        @NotNull @DecimalMin("0") BigDecimal unitPrice,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active
) {}
