package com.project.base_v1.dto.request.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GenerateInvoiceRequest(
        @NotNull UUID contractId,
        @NotNull LocalDate billingPeriod,
        @NotNull LocalDate dueDate,
        @DecimalMin("0") BigDecimal serviceFee,
        @DecimalMin("0") BigDecimal discount,
        @DecimalMin("0") BigDecimal fineAmount
) {}

