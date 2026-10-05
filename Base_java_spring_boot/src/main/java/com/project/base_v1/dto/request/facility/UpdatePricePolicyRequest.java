package com.project.base_v1.dto.request.facility;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePricePolicyRequest(
        @NotBlank(message = "Tên chính sách giá không được để trống")
        @Size(max = 200, message = "Tên chính sách giá tối đa 200 ký tự")
        String name,

        @NotNull(message = "Đơn giá theo tháng không được để trống")
        @DecimalMin(value = "1000", message = "Đơn giá tối thiểu là 1,000 VND")
        BigDecimal pricePerMonth,

        LocalDate effectiveTo,

        @NotNull(message = "Trạng thái kích hoạt không được để trống")
        Boolean isActive,

        String description
) {}
