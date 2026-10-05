package com.project.base_v1.controller;

import com.project.base_v1.dto.request.facility.BatchCreateBedsRequest;
import com.project.base_v1.dto.request.facility.CreateBedRequest;
import com.project.base_v1.dto.request.facility.UpdateBedRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.BedResponse;
import com.project.base_v1.enums.BedStatus;
import com.project.base_v1.service.BedService;
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

@Tag(name = "Facility - Bed", description = "APIs quản lý giường ký túc xá")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/beds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BedController {

    BedService bedService;

    @Operation(summary = "Tạo mới một giường trong phòng")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<BedResponse> createBed(@Valid @RequestBody CreateBedRequest request) {
        return ApiResponseSever.ok(bedService.createBed(request));
    }

    @Operation(summary = "Tạo hàng loạt giường cho phòng")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<List<BedResponse>> batchCreateBeds(@Valid @RequestBody BatchCreateBedsRequest request) {
        return ApiResponseSever.ok(bedService.batchCreateBeds(request));
    }

    @Operation(summary = "Cập nhật thông tin giường")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<BedResponse> updateBed(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBedRequest request) {
        return ApiResponseSever.ok(bedService.updateBed(id, request));
    }

    @Operation(summary = "Cập nhật trạng thái giường (AVAILABLE, MAINTENANCE, INACTIVE)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<BedResponse> updateBedStatus(
            @PathVariable UUID id,
            @RequestParam BedStatus status) {
        return ApiResponseSever.ok(bedService.updateBedStatus(id, status));
    }

    @Operation(summary = "Lấy chi tiết giường theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<BedResponse> getBedById(@PathVariable UUID id) {
        return ApiResponseSever.ok(bedService.getBedById(id));
    }

    @Operation(summary = "Lấy danh sách giường trong một phòng")
    @GetMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<List<BedResponse>> getBedsByRoom(@PathVariable UUID roomId) {
        return ApiResponseSever.ok(bedService.getBedsByRoom(roomId));
    }

    @Operation(summary = "Xóa giường (chỉ khi không có người ở)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Void> deleteBed(@PathVariable UUID id) {
        bedService.deleteBed(id);
        return ApiResponseSever.ok(null);
    }
}
