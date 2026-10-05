package com.project.base_v1.dto.response.billing;

import com.project.base_v1.enums.UtilityType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MeterReadingResponse(UUID id, UUID meterId, String meterCode, UtilityType utilityType,
                                   LocalDate billingPeriod, BigDecimal previousValue, BigDecimal currentValue,
                                   BigDecimal consumption, boolean resetRecorded, String note, Instant readAt) {}

