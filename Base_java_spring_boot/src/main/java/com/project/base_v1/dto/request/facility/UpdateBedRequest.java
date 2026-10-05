package com.project.base_v1.dto.request.facility;

import com.project.base_v1.enums.BedStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBedRequest(
        @NotBlank(message = "Số giường không được để trống")
        @Size(max = 10, message = "Số giường tối đa 10 ký tự")
        String bedNumber,

        @NotNull(message = "Trạng thái giường không được để trống")
        BedStatus status,

        String notes
) {}
