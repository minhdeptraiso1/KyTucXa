package com.project.base_v1.dto.request.facility;

import com.project.base_v1.enums.FacilityStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateFloorRequest(
        @jakarta.validation.constraints.NotNull(message = "Số tầng không được để trống")
        @Positive(message = "Số tầng phải lớn hơn 0")
        Integer floorNumber,

        @Size(max = 100, message = "Tên tầng tối đa 100 ký tự")
        String name,

        FacilityStatus status
) {}
