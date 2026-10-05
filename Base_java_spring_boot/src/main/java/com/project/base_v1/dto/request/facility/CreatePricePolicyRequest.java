package com.project.base_v1.dto.request.facility;

import com.project.base_v1.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePricePolicyRequest(
        @NotBlank(message = "Tên chính sách giá không được để trống")
        @Size(max = 200, message = "Tên chính sách giá tối đa 200 ký tự")
        String name,

        @NotNull(message = "Loại phòng không được để trống")
        RoomType roomType,

        @NotNull(message = "Đơn giá theo tháng không được để trống")
        @DecimalMin(value = "1000", message = "Đơn giá tối thiểu là 1,000 VND")
        BigDecimal pricePerMonth,

        @NotNull(message = "Ngày bắt đầu áp dụng không được để trống")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        Boolean isActive,

        String description
) {}
