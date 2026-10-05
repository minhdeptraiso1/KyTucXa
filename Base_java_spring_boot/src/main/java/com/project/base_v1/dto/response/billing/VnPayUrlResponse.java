package com.project.base_v1.dto.response.billing;

import java.util.UUID;

public record VnPayUrlResponse(UUID paymentId, String paymentCode, String paymentUrl) {}

