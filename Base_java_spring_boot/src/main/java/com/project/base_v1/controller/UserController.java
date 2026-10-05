package com.project.base_v1.controller;

import com.project.base_v1.dto.request.user.CreateUserRequest;
import com.project.base_v1.dto.request.user.UpdateUserRequest;
import com.project.base_v1.dto.request.user.UserSearchRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.dto.response.user.UserProfileResponse;
import com.project.base_v1.dto.request.user.UpdateProfileRequest;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "User",
        description = "APIs quản lý người dùng"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    // ===================== ME =====================
    @Operation(
            summary = "Lấy thông tin người dùng đang đăng nhập",
            description = """
                    Trả về thông tin người dùng hiện tại dựa trên JWT access token.
                    Client cần gửi header: Authorization: Bearer <access_token>.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin người dùng thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @GetMapping("/me")
    public ApiResponseSever<UserResponse> me() {
        return ApiResponseSever.ok(userService.getCurrentUser(CurrentUser.username()));
    }

    @Operation(summary = "Lấy profile của chính mình")
    @GetMapping("/me/profile")
    public ApiResponseSever<UserProfileResponse> myProfile() {
        return ApiResponseSever.ok(userService.getCurrentProfile(CurrentUser.username()));
    }

    @Operation(summary = "Cập nhật profile của chính mình")
    @PatchMapping("/me/profile")
    public ApiResponseSever<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponseSever.ok(userService.updateCurrentProfile(CurrentUser.username(), request));
    }

    // ===================== SEARCH =====================
    @Operation(
            summary = "Tìm kiếm người dùng",
            description = """
                    Tìm kiếm người dùng theo từ khóa, vai trò và trạng thái hoạt động.
                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm kiếm người dùng thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ApiResponseSever<Page<UserResponse>> search(
            @ParameterObject UserSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(userService.searchUsers(request, pageable));
    }

    // ===================== CREATE =====================
    @Operation(
            summary = "Tạo người dùng mới",
            description = """
                    Tạo tài khoản người dùng mới.
                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo người dùng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu gửi lên không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "409", description = "Username hoặc email đã tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponseSever<UserResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin người dùng cần tạo",
                    required = true
            )
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponseSever.ok(userService.createUser(request));
    }

    // ===================== UPDATE =====================
    @Operation(
            summary = "Cập nhật người dùng",
            description = """
                    Cập nhật thông tin người dùng theo id.
                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật người dùng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu gửi lên không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<UserResponse> update(
            @Parameter(
                    description = "ID người dùng cần cập nhật",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin người dùng cần cập nhật",
                    required = true
            )
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponseSever.ok(userService.updateUser(id, request));
    }

    // ===================== DELETE =====================
    @Operation(
            summary = "Xóa người dùng",
            description = """
                    Xóa mềm người dùng theo id.
                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa người dùng thành công"),
            @ApiResponse(responseCode = "400", description = "ID người dùng không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(
            @Parameter(
                    description = "ID người dùng cần xóa",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id
    ) {
        userService.deleteUserById(id);
        return ApiResponseSever.ok(null);
    }
}
