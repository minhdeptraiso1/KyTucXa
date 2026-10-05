package com.project.base_v1.controller;

import com.project.base_v1.dto.request.contract.*;
import com.project.base_v1.dto.response.contract.ContractResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.enums.ContractStatus;
import com.project.base_v1.service.ContractService;
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

@Tag(name = "Contract", description = "Quản lý vòng đời hợp đồng nội trú")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<ContractResponse> create(@Valid @RequestBody CreateContractRequest request) {
        return ApiResponseSever.ok(contractService.create(request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<ContractResponse> activate(@PathVariable UUID id) {
        return ApiResponseSever.ok(contractService.activate(id));
    }

    @PatchMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<ContractResponse> renew(
            @PathVariable UUID id, @Valid @RequestBody RenewContractRequest request) {
        return ApiResponseSever.ok(contractService.renew(id, request));
    }

    @PatchMapping("/{id}/terminate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<ContractResponse> terminate(
            @PathVariable UUID id, @Valid @RequestBody TerminateContractRequest request) {
        return ApiResponseSever.ok(contractService.terminate(id, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<List<ContractResponse>> mine() {
        return ApiResponseSever.ok(contractService.getMyContracts());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Page<ContractResponse>> all(
            @RequestParam(required = false) ContractStatus status,
            @ParameterObject Pageable pageable) {
        return ApiResponseSever.ok(contractService.getAll(status, pageable));
    }
}
