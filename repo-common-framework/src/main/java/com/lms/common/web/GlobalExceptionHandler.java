package com.lms.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import com.lms.common.dto.ApiResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        ErrorCode code = ex.getErrorCode() == null ? ErrorCode.INTERNAL_ERROR : ex.getErrorCode();
        HttpStatus status = code.status();
        ApiResponse<Void> body = ApiResponse.error(code.code(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(err -> err instanceof FieldError fe ? fe.getField() + " " + fe.getDefaultMessage()
                        : err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.BAD_REQUEST.code(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.BAD_REQUEST.code(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.FORBIDDEN.code(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeWithResponseStatus(RuntimeException ex) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus == null) {
            return handleGeneric(ex);
        }

        HttpStatus status = responseStatus.code() != HttpStatus.INTERNAL_SERVER_ERROR
                ? responseStatus.code()
                : responseStatus.value();
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            return handleGeneric(ex);
        }

        String message = ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
        ApiResponse<Void> body = ApiResponse.error(mapErrorCodeByStatus(status), message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.INTERNAL_ERROR.code(), "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String mapErrorCodeByStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.BAD_REQUEST.code();
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED.code();
            case FORBIDDEN -> ErrorCode.FORBIDDEN.code();
            case NOT_FOUND -> ErrorCode.NOT_FOUND.code();
            case CONFLICT -> ErrorCode.CONFLICT.code();
            case TOO_MANY_REQUESTS -> ErrorCode.TOO_MANY_REQUESTS.code();
            default -> ErrorCode.INTERNAL_ERROR.code();
        };
    }
}
