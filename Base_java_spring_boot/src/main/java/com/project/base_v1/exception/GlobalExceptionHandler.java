package com.project.base_v1.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.core.ErrorResponseSever;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.LazyInitializationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.message());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleBusiness(BusinessException ex) {
        ErrorCode code = ex.getErrorCode();
        return buildErrorResponse(code, code.message());
    }

    /**
     * Lỗi validate @RequestBody DTO.
     * Ví dụ: @NotBlank, @NotNull, @Size trong request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.VALIDATION_ERROR.message());

        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * Lỗi validate @RequestParam, @PathVariable.
     * Ví dụ: @Min, @Max, @NotNull trên param.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse(ErrorCode.VALIDATION_ERROR.message());

        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * Lỗi thiếu request param.
     * Ví dụ API cần ?page= nhưng client không gửi.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMissingRequestParam(
            MissingServletRequestParameterException ex
    ) {
        String message = "Thiếu tham số bắt buộc: " + ex.getParameterName();

        return buildErrorResponse(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * Lỗi sai kiểu dữ liệu param/path variable.
     * Ví dụ cần UUID nhưng client gửi abc.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = "Tham số '" + ex.getName() + "' không hợp lệ. Kiểu dữ liệu yêu cầu: " + requiredType;

        return buildErrorResponse(ErrorCode.INVALID_PARAMETER, message);
    }

    /**
     * Lỗi JSON sai format.
     * Ví dụ:
     * - Body không đúng JSON
     * - Enum truyền sai value
     * - LocalDate sai format
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleJsonParse(
            HttpMessageNotReadableException ex
    ) {
        String message = ErrorCode.INVALID_REQUEST_BODY.message();

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath() != null && !ife.getPath().isEmpty()
                    ? ife.getPath().get(0).getFieldName()
                    : "unknown";

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                message = "Giá trị không hợp lệ cho trường '" +
                        fieldName +
                        "'. Các giá trị hợp lệ: " +
                        Arrays.toString(ife.getTargetType().getEnumConstants());
            } else {
                message = "Giá trị không hợp lệ cho trường '" + fieldName + "'";
            }
        }

        return buildErrorResponse(ErrorCode.INVALID_REQUEST_BODY, message);
    }

    /**
     * Lỗi unique key, foreign key, not null, constraint DB.
     * Ví dụ:
     * - username/email bị trùng
     * - FK không tồn tại
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {
        String message = ErrorCode.DATA_INTEGRITY_VIOLATION.message();

        String rootMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : "";

        if (rootMessage != null) {
            String lower = rootMessage.toLowerCase();

            if (lower.contains("duplicate") || lower.contains("unique")) {
                message = "Dữ liệu đã tồn tại";
            } else if (lower.contains("foreign key")) {
                message = "Dữ liệu liên kết không tồn tại";
            } else if (lower.contains("not-null") || lower.contains("null value")) {
                message = "Thiếu dữ liệu bắt buộc";
            }
        }

        return buildErrorResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, message);
    }

    /**
     * Lỗi không tạo được transaction DB.
     * Ví dụ:
     * - DB tắt
     * - DB không kết nối được
     * - Connection pool lỗi
     */
    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleCannotCreateTransaction(
            CannotCreateTransactionException ex
    ) {
        ex.printStackTrace();

        return buildErrorResponse(
                ErrorCode.DATABASE_ERROR,
                ErrorCode.DATABASE_ERROR.message()
        );
    }

    /**
     * Lỗi transaction khi commit/rollback.
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleTransactionSystem(
            TransactionSystemException ex
    ) {
        ex.printStackTrace();

        return buildErrorResponse(
                ErrorCode.TRANSACTION_ERROR,
                ErrorCode.TRANSACTION_ERROR.message()
        );
    }

    /**
     * Lỗi JPA/Hibernate lazy load khi session đã đóng.
     * Ví dụ:
     * failed to lazily initialize a collection of role: ..., could not initialize proxy - no Session
     */
    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleLazyInitialization(
            LazyInitializationException ex
    ) {
        ex.printStackTrace();

        return buildErrorResponse(
                ErrorCode.LAZY_LOADING_ERROR,
                ErrorCode.LAZY_LOADING_ERROR.message()
        );
    }

    /**
     * Lỗi DB chung từ Spring Data.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataAccess(
            DataAccessException ex
    ) {
        ex.printStackTrace();

        return buildErrorResponse(
                ErrorCode.DATABASE_ERROR,
                ErrorCode.DATABASE_ERROR.message()
        );
    }

    /**
     * Fallback cuối cùng.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseSever<Void>> handleSystem(Exception ex) {
        ex.printStackTrace();

        return buildErrorResponse(
                ErrorCode.SYSTEM_ERROR,
                ErrorCode.SYSTEM_ERROR.message()
        );
    }

    private ResponseEntity<ApiResponseSever<Void>> buildErrorResponse(
            ErrorCode code,
            String message
    ) {
        return ResponseEntity
                .status(code.status())
                .body(new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(code.code(), message)
                ));
    }
}
