package com.project.base_v1.dto.request.registration;

import com.project.base_v1.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRegistrationRequest(
        @NotNull RegistrationStatus decision,
        @Size(max = 1000) String rejectionReason
) {}
