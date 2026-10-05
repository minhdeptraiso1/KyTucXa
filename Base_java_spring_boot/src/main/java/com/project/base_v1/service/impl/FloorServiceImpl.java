package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.facility.CreateFloorRequest;
import com.project.base_v1.dto.request.facility.UpdateFloorRequest;
import com.project.base_v1.dto.response.facility.FloorResponse;
import com.project.base_v1.entity.Building;
import com.project.base_v1.entity.Floor;
import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.BuildingRepository;
import com.project.base_v1.repository.FloorRepository;
import com.project.base_v1.repository.RoomRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.FloorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FloorServiceImpl implements FloorService {

    FloorRepository floorRepository;
    BuildingRepository buildingRepository;
    RoomRepository roomRepository;

    @Override
    @Transactional
    public FloorResponse createFloor(UUID buildingId, CreateFloorRequest request) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUILDING_NOT_FOUND));

        if (request.floorNumber() > building.getTotalFloors()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        if (floorRepository.existsByBuildingIdAndFloorNumber(buildingId, request.floorNumber())) {
            throw new BusinessException(ErrorCode.FLOOR_NUMBER_ALREADY_EXISTS);
        }

        Floor floor = Floor.builder()
                .id(UUID.randomUUID())
                .building(building)
                .floorNumber(request.floorNumber())
                .name(request.name() != null ? request.name().trim() : "Tầng " + request.floorNumber())
                .status(request.status() != null ? request.status() : FacilityStatus.ACTIVE)
                .build();

        Floor saved = floorRepository.save(floor);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public FloorResponse updateFloor(UUID id, UpdateFloorRequest request) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOOR_NOT_FOUND));

        if (request.name() != null) {
            floor.setName(request.name().trim());
        }
        floor.setStatus(request.status());

        Floor updated = floorRepository.save(floor);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FloorResponse getFloorById(UUID id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOOR_NOT_FOUND));
        return mapToResponse(floor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FloorResponse> getFloorsByBuilding(UUID buildingId) {
        if (!buildingRepository.existsById(buildingId)) {
            throw new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
        }

        return floorRepository.findByBuildingIdOrderByFloorNumber(buildingId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFloor(UUID id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOOR_NOT_FOUND));

        long roomCount = roomRepository.countByFloorId(id);
        if (roomCount > 0) {
            throw new BusinessException(ErrorCode.FLOOR_HAS_ROOMS);
        }

        floor.setDeletedAt(Instant.now());
        floor.setDeletedBy(CurrentUser.username());
        floorRepository.save(floor);
    }

    private FloorResponse mapToResponse(Floor f) {
        long roomCount = roomRepository.countByFloorId(f.getId());

        return FloorResponse.builder()
                .id(f.getId())
                .buildingId(f.getBuilding().getId())
                .buildingCode(f.getBuilding().getCode())
                .buildingName(f.getBuilding().getName())
                .floorNumber(f.getFloorNumber())
                .name(f.getName())
                .status(f.getStatus())
                .roomCount(roomCount)
                .totalBeds(0)
                .availableBeds(0)
                .occupiedBeds(0)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
