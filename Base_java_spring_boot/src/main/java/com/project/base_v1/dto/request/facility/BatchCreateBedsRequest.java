package com.project.base_v1.dto.request.facility;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BatchCreateBedsRequest(
        @NotNull(message = "ID phòng không được để trống")
        UUID roomId,

        @Size(max = 5, message = "Tiền tố tên giường tối đa 5 ký tự")
        String prefix,

        @NotNull(message = "Số lượng giường không được để trống")
        @Positive(message = "Số lượng giường phải lớn hơn 0")
        @Max(value = 16, message = "Số lượng giường tối đa là 16")
        Integer count
) {}
