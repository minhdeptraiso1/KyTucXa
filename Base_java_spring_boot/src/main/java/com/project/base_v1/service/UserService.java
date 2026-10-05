package com.project.base_v1.service;

import com.project.base_v1.dto.request.user.CreateUserRequest;
import com.project.base_v1.dto.request.user.UpdateUserRequest;
import com.project.base_v1.dto.request.user.UserSearchRequest;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.dto.response.user.UserProfileResponse;
import com.project.base_v1.dto.request.user.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserResponse> searchUsers(
            UserSearchRequest request,
            Pageable pageable
    );

    void deleteUserById(UUID userId);

    UserResponse getCurrentUser(String username);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    UserProfileResponse getCurrentProfile(String username);

    UserProfileResponse updateCurrentProfile(String username, UpdateProfileRequest request);
}
