package org.example.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.DuplicateOrder;
import org.example.domain.OrderNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
 * The one place that knows 400, 404, 409, 415 and 500.
 * Domain exceptions stay plain Java; this class turns them into the error record.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final int MAX_CORRELATION_LENGTH = 128;

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

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> unsupportedMediaType(HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED", "content type must be application/json", request);
    }

    /**
     * Everything else. Spring's own 4xx exceptions (unknown path, wrong method, ...) keep their
     * status and get the same error record; anything unmapped is a 500 whose text stays in the log.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> other(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse springError && springError.getStatusCode().is4xxClientError()) {
            HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
            return build(status, status.name(), status.getReasonPhrase().toLowerCase(), request);
        }
        String correlationId = correlationId(request);
        log.error("unexpected error, correlationId={}", correlationId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL", "unexpected error", correlationId));
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                                  HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(code, message, correlationId(request)));
    }

    /** "title must not be blank", "business key must not be blank". */
    private static String describe(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return words(fieldError.getField()) + " " + fieldError.getDefaultMessage();
        }
        return String.valueOf(error.getDefaultMessage());
    }

    private static String words(String camelCase) {
        return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase();
    }

    /** The caller's id if it sent a sane one, otherwise a fresh one. */
    private static String correlationId(HttpServletRequest request) {
        String sent = request.getHeader(CORRELATION_HEADER);
        boolean usable = sent != null
                && !sent.isBlank()
                && sent.length() <= MAX_CORRELATION_LENGTH
                && sent.chars().noneMatch(Character::isISOControl);
        return usable ? sent.strip() : UUID.randomUUID().toString();
    }
}