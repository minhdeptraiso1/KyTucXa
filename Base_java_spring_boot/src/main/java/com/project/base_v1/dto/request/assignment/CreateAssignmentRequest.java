package com.project.base_v1.dto.request.assignment;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAssignmentRequest(
        @NotNull UUID registrationId,
        @NotNull UUID bedId,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}
