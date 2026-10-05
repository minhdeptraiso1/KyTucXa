package com.project.base_v1.dto.request.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull UUID invoiceId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String idempotencyKey
) {}

