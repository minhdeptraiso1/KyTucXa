package com.project.base_v1.dto.response.billing;

import com.project.base_v1.enums.PaymentMethod;
import com.project.base_v1.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID id, String paymentCode, UUID invoiceId, String invoiceCode,
                              UUID userId, BigDecimal amount, PaymentMethod method, PaymentStatus status,
                              String externalReference, Instant paidAt, Instant createdAt) {}

