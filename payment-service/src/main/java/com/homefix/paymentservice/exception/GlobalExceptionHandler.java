package com.homefix.paymentservice.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central error handling for payment-service.
 *
 * Consistent error shape across all HomeFix services:
 * { "timestamp": "...", "status": 404, "error": "Not Found",
 *   "message": "...", "path": "..." }
 * Validation errors additionally include a "details" map with field-level messages.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private Map<String, Object> errorBody(HttpStatus status, String error,
                                          String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = errorBody(
                HttpStatus.BAD_REQUEST, "Validation Failed",
                "Validation failed for one or more fields", request);
        response.put("details", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(
            PaymentException ex, HttpServletRequest request) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(errorBody(
                status, status == HttpStatus.NOT_FOUND ? "Not Found" : "Bad Request",
                ex.getMessage(), request));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(
                HttpStatus.NOT_FOUND, "Not Found", "The requested resource was not found", request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(errorBody(
                HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(
                HttpStatus.FORBIDDEN, "Forbidden", "Access denied", request));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(
            FeignException ex, HttpServletRequest request) {
        HttpStatus httpStatus = ex.status() >= 400
                ? HttpStatus.valueOf(ex.status())
                : HttpStatus.SERVICE_UNAVAILABLE;

        String error = "Service communication error";
        String message = "An upstream service could not complete the request.";
        if (httpStatus == HttpStatus.NOT_FOUND) {
            error = "Not Found";
            message = "Referenced resource not found in the system";
        } else if (httpStatus.is5xxServerError()) {
            error = "Service Unavailable";
            message = "A required service is currently unavailable. Please try again later.";
        }
        return ResponseEntity.status(httpStatus).body(errorBody(httpStatus, error, message, request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        // Log server-side for debugging; never leak the message/stack trace to the client.
        log.error("Unhandled exception on {}: ", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request));
    }
}
