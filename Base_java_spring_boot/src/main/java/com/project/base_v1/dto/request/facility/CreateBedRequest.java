package com.project.base_v1.dto.request.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBedRequest(
        @NotNull(message = "ID phòng không được để trống")
        UUID roomId,

        @NotBlank(message = "Số giường không được để trống")
        @Size(max = 10, message = "Số giường tối đa 10 ký tự")
        String bedNumber,

        String notes
) {}
