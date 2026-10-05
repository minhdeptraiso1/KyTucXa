package com.project.base_v1.dto.request.facility;

import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateBuildingRequest(
        @NotBlank(message = "Tên tòa nhà không được để trống")
        @Size(max = 200, message = "Tên tòa nhà tối đa 200 ký tự")
        String name,

        @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
        String address,

        @NotNull(message = "Loại giới tính không được để trống")
        GenderType genderType,

        @NotNull(message = "Số tầng không được để trống")
        @Positive(message = "Số tầng phải lớn hơn 0")
        Integer totalFloors,

        @NotNull(message = "Trạng thái không được để trống")
        FacilityStatus status,

        String description
) {}
