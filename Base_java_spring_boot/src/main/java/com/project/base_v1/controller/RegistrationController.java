package com.project.base_v1.controller;

import com.project.base_v1.dto.request.registration.CreateRegistrationRequest;
import com.project.base_v1.dto.request.registration.ReviewRegistrationRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.registration.RegistrationResponse;
import com.project.base_v1.enums.RegistrationStatus;
import com.project.base_v1.service.RegistrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Registration", description = "Đăng ký nội trú và xét duyệt hồ sơ")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<RegistrationResponse> createMyRegistration(
            @Valid @RequestBody CreateRegistrationRequest request) {
        return ApiResponseSever.ok(registrationService.createMyRegistration(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<List<RegistrationResponse>> getMyRegistrations() {
        return ApiResponseSever.ok(registrationService.getMyRegistrations());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<RegistrationResponse> cancel(@PathVariable UUID id) {
        return ApiResponseSever.ok(registrationService.cancelMyRegistration(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Page<RegistrationResponse>> getQueue(
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable) {
        return ApiResponseSever.ok(registrationService.getQueue(status, keyword, pageable));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<RegistrationResponse> review(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRegistrationRequest request) {
        return ApiResponseSever.ok(registrationService.review(id, request));
    }
}
