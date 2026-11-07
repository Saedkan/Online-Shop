package org.example.handler;

/** The only shape of a failure body: code, message, correlationId. No stack trace, no exception class, no entity. */
public record ApiError(String code, String message, String correlationId) {
}
