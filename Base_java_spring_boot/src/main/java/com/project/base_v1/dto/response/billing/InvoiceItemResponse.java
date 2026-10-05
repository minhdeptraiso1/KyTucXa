package com.project.base_v1.dto.response.billing;

import com.project.base_v1.enums.InvoiceItemType;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(UUID id, InvoiceItemType itemType, String description,
                                  BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount) {}

