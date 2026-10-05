package com.project.base_v1.dto.request.billing;

import com.project.base_v1.enums.MeterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMeterRequest(
        @NotBlank String meterCode,
        @NotBlank String unit,
        @NotNull MeterStatus status
) {}
