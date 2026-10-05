package com.project.base_v1.service;

import com.project.base_v1.dto.request.student.UpdateStudentStatusRequest;
import com.project.base_v1.dto.response.student.StudentImportPreviewResponse;
import com.project.base_v1.dto.response.student.StudentImportResultResponse;
import com.project.base_v1.dto.response.student.StudentRegistryResponse;
import com.project.base_v1.entity.StudentRegistry;
import com.project.base_v1.enums.StudentRegistryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StudentRegistryService {

    StudentImportPreviewResponse previewImport(MultipartFile file);

    StudentImportResultResponse confirmImport(MultipartFile file);

    byte[] exportCsv();

    Page<StudentRegistryResponse> getStudents(String keyword, StudentRegistryStatus status, Boolean hasAccount, Pageable pageable);

    StudentRegistryResponse getStudentById(UUID id);

    StudentRegistryResponse updateStatus(UUID id, UpdateStudentStatusRequest request);

    StudentRegistry validateForRegistration(String studentCode, String email, String phone, String fullName);
}
