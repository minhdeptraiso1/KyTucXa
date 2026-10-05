package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.assignment.CreateAssignmentRequest;
import com.project.base_v1.dto.response.assignment.AssignmentResponse;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.*;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.*;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.RoomAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomAssignmentServiceImpl implements RoomAssignmentService {
    private final RegistrationRepository registrationRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AuditLogService auditLogService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public AssignmentResponse assign(CreateAssignmentRequest request) {
        User staff = currentUser();
        Registration registration = registrationRepository.findByIdForUpdate(request.registrationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND));
        if (registration.getStatus() != RegistrationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_TRANSITION);
        }
        if (assignmentRepository.existsByRegistrationId(registration.getId())) {
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_ASSIGNED);
        }
        if (assignmentRepository.existsByUserIdAndStatus(registration.getUser().getId(), AssignmentStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACTIVE_ASSIGNMENT_EXISTS);
        }
        validateDates(request.startDate(), request.endDate());
        Bed bed = bedRepository.findByIdForUpdate(request.bedId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        Room room = bed.getRoom();
        if (!bed.isUsable() || room.getStatus() != RoomStatus.AVAILABLE
                || room.getCurrentOccupancy() >= room.getCapacity()) {
            throw new BusinessException(ErrorCode.BED_NOT_AVAILABLE);
        }
        if (assignmentRepository.existsByBedIdAndStatus(bed.getId(), AssignmentStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACTIVE_ASSIGNMENT_EXISTS);
        }
        if (room.getRoomType() != registration.getRequestedRoomType()
                || (room.getGenderType() != registration.getRequestedGenderType()
                && room.getGenderType() != GenderType.MIXED)) {
            throw new BusinessException(ErrorCode.ROOM_GENDER_MISMATCH);
        }

        RoomAssignment assignment = RoomAssignment.builder()
                .id(UUID.randomUUID()).registration(registration).user(registration.getUser()).bed(bed)
                .startDate(request.startDate()).endDate(request.endDate()).status(AssignmentStatus.ACTIVE)
                .assignedBy(staff).build();
        RoomAssignment saved = assignmentRepository.save(assignment);
        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);
        room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        room.recalculateStatus();
        roomRepository.save(room);
        auditLogService.log(staff.getId(), AuditAction.CREATE_ASSIGNMENT.name());
        messagingTemplate.convertAndSend("/topic/users/" + registration.getUser().getId() + "/notifications",
                Map.of("type", "ROOM_ASSIGNED", "id", saved.getId()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AssignmentResponse end(UUID id) {
        RoomAssignment assignment = assignmentRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_TRANSITION);
        }
        if (contractRepository.existsByAssignmentIdAndStatus(id, ContractStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACTIVE_CONTRACT_BLOCKS_ASSIGNMENT_END);
        }
        assignment.setStatus(AssignmentStatus.ENDED);
        assignment.setEndDate(LocalDate.now());
        releaseBed(assignment);
        RoomAssignment saved = assignmentRepository.save(assignment);
        auditLogService.log(currentUser().getId(), AuditAction.END_ASSIGNMENT.name());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMyAssignments() {
        return assignmentRepository.findByUserIdOrderByCreatedAtDesc(currentUser().getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getAll(Pageable pageable) {
        return assignmentRepository.findAll(pageable).map(this::toResponse);
    }

    private void releaseBed(RoomAssignment assignment) {
        Bed bed = assignment.getBed();
        if (bed.getStatus() == BedStatus.OCCUPIED) bed.setStatus(BedStatus.AVAILABLE);
        bedRepository.save(bed);
        Room room = bed.getRoom();
        room.setCurrentOccupancy(Math.max(0, room.getCurrentOccupancy() - 1));
        room.recalculateStatus();
        roomRepository.save(room);
    }

    private User currentUser() {
        return userRepository.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (end != null && end.isBefore(start)) throw new BusinessException(ErrorCode.INVALID_ASSIGNMENT_DATES);
    }

    private AssignmentResponse toResponse(RoomAssignment assignment) {
        UserProfile profile = profileRepository.findById(assignment.getUser().getId()).orElse(null);
        Bed bed = assignment.getBed();
        Room room = bed.getRoom();
        return new AssignmentResponse(assignment.getId(), assignment.getRegistration().getId(),
                assignment.getUser().getId(), profile == null ? null : profile.getStudentCode(),
                profile == null ? assignment.getUser().getUsername() : profile.getFullName(),
                bed.getId(), bed.getBedNumber(), room.getRoomNumber(), room.getBuilding().getCode(),
                room.getFloor().getFloorNumber(), assignment.getStartDate(), assignment.getEndDate(),
                assignment.getStatus(), assignment.getAssignedBy().getId(), assignment.getCreatedAt());
    }
}
