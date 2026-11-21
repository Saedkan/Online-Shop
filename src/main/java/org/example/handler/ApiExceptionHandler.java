package org.example.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.DuplicateOrder;
import org.example.domain.InvalidTransition;
import org.example.domain.OrderNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

/**
 * The one place that knows 400, 401 (login), 403 (method security), 404, 409, 415 and 500 for
 * exceptions thrown inside a controller. Domain exceptions stay plain Java; this class turns
 * them into the error record. (401 and 403 from the filter chain are written by ApiSecurityErrors.)
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(ApiExceptionHandler::describe)
                .sorted()
                .reduce((a, b) -> a + "; " + b)
                .orElse("request is not valid");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> badPathValue(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = UUID.class.equals(ex.getRequiredType())
                ? ex.getName() + " must be a UUID"
                : ex.getName() + " is not valid";
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> notJson(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", "body must be JSON", request);
    }

    @ExceptionHandler(OrderNotFound.class)
    ResponseEntity<ApiError> notFound(OrderNotFound ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateOrder.class)
    ResponseEntity<ApiError> duplicate(DuplicateOrder ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DUPLICATE", ex.getMessage(), request);
    }

    private static String describe(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + " " + fieldError.getDefaultMessage();
        }

        return error.getDefaultMessage();
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        String correlationId = request.getHeader("X-Correlation-Id");

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Не позволяем слишком длинному значению попасть в ответ.
        if (correlationId.length() > 128) {
            correlationId = correlationId.substring(0, 128);
        }

        ApiError error = new ApiError(
                code,
                message,
                correlationId
        );

        return ResponseEntity
                .status(status)
                .header("X-Correlation-Id", correlationId)
                .body(error);
    }
}