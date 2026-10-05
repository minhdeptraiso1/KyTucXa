package com.project.base_v1.controller;

import com.project.base_v1.dto.request.facility.CreateBuildingRequest;
import com.project.base_v1.dto.request.facility.UpdateBuildingRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.BuildingResponse;
import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Facility - Building", description = "APIs quản lý tòa nhà ký túc xá")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuildingController {

    BuildingService buildingService;

    @Operation(summary = "Tạo mới tòa nhà")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<BuildingResponse> createBuilding(@Valid @RequestBody CreateBuildingRequest request) {
        return ApiResponseSever.ok(buildingService.createBuilding(request));
    }

    @Operation(summary = "Cập nhật thông tin tòa nhà")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<BuildingResponse> updateBuilding(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBuildingRequest request) {
        return ApiResponseSever.ok(buildingService.updateBuilding(id, request));
    }

    @Operation(summary = "Lấy chi tiết tòa nhà theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<BuildingResponse> getBuildingById(@PathVariable UUID id) {
        return ApiResponseSever.ok(buildingService.getBuildingById(id));
    }

    @Operation(summary = "Lấy danh sách tòa nhà có phân trang, tìm kiếm và lọc")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<Page<BuildingResponse>> getBuildings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FacilityStatus status,
            @RequestParam(required = false) GenderType genderType,
            @ParameterObject Pageable pageable) {
        return ApiResponseSever.ok(buildingService.getBuildings(search, status, genderType, pageable));
    }

    @Operation(summary = "Lấy danh sách tất cả tòa nhà đang hoạt động")
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<List<BuildingResponse>> getAllActiveBuildings() {
        return ApiResponseSever.ok(buildingService.getAllActiveBuildings());
    }

    @Operation(summary = "Xóa tòa nhà (chỉ khi không có tầng nào)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Void> deleteBuilding(@PathVariable UUID id) {
        buildingService.deleteBuilding(id);
        return ApiResponseSever.ok(null);
    }
}
