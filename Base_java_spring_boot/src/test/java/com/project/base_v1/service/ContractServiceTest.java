package com.project.base_v1.service;

import com.project.base_v1.dto.request.contract.CreateContractRequest;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.*;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.*;
import com.project.base_v1.service.impl.ContractServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {
    @Mock ContractRepository contractRepository;
    @Mock RoomAssignmentRepository assignmentRepository;
    @Mock BedRepository bedRepository;
    @Mock RoomRepository roomRepository;
    @Mock UserRepository userRepository;
    @Mock UserProfileRepository profileRepository;
    @Mock AuditLogService auditLogService;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("staff", null));
    }

    @Test
    void create_rejectsInvalidDateRange() {
        ContractServiceImpl service = service();
        LocalDate date = LocalDate.now();
        UUID assignmentId = UUID.randomUUID();
        when(assignmentRepository.findDetailedById(assignmentId))
                .thenReturn(Optional.of(RoomAssignment.builder().id(assignmentId).status(AssignmentStatus.ACTIVE).build()));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(
                new CreateContractRequest(assignmentId, date, date, BigDecimal.ZERO)));

        assertEquals(ErrorCode.INVALID_CONTRACT_DATES, exception.getErrorCode());
    }

    @Test
    void activate_rejectsSecondActiveContractForUser() {
        User student = User.builder().id(UUID.randomUUID()).username("student").email("student@edu.vn")
                .role(UserRole.USER).status(AccountStatus.ACTIVE).build();
        RoomAssignment assignment = RoomAssignment.builder().id(UUID.randomUUID()).user(student)
                .status(AssignmentStatus.ACTIVE).build();
        Contract contract = Contract.builder().id(UUID.randomUUID()).user(student).assignment(assignment)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusMonths(6))
                .rentalPrice(BigDecimal.TEN).deposit(BigDecimal.ZERO).status(ContractStatus.DRAFT).build();
        when(contractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));
        when(contractRepository.existsByUserIdAndStatus(student.getId(), ContractStatus.ACTIVE)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service().activate(contract.getId()));

        assertEquals(ErrorCode.ACTIVE_CONTRACT_EXISTS, exception.getErrorCode());
        verify(contractRepository, never()).save(any());
    }

    @Test
    void create_rejectsContractWhenAssignmentIsNotActive() {
        RoomAssignment assignment = RoomAssignment.builder().id(UUID.randomUUID()).status(AssignmentStatus.ENDED).build();
        when(assignmentRepository.findDetailedById(assignment.getId())).thenReturn(Optional.of(assignment));

        BusinessException exception = assertThrows(BusinessException.class, () -> service().create(
                new CreateContractRequest(assignment.getId(), LocalDate.now(), LocalDate.now().plusMonths(1), BigDecimal.ZERO)));

        assertEquals(ErrorCode.INVALID_CONTRACT_TRANSITION, exception.getErrorCode());
    }

    private ContractServiceImpl service() {
        return new ContractServiceImpl(contractRepository, assignmentRepository, bedRepository, roomRepository,
                userRepository, profileRepository, auditLogService);
    }
}
