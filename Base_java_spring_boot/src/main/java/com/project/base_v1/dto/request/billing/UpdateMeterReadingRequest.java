package com.project.base_v1.dto.request.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateMeterReadingRequest(
        @NotNull @DecimalMin("0") BigDecimal currentValue,
        boolean resetRecorded,
        String note
) {}
