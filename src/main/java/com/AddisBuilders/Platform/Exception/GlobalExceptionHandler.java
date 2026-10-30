package com.AddisBuilders.Platform.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle Validation Errors (e.g., @NotBlank, @Email failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    // 2. Handle malformed or unreadable request bodies (bad JSON, unknown enum value, wrong type)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Malformed request body or invalid value");
    }

    // 3. Handle Business / IllegalArgument Exceptions (e.g., Email already in use)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 4. Handle Bad Credentials during Login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    // 5. Handle other authentication failures
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }

    // 6. Handle authorization failures raised by method security or service ownership checks
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return errorResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    // 7. Handle database constraint violations (duplicates, invalid references)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "A database constraint violation occurred. Check for duplicate entries or invalid references.";
        String rootMsg = ex.getRootCause() != null && ex.getRootCause().getMessage() != null
                ? ex.getRootCause().getMessage().toLowerCase()
                : "";

        if (rootMsg.contains("email")) {
            message = "This email address is already registered. Please use another email or sign in.";
        } else if (rootMsg.contains("idx_course_module_order")) {
            message = "A module with this order sequence already exists for this course.";
        } else if (rootMsg.contains("idx_module_lesson_order")) {
            message = "A lesson with this order sequence already exists for this module.";
        } else if (rootMsg.contains("duplicate entry")) {
            message = "A record with the same unique value already exists.";
        }

        return errorResponse(HttpStatus.CONFLICT, message);
    }

    // 8. Fallback Generic Exception Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        log.error("Unhandled exception while processing request", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
