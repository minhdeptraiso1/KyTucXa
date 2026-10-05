package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.student.UpdateStudentStatusRequest;
import com.project.base_v1.dto.response.student.StudentImportPreviewResponse;
import com.project.base_v1.dto.response.student.StudentImportResultResponse;
import com.project.base_v1.dto.response.student.StudentRegistryResponse;
import com.project.base_v1.dto.response.student.StudentRegistryRowDto;
import com.project.base_v1.entity.StudentRegistry;
import com.project.base_v1.enums.AuditAction;
import com.project.base_v1.enums.StudentRegistryStatus;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.StudentRegistryRepository;
import com.project.base_v1.repository.UserProfileRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.StudentRegistryService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentRegistryServiceImpl implements StudentRegistryService {

    StudentRegistryRepository studentRegistryRepository;
    UserRepository userRepository;
    UserProfileRepository userProfileRepository;
    AuditLogService auditLogService;

    static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+() .-]{8,20}$");
    static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public StudentImportPreviewResponse previewImport(MultipartFile file) {
        validateFile(file);
        List<ParsedRow> parsedRows = parseCsv(file);

        int totalRows = parsedRows.size();
        int validRows = 0;
        int invalidRows = 0;
        int duplicateRows = 0;
        int newCount = 0;
        int updateCount = 0;

        List<String> errors = new ArrayList<>();
        List<StudentRegistryRowDto> previewRows = new ArrayList<>();

        for (ParsedRow row : parsedRows) {
            if (row.isDuplicate) {
                duplicateRows++;
                invalidRows++;
                errors.add("Dòng " + row.rowNumber + ": " + row.error);
            } else if (!row.isValid) {
                invalidRows++;
                errors.add("Dòng " + row.rowNumber + ": " + row.error);
            } else {
                validRows++;
                if (row.isExisting) {
                    updateCount++;
                } else {
                    newCount++;
                }
            }

            if (previewRows.size() < 50) {
                previewRows.add(StudentRegistryRowDto.builder()
                        .rowNumber(row.rowNumber)
                        .studentCode(row.studentCode)
                        .email(row.email)
                        .phone(row.phone)
                        .fullName(row.fullName)
                        .className(row.className)
                        .status(row.status)
                        .isValid(row.isValid && !row.isDuplicate)
                        .isDuplicate(row.isDuplicate)
                        .isExisting(row.isExisting)
                        .error(row.error)
                        .build());
            }
        }

        return StudentImportPreviewResponse.builder()
                .totalRows(totalRows)
                .validRows(validRows)
                .invalidRows(invalidRows)
                .duplicateRows(duplicateRows)
                .newCount(newCount)
                .updateCount(updateCount)
                .errors(errors)
                .previewRows(previewRows)
                .build();
    }

    @Override
    @Transactional
    public StudentImportResultResponse confirmImport(MultipartFile file) {
        validateFile(file);
        List<ParsedRow> parsedRows = parseCsv(file);

        int totalProcessed = 0;
        int insertedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        List<String> messages = new ArrayList<>();

        for (ParsedRow row : parsedRows) {
            totalProcessed++;
            if (!row.isValid || row.isDuplicate) {
                skippedCount++;
                continue;
            }

            Optional<StudentRegistry> existingOpt = studentRegistryRepository.findByStudentCodeIgnoreCase(row.studentCode);
            if (existingOpt.isPresent()) {
                StudentRegistry existing = existingOpt.get();
                existing.setFullName(row.fullName);
                existing.setEmail(row.email);
                existing.setPhone(row.phone);
                existing.setClassName(row.className);
                existing.setStatus(row.status);
                studentRegistryRepository.save(existing);
                updatedCount++;
            } else {
                StudentRegistry newStudent = StudentRegistry.builder()
                        .id(UUID.randomUUID())
                        .studentCode(row.studentCode)
                        .fullName(row.fullName)
                        .email(row.email)
                        .phone(row.phone)
                        .className(row.className)
                        .status(row.status)
                        .build();
                studentRegistryRepository.save(newStudent);
                insertedCount++;
            }
        }

        UUID actorId = getCurrentUserId();
        auditLogService.log(actorId, AuditAction.IMPORT_STUDENT_REGISTRY.name());

        messages.add("Đã nhập thành công " + insertedCount + " sinh viên mới, cập nhật " + updatedCount + " sinh viên.");
        if (skippedCount > 0) {
            messages.add("Bỏ qua " + skippedCount + " dòng không hợp lệ hoặc trùng lặp.");
        }

        return StudentImportResultResponse.builder()
                .totalProcessed(totalProcessed)
                .insertedCount(insertedCount)
                .updatedCount(updatedCount)
                .skippedCount(skippedCount)
                .messages(messages)
                .build();
    }

    @Override
    public byte[] exportCsv() {
        List<StudentRegistry> list = studentRegistryRepository.findAll();
        list.sort(Comparator.comparing(StudentRegistry::getStudentCode));

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM for Excel Vietnamese compatibility
        sb.append('\uFEFF');
        sb.append("student_code,email,phone,full_name,class_name,status\n");

        for (StudentRegistry s : list) {
            sb.append(escapeCsv(s.getStudentCode())).append(",")
              .append(escapeCsv(s.getEmail())).append(",")
              .append(escapeCsv(s.getPhone())).append(",")
              .append(escapeCsv(s.getFullName())).append(",")
              .append(escapeCsv(s.getClassName())).append(",")
              .append(escapeCsv(s.getStatus().name())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Page<StudentRegistryResponse> getStudents(String keyword, StudentRegistryStatus status, Boolean hasAccount, Pageable pageable) {
        Specification<StudentRegistry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("studentCode")), kw),
                        cb.like(cb.lower(root.get("fullName")), kw),
                        cb.like(cb.lower(root.get("email")), kw),
                        cb.like(cb.lower(root.get("phone")), kw),
                        cb.like(cb.lower(root.get("className")), kw)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return studentRegistryRepository.findAll(spec, pageable).map(s -> {
            boolean hasUser = userRepository.existsByStudentRegistryId(s.getId());
            return StudentRegistryResponse.builder()
                    .id(s.getId())
                    .studentCode(s.getStudentCode())
                    .fullName(s.getFullName())
                    .email(s.getEmail())
                    .phone(s.getPhone())
                    .className(s.getClassName())
                    .status(s.getStatus())
                    .hasRegisteredAccount(hasUser)
                    .createdAt(s.getCreatedAt())
                    .updatedAt(s.getUpdatedAt())
                    .build();
        });
    }

    @Override
    public StudentRegistryResponse getStudentById(UUID id) {
        StudentRegistry s = studentRegistryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        boolean hasUser = userRepository.existsByStudentRegistryId(s.getId());
        return StudentRegistryResponse.builder()
                .id(s.getId())
                .studentCode(s.getStudentCode())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .className(s.getClassName())
                .status(s.getStatus())
                .hasRegisteredAccount(hasUser)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public StudentRegistryResponse updateStatus(UUID id, UpdateStudentStatusRequest request) {
        StudentRegistry s = studentRegistryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        s.setStatus(request.status());
        studentRegistryRepository.save(s);

        UUID actorId = getCurrentUserId();
        AuditAction action = request.status() == StudentRegistryStatus.ACTIVE ?
                AuditAction.ACTIVATE_STUDENT : AuditAction.DEACTIVATE_STUDENT;
        auditLogService.log(actorId, action.name());

        boolean hasUser = userRepository.existsByStudentRegistryId(s.getId());
        return StudentRegistryResponse.builder()
                .id(s.getId())
                .studentCode(s.getStudentCode())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .className(s.getClassName())
                .status(s.getStatus())
                .hasRegisteredAccount(hasUser)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    @Override
    public StudentRegistry validateForRegistration(String studentCode, String email, String phone, String fullName) {
        if (studentCode == null || studentCode.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        // Bước 2: Tìm Student Registry
        StudentRegistry student = studentRegistryRepository.findByStudentCodeIgnoreCase(studentCode.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_ALLOWED));

        // Bước 3: Kiểm tra status
        if (student.getStatus() != StudentRegistryStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.STUDENT_INACTIVE);
        }

        // Bước 4: Đối chiếu thông tin
        // student_code
        if (!student.getStudentCode().trim().equalsIgnoreCase(studentCode.trim())) {
            throw new BusinessException(ErrorCode.STUDENT_INFO_MISMATCH);
        }

        // email
        if (email == null || !student.getEmail().trim().equalsIgnoreCase(email.trim())) {
            throw new BusinessException(ErrorCode.STUDENT_INFO_MISMATCH);
        }

        // phone (so sánh sau khi chuẩn hóa chỉ lấy chữ số)
        String normPhone1 = normalizePhone(phone);
        String normPhone2 = normalizePhone(student.getPhone());
        if (normPhone1.isEmpty() || !normPhone1.equals(normPhone2)) {
            throw new BusinessException(ErrorCode.STUDENT_INFO_MISMATCH);
        }

        // full_name (chuẩn hóa khoảng trắng và so sánh không phân biệt hoa thường)
        String normName1 = normalizeName(fullName);
        String normName2 = normalizeName(student.getFullName());
        if (normName1.isEmpty() || !normName1.equalsIgnoreCase(normName2)) {
            throw new BusinessException(ErrorCode.STUDENT_INFO_MISMATCH);
        }

        // Bước 5: Kiểm tra tài khoản đã tồn tại
        if (userRepository.existsByStudentRegistryId(student.getId()) ||
            userProfileRepository.existsByStudentCode(student.getStudentCode())) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_REGISTERED);
        }

        return student;
    }

    // ============================================================
    // Helper Methods
    // ============================================================
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_CSV_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_CSV_FILE);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new BusinessException(ErrorCode.INVALID_CSV_FILE);
        }
    }

    private List<ParsedRow> parseCsv(MultipartFile file) {
        List<ParsedRow> rows = new ArrayList<>();
        Set<String> seenStudentCodes = new HashSet<>();
        Set<String> seenEmails = new HashSet<>();
        Set<String> seenPhones = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException(ErrorCode.INVALID_CSV_FILE);
            }

            // Remove UTF-8 BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                colMap.put(canonicalHeader(headers.get(i)), i);
            }

            String[] requiredCols = {"student_code", "email", "phone", "full_name", "class_name"};
            for (String col : requiredCols) {
                if (!colMap.containsKey(col)) {
                    throw new BusinessException(ErrorCode.INVALID_CSV_HEADER);
                }
            }

            int codeIdx = colMap.get("student_code");
            int emailIdx = colMap.get("email");
            int phoneIdx = colMap.get("phone");
            int nameIdx = colMap.get("full_name");
            int classIdx = colMap.get("class_name");
            Integer statusIdx = colMap.get("status");

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                List<String> values = parseCsvLine(line);
                String code = getColValue(values, codeIdx);
                String email = getColValue(values, emailIdx);
                String phone = getColValue(values, phoneIdx);
                String fullName = getColValue(values, nameIdx);
                String className = getColValue(values, classIdx);
                String statusValue = statusIdx == null ? "ACTIVE" : getColValue(values, statusIdx);

                ParsedRow row = new ParsedRow();
                row.rowNumber = lineNum;
                row.studentCode = code;
                row.email = email;
                row.phone = phone;
                row.fullName = fullName;
                row.className = className;
                row.status = parseStatus(statusValue);
                row.isValid = true;

                // Field validations
                if (code.isEmpty()) {
                    row.isValid = false;
                    row.error = "Mã sinh viên không được để trống";
                } else if (email.isEmpty()) {
                    row.isValid = false;
                    row.error = "Email không được để trống";
                } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                    row.isValid = false;
                    row.error = "Email không đúng định dạng: " + email;
                } else if (phone.isEmpty()) {
                    row.isValid = false;
                    row.error = "Số điện thoại không được để trống";
                } else if (!PHONE_PATTERN.matcher(phone).matches()) {
                    row.isValid = false;
                    row.error = "Số điện thoại không đúng định dạng: " + phone;
                } else if (fullName.isEmpty()) {
                    row.isValid = false;
                    row.error = "Họ tên không được để trống";
                } else if (className.isEmpty()) {
                    row.isValid = false;
                    row.error = "Lớp không được để trống";
                } else if (row.status == null) {
                    row.isValid = false;
                    row.error = "Trạng thái không hợp lệ: " + statusValue;
                }

                // In-file duplicate detection
                String codeKey = code.toLowerCase();
                String emailKey = email.toLowerCase();
                String phoneKey = normalizePhone(phone);

                if (row.isValid) {
                    if (seenStudentCodes.contains(codeKey)) {
                        row.isDuplicate = true;
                        row.isValid = false;
                        row.error = "Trùng lặp mã sinh viên trong file: " + code;
                    } else if (seenEmails.contains(emailKey)) {
                        row.isDuplicate = true;
                        row.isValid = false;
                        row.error = "Trùng lặp email trong file: " + email;
                    } else if (!phoneKey.isEmpty() && seenPhones.contains(phoneKey)) {
                        row.isDuplicate = true;
                        row.isValid = false;
                        row.error = "Trùng lặp số điện thoại trong file: " + phone;
                    } else {
                        seenStudentCodes.add(codeKey);
                        seenEmails.add(emailKey);
                        if (!phoneKey.isEmpty()) seenPhones.add(phoneKey);

                        // Check existing in DB
                        row.isExisting = studentRegistryRepository.existsByStudentCodeIgnoreCase(code);
                    }
                }

                rows.add(row);
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CSV_FILE);
        }

        return rows;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString().trim());
        return list;
    }

    private static String getColValue(List<String> values, int index) {
        if (index >= 0 && index < values.size()) {
            return values.get(index).trim();
        }
        return "";
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9+]", "");
    }

    private static String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", " ");
    }

    private static String canonicalHeader(String header) {
        String normalized = header == null ? "" : header.trim().toLowerCase();
        return switch (normalized) {
            case "mã sinh viên", "mã sinh viên (*)" -> "student_code";
            case "họ và tên", "họ và tên (*)" -> "full_name";
            case "email sinh viên", "email sinh viên (*)" -> "email";
            case "số điện thoại", "số điện thoại (*)" -> "phone";
            case "lớp / khóa", "lớp / khóa (*)" -> "class_name";
            case "trạng thái" -> "status";
            default -> normalized;
        };
    }

    private static StudentRegistryStatus parseStatus(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("ACTIVE")
                || value.equalsIgnoreCase("Đang hoạt động")) {
            return StudentRegistryStatus.ACTIVE;
        }
        if (value.equalsIgnoreCase("INACTIVE") || value.equalsIgnoreCase("Tạm ngưng")) {
            return StudentRegistryStatus.INACTIVE;
        }
        return null;
    }

    private UUID getCurrentUserId() {
        String username = CurrentUser.username();
        if (username != null) {
            return userRepository.findByUsername(username)
                    .map(com.project.base_v1.entity.User::getId)
                    .orElse(null);
        }
        return null;
    }

    private static class ParsedRow {
        int rowNumber;
        String studentCode = "";
        String email = "";
        String phone = "";
        String fullName = "";
        String className = "";
        StudentRegistryStatus status = StudentRegistryStatus.ACTIVE;
        boolean isValid = false;
        boolean isDuplicate = false;
        boolean isExisting = false;
        String error;
    }
}
