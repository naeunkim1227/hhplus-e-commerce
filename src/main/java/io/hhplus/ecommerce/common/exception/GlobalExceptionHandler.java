package io.hhplus.ecommerce.common.exception;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.common.response.CommonErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        CommonErrorResponse error = CommonErrorResponse.of(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        CommonResponse<Void> response = CommonResponse.error(
                errorCode.getStatus().value(),
                error
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * Validation 예외 처리 (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        Map<String, Object> details = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        CommonErrorResponse error = CommonErrorResponse.of(
                CommonErrorCode.VALIDATION_FAILED.getCode(),
                CommonErrorCode.VALIDATION_FAILED.getMessage(),
                details
        );

        CommonResponse<Void> response = CommonResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                error
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Validation 예외 처리 (@ModelAttribute)
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<CommonResponse<Void>> handleBindException(org.springframework.validation.BindException e) {
        log.warn("Bind validation failed: {}", e.getMessage());

        Map<String, Object> details = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        CommonErrorResponse error = CommonErrorResponse.of(
                CommonErrorCode.VALIDATION_FAILED.getCode(),
                CommonErrorCode.VALIDATION_FAILED.getMessage(),
                details
        );

        CommonResponse<Void> response = CommonResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                error
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Validation 예외 처리 (@RequestParam, @PathVariable)
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handleConstraintViolationException(jakarta.validation.ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());

        Map<String, Object> details = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            details.put(propertyPath, message);
        });

        CommonErrorResponse error = CommonErrorResponse.of(
                CommonErrorCode.VALIDATION_FAILED.getCode(),
                CommonErrorCode.VALIDATION_FAILED.getMessage(),
                details
        );

        CommonResponse<Void> response = CommonResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                error
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 예상 외의 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);

        CommonErrorResponse error = CommonErrorResponse.of(
                CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        CommonResponse<Void> response = CommonResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}