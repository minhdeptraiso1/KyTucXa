package com.project.base_v1.controller;

import com.project.base_v1.dto.request.student.UpdateStudentStatusRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.student.StudentImportPreviewResponse;
import com.project.base_v1.dto.response.student.StudentImportResultResponse;
import com.project.base_v1.dto.response.student.StudentRegistryResponse;
import com.project.base_v1.enums.StudentRegistryStatus;
import com.project.base_v1.service.StudentRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(
        name = "Student Registry",
        description = "APIs quản lý danh sách sinh viên trường & import CSV phục vụ đăng ký KTX"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/student-registry")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StudentRegistryController {

    StudentRegistryService studentRegistryService;

    @Operation(
            summary = "Xem trước kết quả import CSV danh sách sinh viên",
            description = "Tải lên file CSV danh sách sinh viên, hệ thống sẽ kiểm tra cấu trúc header, validate từng dòng và phát hiện trùng lặp trước khi lưu vào DB."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kiểm tra và trả về kết quả preview thành công"),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc thiếu cột bắt buộc"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseSever<StudentImportPreviewResponse> previewImport(
            @Parameter(description = "File CSV danh sách sinh viên", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponseSever.ok(studentRegistryService.previewImport(file));
    }

    @Operation(
            summary = "Xác nhận import CSV danh sách sinh viên vào PostgreSQL",
            description = "Xác nhận lưu danh sách sinh viên từ file CSV vào bảng student_registry trong PostgreSQL. Thêm mới nếu chưa có, cập nhật nếu đã tồn tại."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import sinh viên thành công"),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc dữ liệu lỗi"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseSever<StudentImportResultResponse> confirmImport(
            @Parameter(description = "File CSV danh sách sinh viên", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponseSever.ok(studentRegistryService.confirmImport(file));
    }

    @Operation(
            summary = "Xuất danh sách sinh viên ra file CSV",
            description = "Xuất toàn bộ danh sách sinh viên trong Student Registry ra định dạng file CSV hỗ trợ tiếng Việt có dấu."
    )
    @GetMapping(value = "/export", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csvData = studentRegistryService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_registry.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @Operation(
            summary = "Danh sách sinh viên trong Student Registry",
            description = "Lấy danh sách sinh viên có phân trang, tìm kiếm theo từ khóa (Mã SV, Họ tên, Email, SĐT, Lớp) và lọc theo trạng thái."
    )
    @GetMapping
    public ApiResponseSever<Page<StudentRegistryResponse>> getStudents(
            @Parameter(description = "Từ khóa tìm kiếm (Mã SV, Họ tên, Email, SĐT, Lớp)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)")
            @RequestParam(required = false) StudentRegistryStatus status,
            @Parameter(description = "Lọc theo trạng thái đã có tài khoản hay chưa")
            @RequestParam(required = false) Boolean hasAccount,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(studentRegistryService.getStudents(keyword, status, hasAccount, pageable));
    }

    @Operation(summary = "Xem chi tiết một sinh viên theo ID")
    @GetMapping("/{id}")
    public ApiResponseSever<StudentRegistryResponse> getStudentById(
            @PathVariable UUID id
    ) {
        return ApiResponseSever.ok(studentRegistryService.getStudentById(id));
    }

    @Operation(summary = "Cập nhật trạng thái sinh viên (Kích hoạt / Tạm dừng)")
    @PatchMapping("/{id}/status")
    public ApiResponseSever<StudentRegistryResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentStatusRequest request
    ) {
        return ApiResponseSever.ok(studentRegistryService.updateStatus(id, request));
    }
}
