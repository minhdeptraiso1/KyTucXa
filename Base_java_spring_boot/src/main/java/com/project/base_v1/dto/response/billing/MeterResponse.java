package com.project.base_v1.dto.response.billing;

import com.project.base_v1.enums.MeterStatus;
import com.project.base_v1.enums.UtilityType;
import java.util.UUID;

public record MeterResponse(UUID id, UUID roomId, String roomNumber, String buildingCode,
                            String meterCode, UtilityType utilityType, String unit, MeterStatus status) {}

