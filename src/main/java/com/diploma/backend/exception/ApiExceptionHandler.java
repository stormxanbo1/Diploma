package com.diploma.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик ошибок.  Возвращает единый JSON-формат:
 * { code, message, timestamp }
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /* ---------- 404 ---------- */

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    /* ---------- 400 валидация ---------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", details);
    }

    /* ---------- 400 – дубли и прочие bad-request ---------- */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegal(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    /* ---------- 401 ---------- */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> creds(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", ex.getMessage());
    }

    /* ---------- 403 ---------- */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }

    /* ---------- 500 fallback ---------- */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        ex.printStackTrace();                                   // для отладки
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName(), ex.getMessage());
    }

    /* ---------- helper ---------- */

    private ResponseEntity<ApiError> build(HttpStatus status,
                                           String code,
                                           String message) {
        ApiError error = new ApiError(code, message, LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
