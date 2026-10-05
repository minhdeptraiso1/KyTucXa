package com.project.base_v1.controller;

import com.project.base_v1.dto.request.facility.CreateFloorRequest;
import com.project.base_v1.dto.request.facility.UpdateFloorRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.FloorResponse;
import com.project.base_v1.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Facility - Floor", description = "APIs quản lý tầng của tòa nhà ký túc xá")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/floors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FloorController {

    FloorService floorService;

    @Operation(summary = "Tạo mới tầng trong tòa nhà")
    @PostMapping("/buildings/{buildingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<FloorResponse> createFloor(
            @PathVariable UUID buildingId,
            @Valid @RequestBody CreateFloorRequest request) {
        return ApiResponseSever.ok(floorService.createFloor(buildingId, request));
    }

    @Operation(summary = "Cập nhật thông tin tầng")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<FloorResponse> updateFloor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFloorRequest request) {
        return ApiResponseSever.ok(floorService.updateFloor(id, request));
    }

    @Operation(summary = "Lấy chi tiết tầng theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<FloorResponse> getFloorById(@PathVariable UUID id) {
        return ApiResponseSever.ok(floorService.getFloorById(id));
    }

    @Operation(summary = "Lấy danh sách tầng theo ID tòa nhà")
    @GetMapping("/buildings/{buildingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<List<FloorResponse>> getFloorsByBuilding(@PathVariable UUID buildingId) {
        return ApiResponseSever.ok(floorService.getFloorsByBuilding(buildingId));
    }

    @Operation(summary = "Xóa tầng (chỉ khi không có phòng nào)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Void> deleteFloor(@PathVariable UUID id) {
        floorService.deleteFloor(id);
        return ApiResponseSever.ok(null);
    }
}
