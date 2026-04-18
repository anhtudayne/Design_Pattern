package com.cinema.booking.exception;

import com.cinema.booking.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler using @RestControllerAdvice pattern.
 * Centralizes all exception handling logic to eliminate duplicated try-catch blocks across controllers.
 * 
 * This addresses:
 * - DRY Principle: Removes 23+ duplicated try-catch blocks
 * - SRP: Controllers no longer handle error formatting
 * - Consistency: Standardized error response format across all endpoints
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations
     * Returns 400 Bad Request with detailed field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), fieldErrors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Dữ liệu đầu vào không hợp lệ")
                .path(request.getRequestURI())
                .details(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle missing request parameters
     * Returns 400 Bad Request
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        
        log.warn("Missing parameter '{}' for request to {}", ex.getParameterName(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message("Thiếu tham số bắt buộc: " + ex.getParameterName())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle type mismatch errors (e.g., passing string to integer parameter)
     * Returns 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        log.warn("Type mismatch for parameter '{}' in request to {}", ex.getName(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message("Sai định dạng tham số: " + ex.getName())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle authentication failures (wrong credentials)
     * Returns 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {
        
        log.warn("Authentication failed for request to {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Sai tên đăng nhập hoặc mật khẩu")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle disabled account exceptions
     * Returns 403 Forbidden
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledAccount(
            DisabledException ex,
            HttpServletRequest request) {
        
        log.warn("Disabled account attempted login: {}", request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Account Disabled")
                .message("Tài khoản đã bị vô hiệu hóa")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle locked account exceptions
     * Returns 403 Forbidden
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedAccount(
            LockedException ex,
            HttpServletRequest request) {
        
        log.warn("Locked account attempted login: {}", request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Account Locked")
                .message("Tài khoản đã bị khóa")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle access denied (insufficient permissions)
     * Returns 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.warn("Access denied for request to {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Bạn không có quyền thực hiện thao tác này")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Lỗi gọi cổng thanh toán MoMo / HTTP upstream (403 do IP chưa whitelist, sai endpoint, v.v.)
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(
            RestClientException ex,
            HttpServletRequest request) {

        int upstreamStatus = -1;
        if (ex instanceof HttpStatusCodeException hx) {
            upstreamStatus = hx.getStatusCode().value();
            log.warn("Upstream HTTP error calling MoMo/external {} on {}: {}", upstreamStatus, request.getRequestURI(), hx.getMessage());
        } else {
            log.error("RestClientException on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        }

        String message;
        if (upstreamStatus == 403) {
            message = "Cổng MoMo trả về 403 Forbidden. Thường gặp khi: (1) IP máy chủ backend chưa đăng ký trên MoMo Sandbox, "
                    + "(2) sai endpoint hoặc partner code / secret, (3) môi trường dev — đặt momo.dev-skip-external=true hoặc MOMO_DEV_SKIP_EXTERNAL=true trong .env để bỏ qua gọi MoMo.";
        } else if (upstreamStatus == 401) {
            message = "Cổng MoMo từ chối xác thực (401). Kiểm tra accessKey, secretKey và partnerCode.";
        } else if (upstreamStatus > 0) {
            message = "Không gọi được cổng thanh toán MoMo (HTTP " + upstreamStatus + "). Kiểm tra endpoint và biến môi trường MoMo.";
        } else {
            message = "Lỗi kết nối tới cổng thanh toán: " + ex.getMessage();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Bad Gateway")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException (invalid arguments passed to methods)
     * Returns 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        log.warn("Illegal argument in request to {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle IllegalStateException (invalid state transitions, e.g., booking state)
     * Returns 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {
        
        log.warn("Illegal state in request to {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle business logic exceptions (RuntimeException and subclasses)
     * Returns 400 Bad Request
     * This covers most custom exceptions thrown by services
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        if (ex instanceof RestClientException) {
            return handleRestClientException((RestClientException) ex, request);
        }

        log.error("RuntimeException in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle resource not found (404)
     * Returns 404 Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        
        log.warn("Resource not found: {}", request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("Không tìm thấy tài nguyên yêu cầu")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle all other uncaught exceptions
     * Returns 500 Internal Server Error
     * Logs full stack trace for debugging but hides details from client for security
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Unhandled exception in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
