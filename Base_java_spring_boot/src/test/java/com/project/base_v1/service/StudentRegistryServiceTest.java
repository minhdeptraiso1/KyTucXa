package com.project.base_v1.service;

import com.project.base_v1.dto.request.student.UpdateStudentStatusRequest;
import com.project.base_v1.dto.response.student.StudentImportPreviewResponse;
import com.project.base_v1.dto.response.student.StudentImportResultResponse;
import com.project.base_v1.dto.response.student.StudentRegistryResponse;
import com.project.base_v1.entity.StudentRegistry;
import com.project.base_v1.enums.StudentRegistryStatus;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.StudentRegistryRepository;
import com.project.base_v1.repository.UserProfileRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.service.impl.StudentRegistryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRegistryServiceTest {

    @Mock
    private StudentRegistryRepository studentRegistryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuditLogService auditLogService;

    private StudentRegistryServiceImpl studentRegistryService;

    @BeforeEach
    void setUp() {
        studentRegistryService = new StudentRegistryServiceImpl(
                studentRegistryRepository,
                userRepository,
                userProfileRepository,
                auditLogService
        );
    }

    @Test
    void validateForRegistration_success() {
        StudentRegistry student = StudentRegistry.builder()
                .id(UUID.randomUUID())
                .studentCode("SV001")
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@abc.edu.vn")
                .phone("0901234567")
                .className("CNTT01")
                .status(StudentRegistryStatus.ACTIVE)
                .build();

        when(studentRegistryRepository.findByStudentCodeIgnoreCase(anyString())).thenReturn(Optional.of(student));
        when(userRepository.existsByStudentRegistryId(student.getId())).thenReturn(false);
        when(userProfileRepository.existsByStudentCode(student.getStudentCode())).thenReturn(false);

        StudentRegistry result = studentRegistryService.validateForRegistration(
                "SV001", "nguyenvana@abc.edu.vn", "0901234567", "Nguyễn Văn A"
        );

        assertNotNull(result);
        assertEquals("SV001", result.getStudentCode());
    }

    @Test
    void validateForRegistration_studentNotFound_throwsException() {
        when(studentRegistryRepository.findByStudentCodeIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.validateForRegistration("UNKNOWN", "a@abc.edu.vn", "0901234567", "Name")
        );

        assertEquals(ErrorCode.REGISTRATION_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    void validateForRegistration_inactive_throwsException() {
        StudentRegistry student = StudentRegistry.builder()
                .id(UUID.randomUUID())
                .studentCode("SV001")
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@abc.edu.vn")
                .phone("0901234567")
                .className("CNTT01")
                .status(StudentRegistryStatus.INACTIVE)
                .build();

        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV001")).thenReturn(Optional.of(student));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.validateForRegistration("SV001", "nguyenvana@abc.edu.vn", "0901234567", "Nguyễn Văn A")
        );

        assertEquals(ErrorCode.STUDENT_INACTIVE, ex.getErrorCode());
    }

    @Test
    void validateForRegistration_wrongEmail_throwsException() {
        StudentRegistry student = StudentRegistry.builder()
                .id(UUID.randomUUID())
                .studentCode("SV001")
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@abc.edu.vn")
                .phone("0901234567")
                .className("CNTT01")
                .status(StudentRegistryStatus.ACTIVE)
                .build();

        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV001")).thenReturn(Optional.of(student));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.validateForRegistration("SV001", "other@gmail.com", "0901234567", "Nguyễn Văn A")
        );

        assertEquals(ErrorCode.STUDENT_INFO_MISMATCH, ex.getErrorCode());
    }

    @Test
    void validateForRegistration_wrongPhone_throwsException() {
        StudentRegistry student = StudentRegistry.builder()
                .id(UUID.randomUUID())
                .studentCode("SV001")
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@abc.edu.vn")
                .phone("0901234567")
                .className("CNTT01")
                .status(StudentRegistryStatus.ACTIVE)
                .build();

        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV001")).thenReturn(Optional.of(student));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.validateForRegistration("SV001", "nguyenvana@abc.edu.vn", "0988888888", "Nguyễn Văn A")
        );

        assertEquals(ErrorCode.STUDENT_INFO_MISMATCH, ex.getErrorCode());
    }

    @Test
    void validateForRegistration_wrongName_throwsException() {
        StudentRegistry student = StudentRegistry.builder()
                .id(UUID.randomUUID())
                .studentCode("SV001")
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@abc.edu.vn")
                .phone("0901234567")
                .className("CNTT01")
                .status(StudentRegistryStatus.ACTIVE)
                .build();

        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV001")).thenReturn(Optional.of(student));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.validateForRegistration("SV001", "nguyenvana@abc.edu.vn", "0901234567", "Trần Văn B")
        );

        assertEquals(ErrorCode.STUDENT_INFO_MISMATCH, ex.getErrorCode());
    }

    @Test
    void validateForRegistration_alreadyRegistered_throwsException() {
        StudentRegistry student = StudentRegistry.builder()
                .id(UUID.randomUUID())
                .studentCode("SV001")
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@abc.edu.vn")
                .phone("0901234567")
                .className("CNTT01")
                .status(StudentRegistryStatus.ACTIVE)
                .build();

        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV001")).thenReturn(Optional.of(student));
        when(userRepository.existsByStudentRegistryId(student.getId())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.validateForRegistration("SV001", "nguyenvana@abc.edu.vn", "0901234567", "Nguyễn Văn A")
        );

        assertEquals(ErrorCode.STUDENT_ALREADY_REGISTERED, ex.getErrorCode());
    }

    @Test
    void previewImport_validCsv_returnsCorrectStats() {
        String csv = "student_code,email,phone,full_name,class_name\n" +
                "SV001,nguyenvana@abc.edu.vn,0901234567,Nguyễn Văn A,CNTT01\n" +
                "SV002,tranthib@abc.edu.vn,0912345678,Trần Thị B,CNTT02\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        when(studentRegistryRepository.existsByStudentCodeIgnoreCase("SV001")).thenReturn(false);
        when(studentRegistryRepository.existsByStudentCodeIgnoreCase("SV002")).thenReturn(true);

        StudentImportPreviewResponse response = studentRegistryService.previewImport(file);

        assertEquals(2, response.totalRows());
        assertEquals(2, response.validRows());
        assertEquals(0, response.invalidRows());
        assertEquals(1, response.newCount());
        assertEquals(1, response.updateCount());
    }

    @Test
    void previewImport_missingHeader_throwsException() {
        String csv = "code,mail,phone,name\nSV001,a@abc.edu.vn,0901234567,Nguyen Van A\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                studentRegistryService.previewImport(file)
        );

        assertEquals(ErrorCode.INVALID_CSV_HEADER, ex.getErrorCode());
    }

    @Test
    void previewImport_duplicateInFile_detected() {
        String csv = "student_code,email,phone,full_name,class_name\n" +
                "SV001,nguyenvana@abc.edu.vn,0901234567,Nguyễn Văn A,CNTT01\n" +
                "SV001,duplicate@abc.edu.vn,0999999999,Nguyễn Văn A Duplicate,CNTT01\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        StudentImportPreviewResponse response = studentRegistryService.previewImport(file);

        assertEquals(2, response.totalRows());
        assertEquals(1, response.validRows());
        assertEquals(1, response.duplicateRows());
        assertEquals(1, response.invalidRows());
    }

    @Test
    void confirmImport_persistsData() {
        String csv = "student_code,email,phone,full_name,class_name\n" +
                "SV001,nguyenvana@abc.edu.vn,0901234567,Nguyễn Văn A,CNTT01\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV001")).thenReturn(Optional.empty());

        StudentImportResultResponse response = studentRegistryService.confirmImport(file);

        assertEquals(1, response.totalProcessed());
        assertEquals(1, response.insertedCount());
        assertEquals(0, response.updatedCount());
        verify(studentRegistryRepository, times(1)).save(any(StudentRegistry.class));
    }

    @Test
    void confirmImport_acceptsVietnameseHeadersAndInactiveStatus() {
        String csv = "Mã sinh viên (*),Họ và tên (*),Email sinh viên (*),Số điện thoại (*),Lớp / Khóa (*),Trạng thái\n" +
                "SV009,Nguyễn Văn Chín,sv009@abc.edu.vn,0900000009,CNTT09,Tạm ngưng\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "danh-sach.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );
        when(studentRegistryRepository.findByStudentCodeIgnoreCase("SV009")).thenReturn(Optional.empty());

        studentRegistryService.confirmImport(file);

        var captor = org.mockito.ArgumentCaptor.forClass(StudentRegistry.class);
        verify(studentRegistryRepository).save(captor.capture());
        assertEquals(StudentRegistryStatus.INACTIVE, captor.getValue().getStatus());
        assertEquals("0900000009", captor.getValue().getPhone());
    }
}
