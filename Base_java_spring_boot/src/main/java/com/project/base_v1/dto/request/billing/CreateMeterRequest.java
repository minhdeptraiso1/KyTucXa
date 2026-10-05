package com.project.base_v1.dto.request.billing;

import com.project.base_v1.enums.UtilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMeterRequest(
        @NotNull UUID roomId,
        @NotBlank String meterCode,
        @NotNull UtilityType utilityType,
        @NotBlank String unit
) {}

