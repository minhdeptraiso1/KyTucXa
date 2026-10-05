package com.project.base_v1.dto.request.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMeterReadingRequest(
        @NotNull UUID meterId,
        @NotNull LocalDate billingPeriod,
        @NotNull @DecimalMin("0") BigDecimal currentValue,
        boolean resetRecorded,
        String note
) {}

