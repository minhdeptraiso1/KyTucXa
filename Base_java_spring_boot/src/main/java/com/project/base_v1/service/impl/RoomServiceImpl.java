package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.facility.CreateRoomRequest;
import com.project.base_v1.dto.request.facility.UpdateRoomRequest;
import com.project.base_v1.dto.response.facility.PublicRoomSummaryResponse;
import com.project.base_v1.dto.response.facility.RoomResponse;
import com.project.base_v1.entity.Bed;
import com.project.base_v1.entity.Building;
import com.project.base_v1.entity.Floor;
import com.project.base_v1.entity.PricePolicy;
import com.project.base_v1.entity.Room;
import com.project.base_v1.enums.BedStatus;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.BedRepository;
import com.project.base_v1.repository.FloorRepository;
import com.project.base_v1.repository.PricePolicyRepository;
import com.project.base_v1.repository.RoomRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.RoomService;
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
public class RoomServiceImpl implements RoomService {

    RoomRepository roomRepository;
    FloorRepository floorRepository;
    BedRepository bedRepository;
    PricePolicyRepository pricePolicyRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        Floor floor = floorRepository.findById(request.floorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOOR_NOT_FOUND));

        Building building = floor.getBuilding();

        // Kiểm tra tính phù hợp giới tính giữa Room và Building
        if (building.getGenderType() != GenderType.MIXED && building.getGenderType() != request.genderType()) {
            throw new BusinessException(ErrorCode.ROOM_GENDER_MISMATCH);
        }

        if (roomRepository.existsByFloorIdAndRoomNumberIgnoreCase(floor.getId(), request.roomNumber())) {
            throw new BusinessException(ErrorCode.ROOM_NUMBER_ALREADY_EXISTS);
        }

        PricePolicy pricePolicy = null;
        if (request.pricePolicyId() != null) {
            pricePolicy = pricePolicyRepository.findById(request.pricePolicyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRICE_POLICY_NOT_FOUND));
        }

        Room room = Room.builder()
                .id(UUID.randomUUID())
                .floor(floor)
                .building(building)
                .roomNumber(request.roomNumber().trim())
                .roomType(request.roomType())
                .capacity(request.capacity())
                .genderType(request.genderType())
                .status(RoomStatus.AVAILABLE)
                .currentOccupancy(0)
                .areaSqm(request.areaSqm())
                .pricePolicy(pricePolicy)
                .imageUrl(blankToNull(request.imageUrl()))
                .description(request.description())
                .notes(request.notes())
                .build();

        Room saved = roomRepository.save(room);

        // Tự động tạo giường nếu có yêu cầu
        if (Boolean.TRUE.equals(request.autoGenerateBeds())) {
            List<Bed> beds = new ArrayList<>();
            for (int i = 1; i <= request.capacity(); i++) {
                String bedNum = String.format("%02d", i);
                beds.add(Bed.builder()
                        .id(UUID.randomUUID())
                        .room(saved)
                        .bedNumber(bedNum)
                        .status(BedStatus.AVAILABLE)
                        .build());
            }
            bedRepository.saveAll(beds);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(UUID id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getRoomNumber().equalsIgnoreCase(request.roomNumber()) &&
                roomRepository.existsByFloorIdAndRoomNumberIgnoreCaseAndIdNot(room.getFloor().getId(), request.roomNumber(), id)) {
            throw new BusinessException(ErrorCode.ROOM_NUMBER_ALREADY_EXISTS);
        }

        if (request.capacity() < room.getCurrentOccupancy()) {
            throw new BusinessException(ErrorCode.ROOM_OCCUPIED_CANNOT_CHANGE_CAPACITY);
        }

        Building building = room.getBuilding();
        if (building.getGenderType() != GenderType.MIXED && building.getGenderType() != request.genderType()) {
            throw new BusinessException(ErrorCode.ROOM_GENDER_MISMATCH);
        }

        PricePolicy pricePolicy = null;
        if (request.pricePolicyId() != null) {
            pricePolicy = pricePolicyRepository.findById(request.pricePolicyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRICE_POLICY_NOT_FOUND));
        }

        room.setRoomNumber(request.roomNumber().trim());
        room.setRoomType(request.roomType());
        room.setCapacity(request.capacity());
        room.setGenderType(request.genderType());
        room.setStatus(request.status());
        room.setAreaSqm(request.areaSqm());
        room.setPricePolicy(pricePolicy);
        room.setImageUrl(blankToNull(request.imageUrl()));
        room.setDescription(request.description());
        room.setNotes(request.notes());

        room.recalculateStatus();

        Room updated = roomRepository.save(room);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> getRooms(UUID buildingId, UUID floorId, RoomStatus status, RoomType roomType, GenderType genderType, String search, Pageable pageable) {
        Specification<Room> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (buildingId != null) {
                predicates.add(cb.equal(root.get("building").get("id"), buildingId));
            }

            if (floorId != null) {
                predicates.add(cb.equal(root.get("floor").get("id"), floorId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (roomType != null) {
                predicates.add(cb.equal(root.get("roomType"), roomType));
            }

            if (genderType != null) {
                predicates.add(cb.equal(root.get("genderType"), genderType));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("roomNumber")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(numMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return roomRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByFloor(UUID floorId) {
        return roomRepository.findByFloorId(floorId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicRoomSummaryResponse> getPublicAvailableRooms(RoomType roomType, GenderType genderType) {
        Specification<Room> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), RoomStatus.AVAILABLE));
            predicates.add(cb.lessThan(root.get("currentOccupancy"), root.get("capacity")));

            if (roomType != null) {
                predicates.add(cb.equal(root.get("roomType"), roomType));
            }

            if (genderType != null) {
                predicates.add(cb.equal(root.get("genderType"), genderType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return roomRepository.findAll(spec).stream()
                .map(r -> PublicRoomSummaryResponse.builder()
                        .id(r.getId())
                        .buildingCode(r.getBuilding().getCode())
                        .buildingName(r.getBuilding().getName())
                        .floorNumber(r.getFloor().getFloorNumber())
                        .roomNumber(r.getRoomNumber())
                        .roomType(r.getRoomType())
                        .capacity(r.getCapacity())
                        .availableBeds(r.getAvailableBeds())
                        .genderType(r.getGenderType())
                        .status(r.getStatus())
                        .areaSqm(r.getAreaSqm())
                        .pricePerMonth(r.getPricePolicy() != null ? r.getPricePolicy().getPricePerMonth() : null)
                        .imageUrl(r.getImageUrl())
                        .description(r.getDescription())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void deleteRoom(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        long bedCount = bedRepository.countByRoomId(id);
        if (bedCount > 0) {
            throw new BusinessException(ErrorCode.ROOM_HAS_BEDS);
        }

        room.setDeletedAt(Instant.now());
        room.setDeletedBy(CurrentUser.username());
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void updateRoomStatus(UUID id, RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        room.setStatus(status);
        roomRepository.save(room);
    }

    private RoomResponse mapToResponse(Room r) {
        long bedCount = bedRepository.countByRoomId(r.getId());

        return RoomResponse.builder()
                .id(r.getId())
                .floorId(r.getFloor().getId())
                .floorNumber(r.getFloor().getFloorNumber())
                .floorName(r.getFloor().getName())
                .buildingId(r.getBuilding().getId())
                .buildingCode(r.getBuilding().getCode())
                .buildingName(r.getBuilding().getName())
                .roomNumber(r.getRoomNumber())
                .roomType(r.getRoomType())
                .capacity(r.getCapacity())
                .currentOccupancy(r.getCurrentOccupancy())
                .availableBeds(r.getAvailableBeds())
                .genderType(r.getGenderType())
                .status(r.getStatus())
                .areaSqm(r.getAreaSqm())
                .pricePolicyId(r.getPricePolicy() != null ? r.getPricePolicy().getId() : null)
                .pricePolicyName(r.getPricePolicy() != null ? r.getPricePolicy().getName() : null)
                .pricePerMonth(r.getPricePolicy() != null ? r.getPricePolicy().getPricePerMonth() : null)
                .imageUrl(r.getImageUrl())
                .description(r.getDescription())
                .notes(r.getNotes())
                .bedCount(bedCount)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
