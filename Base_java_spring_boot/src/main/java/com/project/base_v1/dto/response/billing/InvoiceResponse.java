package com.project.base_v1.dto.response.billing;

import com.project.base_v1.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id, String invoiceCode, UUID userId, String studentCode, String studentName,
        UUID contractId, String contractCode, String roomNumber, LocalDate billingPeriod, LocalDate dueDate,
        BigDecimal subtotal, BigDecimal discount, BigDecimal fineAmount, BigDecimal totalAmount,
        BigDecimal paidAmount, BigDecimal remainingAmount, InvoiceStatus status,
        List<InvoiceItemResponse> items
) {}

