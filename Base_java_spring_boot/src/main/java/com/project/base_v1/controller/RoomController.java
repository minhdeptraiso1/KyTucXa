package com.project.base_v1.controller;

import com.project.base_v1.dto.request.facility.CreateRoomRequest;
import com.project.base_v1.dto.request.facility.UpdateRoomRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.facility.RoomResponse;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import com.project.base_v1.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Facility - Room", description = "APIs quản lý phòng ký túc xá")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomController {

    RoomService roomService;

    @Operation(summary = "Tạo mới phòng ký túc xá")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponseSever.ok(roomService.createRoom(request));
    }

    @Operation(summary = "Cập nhật thông tin phòng")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<RoomResponse> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomRequest request) {
        return ApiResponseSever.ok(roomService.updateRoom(id, request));
    }

    @Operation(summary = "Cập nhật nhanh trạng thái phòng (Bảo trì, ngưng hoạt động, sẵn sàng)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Void> updateRoomStatus(
            @PathVariable UUID id,
            @RequestParam RoomStatus status) {
        roomService.updateRoomStatus(id, status);
        return ApiResponseSever.ok(null);
    }

    @Operation(summary = "Lấy chi tiết phòng theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<RoomResponse> getRoomById(@PathVariable UUID id) {
        return ApiResponseSever.ok(roomService.getRoomById(id));
    }

    @Operation(summary = "Lấy danh sách phòng có phân trang và bộ lọc nâng cao")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<Page<RoomResponse>> getRooms(
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) UUID floorId,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) GenderType genderType,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return ApiResponseSever.ok(roomService.getRooms(buildingId, floorId, status, roomType, genderType, search, pageable));
    }

    @Operation(summary = "Lấy danh sách phòng theo tầng")
    @GetMapping("/floors/{floorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<List<RoomResponse>> getRoomsByFloor(@PathVariable UUID floorId) {
        return ApiResponseSever.ok(roomService.getRoomsByFloor(floorId));
    }

    @Operation(summary = "Xóa phòng (chỉ khi không có giường nào)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Void> deleteRoom(@PathVariable UUID id) {
        roomService.deleteRoom(id);
        return ApiResponseSever.ok(null);
    }
}
