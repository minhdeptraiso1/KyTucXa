package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.contract.*;
import com.project.base_v1.dto.response.contract.ContractResponse;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.*;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.*;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.ContractService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ContractResponse create(CreateContractRequest request) {
        RoomAssignment assignment = assignmentRepository.findDetailedById(request.assignmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));
        validateDates(request.startDate(), request.endDate());
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_TRANSITION);
        }
        if (contractRepository.findByAssignmentId(assignment.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.CONTRACT_FOR_ASSIGNMENT_EXISTS);
        }
        if (request.startDate().isBefore(assignment.getStartDate())
                || (assignment.getEndDate() != null && request.endDate().isAfter(assignment.getEndDate()))) {
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_DATES);
        }
        PricePolicy policy = assignment.getBed().getRoom().getPricePolicy();
        if (policy == null) throw new BusinessException(ErrorCode.PRICE_POLICY_NOT_FOUND);

        Contract contract = Contract.builder()
                .id(UUID.randomUUID()).contractCode(nextCode()).user(assignment.getUser()).assignment(assignment)
                .startDate(request.startDate()).endDate(request.endDate()).rentalPrice(policy.getPricePerMonth())
                .deposit(request.deposit() == null ? BigDecimal.ZERO : request.deposit())
                .status(ContractStatus.DRAFT).build();
        Contract saved = contractRepository.save(contract);
        auditLogService.log(currentUser().getId(), AuditAction.CREATE_CONTRACT.name());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse activate(UUID id) {
        Contract contract = get(id);
        if (contract.getStatus() != ContractStatus.DRAFT
                || contract.getAssignment().getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_TRANSITION);
        }
        if (contractRepository.existsByUserIdAndStatus(contract.getUser().getId(), ContractStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACTIVE_CONTRACT_EXISTS);
        }
        contract.setStatus(ContractStatus.ACTIVE);
        Contract saved = contractRepository.save(contract);
        auditLogService.log(currentUser().getId(), AuditAction.ACTIVATE_CONTRACT.name());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse renew(UUID id, RenewContractRequest request) {
        Contract contract = get(id);
        if (contract.getStatus() != ContractStatus.ACTIVE || !request.newEndDate().isAfter(contract.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_TRANSITION);
        }
        if (contract.getAssignment().getEndDate() != null
                && request.newEndDate().isAfter(contract.getAssignment().getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_DATES);
        }
        contract.setEndDate(request.newEndDate());
        Contract saved = contractRepository.save(contract);
        auditLogService.log(currentUser().getId(), AuditAction.RENEW_CONTRACT.name());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponse terminate(UUID id, TerminateContractRequest request) {
        Contract contract = get(id);
        if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_TRANSITION);
        }
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setTerminatedAt(Instant.now());
        contract.setTerminatedBy(currentUser());
        contract.setTerminationReason(request.reason().trim());
        endAssignment(contract.getAssignment(), LocalDate.now());
        Contract saved = contractRepository.save(contract);
        auditLogService.log(contract.getTerminatedBy().getId(), AuditAction.TERMINATE_CONTRACT.name());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getMyContracts() {
        return contractRepository.findByUserIdOrderByCreatedAtDesc(currentUser().getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractResponse> getAll(ContractStatus status, Pageable pageable) {
        Specification<Contract> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return contractRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void expireContracts() {
        contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, LocalDate.now()).forEach(contract -> {
            contract.setStatus(ContractStatus.EXPIRED);
            endAssignment(contract.getAssignment(), contract.getEndDate());
            contractRepository.save(contract);
            auditLogService.log(contract.getUser().getId(), AuditAction.EXPIRE_CONTRACT.name());
        });
    }

    private void endAssignment(RoomAssignment assignment, LocalDate endDate) {
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) return;
        assignment.setStatus(AssignmentStatus.ENDED);
        assignment.setEndDate(endDate);
        Bed bed = assignment.getBed();
        if (bed.getStatus() == BedStatus.OCCUPIED) bed.setStatus(BedStatus.AVAILABLE);
        bedRepository.save(bed);
        Room room = bed.getRoom();
        room.setCurrentOccupancy(Math.max(0, room.getCurrentOccupancy() - 1));
        room.recalculateStatus();
        roomRepository.save(room);
        assignmentRepository.save(assignment);
    }

    private Contract get(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    private User currentUser() {
        return userRepository.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (!end.isAfter(start)) throw new BusinessException(ErrorCode.INVALID_CONTRACT_DATES);
    }

    private String nextCode() {
        String code;
        do {
            code = "KTX-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (contractRepository.existsByContractCode(code));
        return code;
    }

    private ContractResponse toResponse(Contract contract) {
        UserProfile profile = profileRepository.findById(contract.getUser().getId()).orElse(null);
        Bed bed = contract.getAssignment().getBed();
        Room room = bed.getRoom();
        return new ContractResponse(contract.getId(), contract.getContractCode(), contract.getUser().getId(),
                profile == null ? null : profile.getStudentCode(),
                profile == null ? contract.getUser().getUsername() : profile.getFullName(),
                contract.getAssignment().getId(), room.getRoomNumber(), bed.getBedNumber(), room.getBuilding().getCode(),
                contract.getStartDate(), contract.getEndDate(), contract.getRentalPrice(), contract.getDeposit(),
                contract.getStatus(), contract.getTerminatedAt(), contract.getTerminationReason(), contract.getCreatedAt());
    }
}
