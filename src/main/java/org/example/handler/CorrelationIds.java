package org.example.handler;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/**
 * One rule for the correlation id, used by the advice (Lab 4) and by the security
 * handlers (Lab 5): echo the caller's X-Correlation-Id, otherwise mint a new one.
 */
public final class CorrelationIds {

    public static final String HEADER = "X-Correlation-Id";
    private static final int MAX_LENGTH = 128;

    private CorrelationIds() {
    }

    public static String resolve(HttpServletRequest request) {
        String sent = request.getHeader(HEADER);
        boolean usable = sent != null
                && !sent.isBlank()
                && sent.length() <= MAX_LENGTH
                && sent.chars().noneMatch(Character::isISOControl);
        return usable ? sent.strip() : UUID.randomUUID().toString();
    }
}
