package com.project.base_v1.service;

import com.project.base_v1.dto.request.assignment.CreateAssignmentRequest;
import com.project.base_v1.dto.request.registration.CreateRegistrationRequest;
import com.project.base_v1.dto.request.registration.ReviewRegistrationRequest;
import com.project.base_v1.entity.Registration;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.*;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.*;
import com.project.base_v1.service.impl.RegistrationServiceImpl;
import com.project.base_v1.service.impl.RoomAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationAssignmentServiceTest {
    @Mock RegistrationRepository registrationRepository;
    @Mock RoomAssignmentRepository assignmentRepository;
    @Mock BedRepository bedRepository;
    @Mock RoomRepository roomRepository;
    @Mock ContractRepository contractRepository;
    @Mock UserRepository userRepository;
    @Mock UserProfileRepository profileRepository;
    @Mock AuditLogService auditLogService;
    @Mock SimpMessagingTemplate messagingTemplate;

    User student;
    User staff;

    @BeforeEach
    void setUp() {
        student = User.builder().id(UUID.randomUUID()).username("student").email("student@edu.vn")
                .role(UserRole.USER).status(AccountStatus.ACTIVE).build();
        staff = User.builder().id(UUID.randomUUID()).username("staff").email("staff@edu.vn")
                .role(UserRole.STAFF).status(AccountStatus.ACTIVE).build();
    }

    @Test
    void createRegistration_rejectsSecondPendingRegistration() {
        authenticate("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(student));
        when(registrationRepository.existsByUserIdAndStatus(student.getId(), RegistrationStatus.PENDING)).thenReturn(true);
        RegistrationServiceImpl service = registrationService();

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createMyRegistration(
                new CreateRegistrationRequest(RoomType.STANDARD_8, GenderType.MALE, LocalDate.now(), null, null)));

        assertEquals(ErrorCode.ACTIVE_REGISTRATION_EXISTS, exception.getErrorCode());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void rejectRegistration_requiresReason() {
        authenticate("staff");
        Registration registration = Registration.builder().id(UUID.randomUUID()).user(student)
                .requestedRoomType(RoomType.STANDARD_8).requestedGenderType(GenderType.MALE)
                .preferredStartDate(LocalDate.now()).status(RegistrationStatus.PENDING).build();
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(staff));
        when(registrationRepository.findByIdForUpdate(registration.getId())).thenReturn(Optional.of(registration));

        BusinessException exception = assertThrows(BusinessException.class, () -> registrationService().review(
                registration.getId(), new ReviewRegistrationRequest(RegistrationStatus.REJECTED, " ")));

        assertEquals(ErrorCode.REJECTION_REASON_REQUIRED, exception.getErrorCode());
    }

    @Test
    void cancelRegistration_enforcesOwnership() {
        authenticate("student");
        User other = User.builder().id(UUID.randomUUID()).username("other").email("other@edu.vn")
                .role(UserRole.USER).status(AccountStatus.ACTIVE).build();
        Registration registration = Registration.builder().id(UUID.randomUUID()).user(other)
                .requestedRoomType(RoomType.STANDARD_8).requestedGenderType(GenderType.MALE)
                .preferredStartDate(LocalDate.now()).status(RegistrationStatus.PENDING).build();
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(student));
        when(registrationRepository.findByIdForUpdate(registration.getId())).thenReturn(Optional.of(registration));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> registrationService().cancelMyRegistration(registration.getId()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void assignment_rejectsUserWithActiveAssignment_beforeLockingBed() {
        authenticate("staff");
        Registration registration = Registration.builder().id(UUID.randomUUID()).user(student)
                .requestedRoomType(RoomType.STANDARD_8).requestedGenderType(GenderType.MALE)
                .preferredStartDate(LocalDate.now()).status(RegistrationStatus.APPROVED).build();
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(staff));
        when(registrationRepository.findByIdForUpdate(registration.getId())).thenReturn(Optional.of(registration));
        when(assignmentRepository.existsByUserIdAndStatus(student.getId(), AssignmentStatus.ACTIVE)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> assignmentService().assign(
                new CreateAssignmentRequest(registration.getId(), UUID.randomUUID(), LocalDate.now(), null)));

        assertEquals(ErrorCode.ACTIVE_ASSIGNMENT_EXISTS, exception.getErrorCode());
        verify(bedRepository, never()).findByIdForUpdate(any());
    }

    private RegistrationServiceImpl registrationService() {
        return new RegistrationServiceImpl(registrationRepository, assignmentRepository, bedRepository, userRepository,
                profileRepository, auditLogService, messagingTemplate);
    }

    private RoomAssignmentServiceImpl assignmentService() {
        return new RoomAssignmentServiceImpl(registrationRepository, assignmentRepository, bedRepository, roomRepository,
                contractRepository, userRepository, profileRepository, auditLogService, messagingTemplate);
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, null));
    }
}
