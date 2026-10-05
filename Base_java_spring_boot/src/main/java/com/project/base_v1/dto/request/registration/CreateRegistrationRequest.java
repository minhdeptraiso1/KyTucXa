package com.project.base_v1.dto.request.registration;

import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateRegistrationRequest(
        @NotNull RoomType requestedRoomType,
        @NotNull GenderType requestedGenderType,
        @NotNull @FutureOrPresent LocalDate preferredStartDate,
        LocalDate preferredEndDate,
        @Size(max = 1000) String reason
) {}
