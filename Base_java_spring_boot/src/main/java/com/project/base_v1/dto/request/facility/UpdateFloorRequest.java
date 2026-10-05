package com.project.base_v1.dto.request.facility;

import com.project.base_v1.enums.FacilityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFloorRequest(
        @Size(max = 100, message = "Tên tầng tối đa 100 ký tự")
        String name,

        @NotNull(message = "Trạng thái tầng không được để trống")
        FacilityStatus status
) {}
