package com.project.base_v1.service;

import com.project.base_v1.dto.request.facility.CreateRoomRequest;
import com.project.base_v1.dto.request.facility.UpdateRoomRequest;
import com.project.base_v1.dto.response.facility.PublicRoomSummaryResponse;
import com.project.base_v1.dto.response.facility.RoomResponse;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request);

    RoomResponse updateRoom(UUID id, UpdateRoomRequest request);

    RoomResponse getRoomById(UUID id);

    Page<RoomResponse> getRooms(UUID buildingId, UUID floorId, RoomStatus status, RoomType roomType, GenderType genderType, String search, Pageable pageable);

    List<RoomResponse> getRoomsByFloor(UUID floorId);

    List<PublicRoomSummaryResponse> getPublicAvailableRooms(RoomType roomType, GenderType genderType);

    void deleteRoom(UUID id);

    void updateRoomStatus(UUID id, RoomStatus status);
}
