package org.example.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.handler.ApiError;
import org.example.handler.CorrelationIds;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 401 and 403 are decided inside the filter chain, before any controller runs, so the
 * @RestControllerAdvice never sees them. These two handlers write the same error record
 * (code, message, correlationId) as every other failure.
 */
public class ApiSecurityErrors implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ApiSecurityErrors(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** No token, a malformed token, a wrong signature, an expired token or a foreign issuer. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        write(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "valid token required");
    }

    /** A valid token whose role is not allowed here. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "not allowed for your role");
    }

    private void write(HttpServletRequest request, HttpServletResponse response,
                       HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(),
                new ApiError(code, message, CorrelationIds.resolve(request)));
    }
}