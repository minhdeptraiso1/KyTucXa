package com.project.base_v1.dto.request.facility;

import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRoomRequest(
        @NotNull(message = "ID tầng không được để trống")
        UUID floorId,

        @NotBlank(message = "Số phòng không được để trống")
        @Size(max = 20, message = "Số phòng tối đa 20 ký tự")
        String roomNumber,

        @NotNull(message = "Loại phòng không được để trống")
        RoomType roomType,

        @NotNull(message = "Sức chứa không được để trống")
        @Positive(message = "Sức chứa phải lớn hơn 0")
        Integer capacity,

        @NotNull(message = "Loại giới tính không được để trống")
        GenderType genderType,

        @Positive(message = "Diện tích phải lớn hơn 0")
        BigDecimal areaSqm,

        UUID pricePolicyId,

        @Size(max = 1000, message = "URL ảnh tối đa 1000 ký tự")
        String imageUrl,

        String description,

        String notes,

        // Optional: Tự động khởi tạo số lượng giường tương ứng với sức chứa
        Boolean autoGenerateBeds
) {}
