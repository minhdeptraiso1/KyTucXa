package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.registration.CreateRegistrationRequest;
import com.project.base_v1.dto.request.registration.ReviewRegistrationRequest;
import com.project.base_v1.dto.response.registration.RegistrationResponse;
import com.project.base_v1.entity.Registration;
import com.project.base_v1.entity.User;
import com.project.base_v1.entity.UserProfile;
import com.project.base_v1.enums.AssignmentStatus;
import com.project.base_v1.enums.AuditAction;
import com.project.base_v1.enums.RegistrationStatus;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.BedRepository;
import com.project.base_v1.repository.RegistrationRepository;
import com.project.base_v1.repository.RoomAssignmentRepository;
import com.project.base_v1.repository.UserProfileRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.RegistrationService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AuditLogService auditLogService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public RegistrationResponse createMyRegistration(CreateRegistrationRequest request) {
        User user = currentUser();
        validateDates(request.preferredStartDate(), request.preferredEndDate());
        if (registrationRepository.existsByUserIdAndStatus(user.getId(), RegistrationStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ACTIVE_REGISTRATION_EXISTS);
        }
        if (assignmentRepository.existsByUserIdAndStatus(user.getId(), AssignmentStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACTIVE_ASSIGNMENT_EXISTS);
        }
        boolean hasApprovedWaitingForAssignment = registrationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .anyMatch(item -> item.getStatus() == RegistrationStatus.APPROVED
                        && !assignmentRepository.existsByRegistrationId(item.getId()));
        if (hasApprovedWaitingForAssignment) {
            throw new BusinessException(ErrorCode.ACTIVE_REGISTRATION_EXISTS);
        }

        Registration registration = Registration.builder()
                .id(UUID.randomUUID())
                .user(user)
                .requestedRoomType(request.requestedRoomType())
                .requestedGenderType(request.requestedGenderType())
                .preferredStartDate(request.preferredStartDate())
                .preferredEndDate(request.preferredEndDate())
                .reason(blankToNull(request.reason()))
                .status(RegistrationStatus.PENDING)
                .build();
        Registration saved = registrationRepository.save(registration);
        auditLogService.log(user.getId(), AuditAction.CREATE_REGISTRATION.name());
        messagingTemplate.convertAndSend("/topic/registrations", Map.of("id", saved.getId(), "status", saved.getStatus()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations() {
        return registrationRepository.findByUserIdOrderByCreatedAtDesc(currentUser().getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public RegistrationResponse cancelMyRegistration(UUID id) {
        User user = currentUser();
        Registration registration = registrationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND));
        if (!registration.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_TRANSITION);
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        Registration saved = registrationRepository.save(registration);
        auditLogService.log(user.getId(), AuditAction.CANCEL_REGISTRATION.name());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponse> getQueue(RegistrationStatus status, String keyword, Pageable pageable) {
        Specification<Registration> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("user").get("email")), pattern),
                        cb.like(cb.lower(root.get("user").get("username")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return registrationRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public RegistrationResponse review(UUID id, ReviewRegistrationRequest request) {
        User reviewer = currentUser();
        Registration registration = registrationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND));
        if (registration.getStatus() != RegistrationStatus.PENDING
                || (request.decision() != RegistrationStatus.APPROVED
                && request.decision() != RegistrationStatus.REJECTED)) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_TRANSITION);
        }
        if (request.decision() == RegistrationStatus.APPROVED
                && bedRepository.findAvailableForRegistration(
                        registration.getRequestedRoomType(), registration.getRequestedGenderType()).isEmpty()) {
            throw new BusinessException(ErrorCode.NO_COMPATIBLE_BED);
        }
        if (request.decision() == RegistrationStatus.REJECTED
                && (request.rejectionReason() == null || request.rejectionReason().isBlank())) {
            throw new BusinessException(ErrorCode.REJECTION_REASON_REQUIRED);
        }

        registration.setStatus(request.decision());
        registration.setRejectionReason(request.decision() == RegistrationStatus.REJECTED
                ? request.rejectionReason().trim() : null);
        registration.setReviewedBy(reviewer);
        registration.setReviewedAt(Instant.now());
        Registration saved = registrationRepository.save(registration);
        auditLogService.log(reviewer.getId(), request.decision() == RegistrationStatus.APPROVED
                ? AuditAction.APPROVE_REGISTRATION.name() : AuditAction.REJECT_REGISTRATION.name());
        messagingTemplate.convertAndSend("/topic/users/" + registration.getUser().getId() + "/notifications",
                Map.of("type", "REGISTRATION_REVIEWED", "id", saved.getId(), "status", saved.getStatus()));
        return toResponse(saved);
    }

    private User currentUser() {
        return userRepository.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (end != null && !end.isAfter(start)) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_DATES);
        }
    }

    private RegistrationResponse toResponse(Registration registration) {
        UserProfile profile = profileRepository.findById(registration.getUser().getId()).orElse(null);
        return new RegistrationResponse(
                registration.getId(), registration.getUser().getId(),
                profile == null ? null : profile.getStudentCode(),
                profile == null ? registration.getUser().getUsername() : profile.getFullName(),
                registration.getUser().getEmail(), registration.getRequestedRoomType(),
                registration.getRequestedGenderType(), registration.getPreferredStartDate(),
                registration.getPreferredEndDate(), registration.getReason(), registration.getStatus(),
                registration.getRejectionReason(), registration.getReviewedBy() == null ? null : registration.getReviewedBy().getId(),
                registration.getReviewedAt(), registration.getCreatedAt());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
