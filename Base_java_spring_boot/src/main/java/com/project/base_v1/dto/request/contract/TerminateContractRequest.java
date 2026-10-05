package com.project.base_v1.dto.request.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TerminateContractRequest(@NotBlank @Size(max = 1000) String reason) {}
