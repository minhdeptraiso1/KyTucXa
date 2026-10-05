package com.project.base_v1.dto.request.student;

import com.project.base_v1.enums.StudentRegistryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStudentStatusRequest(
        @NotNull(message = "Trạng thái không được để trống")
        StudentRegistryStatus status
) {
}
