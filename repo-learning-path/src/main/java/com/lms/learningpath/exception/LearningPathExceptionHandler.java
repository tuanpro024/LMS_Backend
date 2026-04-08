package com.lms.learningpath.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.lms.common.dto.ApiResponse;
import com.lms.common.exception.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.lms.learningpath")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LearningPathExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        String message = "Invalid request payload";

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();
            String field = invalidFormatException.getPath().isEmpty()
                    ? "value"
                    : invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getFieldName();

            if (targetType != null && targetType.isEnum()) {
                String allowedValues = Arrays.stream(targetType.getEnumConstants())
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                message = "Invalid value for '" + field + "'. Allowed values: " + allowedValues;
            } else {
                message = "Invalid value for '" + field + "'";
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.code(), message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.code(), ex.getMessage()));
    }
}
