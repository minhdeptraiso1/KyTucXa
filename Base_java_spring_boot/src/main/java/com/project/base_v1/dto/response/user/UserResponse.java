package com.project.base_v1.dto.response.user;

import com.project.base_v1.enums.UserRole;
import com.project.base_v1.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record UserResponse(

        UUID id,

        String username,

        String email,

        AccountStatus status,

        UserRole role
) {
}

