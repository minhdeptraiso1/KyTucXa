package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    UserResponse toResponse(User user);

}
