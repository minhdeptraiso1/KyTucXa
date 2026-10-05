package com.project.base_v1.controller;

import com.project.base_v1.dto.request.facility.CreatePricePolicyRequest;
import com.project.base_v1.dto.request.facility.UpdatePricePolicyRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.PricePolicyResponse;
import com.project.base_v1.service.PricePolicyService;
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

@Tag(name = "Facility - Price Policy", description = "APIs quản lý chính sách bảng giá phòng ký túc xá")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/price-policies")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PricePolicyController {

    PricePolicyService pricePolicyService;

    @Operation(summary = "Tạo mới chính sách giá phòng")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<PricePolicyResponse> createPolicy(@Valid @RequestBody CreatePricePolicyRequest request) {
        return ApiResponseSever.ok(pricePolicyService.createPolicy(request));
    }

    @Operation(summary = "Cập nhật chính sách giá")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<PricePolicyResponse> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePricePolicyRequest request) {
        return ApiResponseSever.ok(pricePolicyService.updatePolicy(id, request));
    }

    @Operation(summary = "Lấy chi tiết chính sách giá theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<PricePolicyResponse> getPolicyById(@PathVariable UUID id) {
        return ApiResponseSever.ok(pricePolicyService.getPolicyById(id));
    }

    @Operation(summary = "Lấy tất cả chính sách giá")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<List<PricePolicyResponse>> getAllPolicies() {
        return ApiResponseSever.ok(pricePolicyService.getAllPolicies());
    }

    @Operation(summary = "Lấy các chính sách giá đang có hiệu lực")
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<List<PricePolicyResponse>> getActivePolicies() {
        return ApiResponseSever.ok(pricePolicyService.getActivePolicies());
    }

    @Operation(summary = "Xóa chính sách giá (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Void> deletePolicy(@PathVariable UUID id) {
        pricePolicyService.deletePolicy(id);
        return ApiResponseSever.ok(null);
    }
}
