package com.project.base_v1.controller;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.BuildingResponse;
import com.project.base_v1.dto.response.facility.FacilityOverviewStatsResponse;
import com.project.base_v1.dto.response.facility.PricePolicyResponse;
import com.project.base_v1.dto.response.facility.PublicRoomSummaryResponse;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomType;
import com.project.base_v1.service.BuildingService;
import com.project.base_v1.service.FacilityStatsService;
import com.project.base_v1.service.PricePolicyService;
import com.project.base_v1.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Facility", description = "APIs công khai thông tin ký túc xá cho sinh viên và khách tham quan")
@RestController
@RequestMapping("/public/facilities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicFacilityController {

    RoomService roomService;
    BuildingService buildingService;
    PricePolicyService pricePolicyService;
    FacilityStatsService facilityStatsService;

    @Operation(summary = "Xem thống kê tổng quan KTX (Public)")
    @GetMapping("/overview")
    public ApiResponseSever<FacilityOverviewStatsResponse> getOverview() {
        return ApiResponseSever.ok(facilityStatsService.getOverviewStats());
    }

    @Operation(summary = "Xem danh sách các phòng còn chỗ khả dụng (Public)")
    @GetMapping("/rooms")
    public ApiResponseSever<List<PublicRoomSummaryResponse>> getAvailableRooms(
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) GenderType genderType) {
        return ApiResponseSever.ok(roomService.getPublicAvailableRooms(roomType, genderType));
    }

    @Operation(summary = "Xem danh sách các tòa nhà KTX (Public)")
    @GetMapping("/buildings")
    public ApiResponseSever<List<BuildingResponse>> getActiveBuildings() {
        return ApiResponseSever.ok(buildingService.getAllActiveBuildings());
    }

    @Operation(summary = "Xem bảng giá phòng KTX hiện hành (Public)")
    @GetMapping("/price-policies")
    public ApiResponseSever<List<PricePolicyResponse>> getActivePricePolicies() {
        return ApiResponseSever.ok(pricePolicyService.getActivePolicies());
    }
}
