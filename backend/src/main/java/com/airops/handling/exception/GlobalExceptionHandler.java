package com.airops.handling.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Validation errors ──────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErrorBody(HttpStatus.BAD_REQUEST.value(),
                        "Validation failed", fields));
    }

    // ── Domain exceptions ──────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorBody> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorBody(404, ex.getMessage(), null));
    }

    @ExceptionHandler(ShiftConflictException.class)
    ResponseEntity<ErrorBody> handleConflict(ShiftConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorBody(409, ex.getMessage(), null));
    }

    @ExceptionHandler(ContractViolationException.class)
    ResponseEntity<ErrorBody> handleContractViolation(ContractViolationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorBody(422, ex.getMessage(), null));
    }

    // ── Security ───────────────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorBody> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorBody(401, "Invalid username or password", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorBody> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorBody(403, "Access denied", null));
    }

    // ── Catch-all ─────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorBody> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorBody(500, "An unexpected error occurred", null));
    }

    // ── Error body record ─────────────────────────────────────────────────
    public record ErrorBody(
            int status,
            String message,
            Map<String, String> errors,
            LocalDateTime timestamp
    ) {
        ErrorBody(int status, String message, Map<String, String> errors) {
            this(status, message, errors, LocalDateTime.now());
        }
    }
}
