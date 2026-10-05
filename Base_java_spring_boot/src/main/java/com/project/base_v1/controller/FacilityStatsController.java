package com.project.base_v1.controller;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.FacilityOverviewStatsResponse;
import com.project.base_v1.service.FacilityStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Facility - Statistics", description = "APIs thống kê cơ sở vật chất KTX")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/facility-stats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FacilityStatsController {

    FacilityStatsService facilityStatsService;

    @Operation(summary = "Lấy thống kê tổng quan cơ sở vật chất (Số tòa, phòng, giường, tỷ lệ lấp đầy)")
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<FacilityOverviewStatsResponse> getOverviewStats() {
        return ApiResponseSever.ok(facilityStatsService.getOverviewStats());
    }
}
