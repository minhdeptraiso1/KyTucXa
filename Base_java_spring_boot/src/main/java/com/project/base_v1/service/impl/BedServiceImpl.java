package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.facility.BatchCreateBedsRequest;
import com.project.base_v1.dto.request.facility.CreateBedRequest;
import com.project.base_v1.dto.request.facility.UpdateBedRequest;
import com.project.base_v1.dto.response.facility.BedResponse;
import com.project.base_v1.entity.Bed;
import com.project.base_v1.entity.Room;
import com.project.base_v1.enums.BedStatus;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.BedRepository;
import com.project.base_v1.repository.RoomRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.BedService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BedServiceImpl implements BedService {

    BedRepository bedRepository;
    RoomRepository roomRepository;

    @Override
    @Transactional
    public BedResponse createBed(CreateBedRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (bedRepository.existsByRoomIdAndBedNumberIgnoreCase(room.getId(), request.bedNumber())) {
            throw new BusinessException(ErrorCode.BED_NUMBER_ALREADY_EXISTS);
        }

        long currentBedCount = bedRepository.countByRoomId(room.getId());
        if (currentBedCount >= room.getCapacity()) {
            throw new BusinessException(ErrorCode.ROOM_CAPACITY_EXCEEDED);
        }

        Bed bed = Bed.builder()
                .id(UUID.randomUUID())
                .room(room)
                .bedNumber(request.bedNumber().trim())
                .status(room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.INACTIVE
                        ? BedStatus.MAINTENANCE : BedStatus.AVAILABLE)
                .notes(request.notes())
                .build();

        Bed saved = bedRepository.save(bed);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public List<BedResponse> batchCreateBeds(BatchCreateBedsRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        long currentBedCount = bedRepository.countByRoomId(room.getId());
        if (currentBedCount + request.count() > room.getCapacity()) {
            throw new BusinessException(ErrorCode.ROOM_CAPACITY_EXCEEDED);
        }

        String prefix = request.prefix() != null ? request.prefix().trim() : "";
        List<Bed> beds = new ArrayList<>();
        for (int i = 1; i <= request.count(); i++) {
            String bedNum = prefix.isEmpty() ? String.format("%02d", (int) currentBedCount + i) : prefix + "-" + i;
            if (!bedRepository.existsByRoomIdAndBedNumberIgnoreCase(room.getId(), bedNum)) {
                beds.add(Bed.builder()
                        .id(UUID.randomUUID())
                        .room(room)
                        .bedNumber(bedNum)
                        .status(room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.INACTIVE
                                ? BedStatus.MAINTENANCE : BedStatus.AVAILABLE)
                        .build());
            }
        }

        List<Bed> saved = bedRepository.saveAll(beds);
        return saved.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public BedResponse updateBed(UUID id, UpdateBedRequest request) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));

        Room room = bed.getRoom();
        if (!bed.getBedNumber().equalsIgnoreCase(request.bedNumber()) &&
                bedRepository.existsByRoomIdAndBedNumberIgnoreCaseAndIdNot(room.getId(), request.bedNumber(), id)) {
            throw new BusinessException(ErrorCode.BED_NUMBER_ALREADY_EXISTS);
        }

        // BR-FAC-06 & BR-FAC-07: Không được active Bed khi Room không usable
        if (request.status() == BedStatus.AVAILABLE &&
                (room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.INACTIVE)) {
            throw new BusinessException(ErrorCode.BED_CANNOT_BE_ACTIVE_WHEN_ROOM_NOT_USABLE);
        }

        bed.setBedNumber(request.bedNumber().trim());
        bed.setStatus(request.status());
        bed.setNotes(request.notes());

        Bed updated = bedRepository.save(bed);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public BedResponse updateBedStatus(UUID id, BedStatus status) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));

        Room room = bed.getRoom();
        if (status == BedStatus.AVAILABLE &&
                (room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.INACTIVE)) {
            throw new BusinessException(ErrorCode.BED_CANNOT_BE_ACTIVE_WHEN_ROOM_NOT_USABLE);
        }

        if (bed.getStatus() == BedStatus.OCCUPIED && status != BedStatus.OCCUPIED) {
            // Giường đang có người ở cần lưu ý
        }

        bed.setStatus(status);
        Bed updated = bedRepository.save(bed);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BedResponse getBedById(UUID id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        return mapToResponse(bed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getBedsByRoom(UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        return bedRepository.findByRoomIdOrderByBedNumber(roomId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteBed(UUID id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new BusinessException(ErrorCode.BED_OCCUPIED);
        }

        bed.setDeletedAt(Instant.now());
        bed.setDeletedBy(CurrentUser.username());
        bedRepository.save(bed);
    }

    private BedResponse mapToResponse(Bed b) {
        Room r = b.getRoom();
        return BedResponse.builder()
                .id(b.getId())
                .roomId(r.getId())
                .roomNumber(r.getRoomNumber())
                .floorNumber(r.getFloor().getFloorNumber())
                .buildingCode(r.getBuilding().getCode())
                .buildingName(r.getBuilding().getName())
                .bedNumber(b.getBedNumber())
                .status(b.getStatus())
                .isUsable(b.isUsable())
                .notes(b.getNotes())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
