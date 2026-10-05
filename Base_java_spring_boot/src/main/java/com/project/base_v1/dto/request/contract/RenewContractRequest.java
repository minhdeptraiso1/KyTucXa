package com.project.base_v1.dto.request.contract;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RenewContractRequest(@NotNull LocalDate newEndDate) {}
