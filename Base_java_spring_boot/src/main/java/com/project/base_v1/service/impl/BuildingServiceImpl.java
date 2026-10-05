package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.facility.CreateBuildingRequest;
import com.project.base_v1.dto.request.facility.UpdateBuildingRequest;
import com.project.base_v1.dto.response.facility.BuildingResponse;
import com.project.base_v1.entity.Building;
import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.BedRepository;
import com.project.base_v1.repository.BuildingRepository;
import com.project.base_v1.repository.FloorRepository;
import com.project.base_v1.repository.RoomRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.BuildingService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuildingServiceImpl implements BuildingService {

    BuildingRepository buildingRepository;
    FloorRepository floorRepository;
    RoomRepository roomRepository;
    BedRepository bedRepository;

    @Override
    @Transactional
    public BuildingResponse createBuilding(CreateBuildingRequest request) {
        if (buildingRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException(ErrorCode.BUILDING_CODE_ALREADY_EXISTS);
        }

        Building building = Building.builder()
                .id(UUID.randomUUID())
                .code(request.code().trim().toUpperCase())
                .name(request.name().trim())
                .address(request.address())
                .genderType(request.genderType())
                .totalFloors(request.totalFloors())
                .status(request.status() != null ? request.status() : FacilityStatus.ACTIVE)
                .description(request.description())
                .build();

        Building saved = buildingRepository.save(building);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BuildingResponse updateBuilding(UUID id, UpdateBuildingRequest request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUILDING_NOT_FOUND));

        building.setName(request.name().trim());
        building.setAddress(request.address());
        building.setGenderType(request.genderType());
        building.setTotalFloors(request.totalFloors());
        building.setStatus(request.status());
        building.setDescription(request.description());

        Building updated = buildingRepository.save(building);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingResponse getBuildingById(UUID id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUILDING_NOT_FOUND));
        return mapToResponse(building);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingResponse> getBuildings(String search, FacilityStatus status, GenderType genderType, Pageable pageable) {
        Specification<Building> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("code")), pattern);
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                predicates.add(cb.or(codeMatch, nameMatch));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (genderType != null) {
                predicates.add(cb.equal(root.get("genderType"), genderType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return buildingRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingResponse> getAllActiveBuildings() {
        return buildingRepository.findByStatus(FacilityStatus.ACTIVE).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteBuilding(UUID id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUILDING_NOT_FOUND));

        long floorCount = floorRepository.countByBuildingId(id);
        if (floorCount > 0) {
            throw new BusinessException(ErrorCode.BUILDING_HAS_FLOORS);
        }

        building.setDeletedAt(Instant.now());
        building.setDeletedBy(CurrentUser.username());
        buildingRepository.save(building);
    }

    private BuildingResponse mapToResponse(Building b) {
        long floorCount = floorRepository.countByBuildingId(b.getId());
        long roomCount = roomRepository.countByBuildingId(b.getId());
        long availableBeds = bedRepository.countAvailableBedsByBuilding(b.getId());

        return BuildingResponse.builder()
                .id(b.getId())
                .code(b.getCode())
                .name(b.getName())
                .address(b.getAddress())
                .genderType(b.getGenderType())
                .totalFloors(b.getTotalFloors())
                .status(b.getStatus())
                .description(b.getDescription())
                .floorCount(floorCount)
                .roomCount(roomCount)
                .totalBeds(0) // computed dynamically when needed
                .availableBeds(availableBeds)
                .occupiedBeds(0)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
