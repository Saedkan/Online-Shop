package org.example.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/** Same X-Correlation-Id rules as Lab 4's exception advice. */
final class CorrelationIds {

    static final String HEADER = "X-Correlation-Id";
    private static final int MAX_LENGTH = 128;

    private CorrelationIds() {
    }

    static String resolve(HttpServletRequest request) {
        String sent = request.getHeader(HEADER);
        boolean usable = sent != null
                && !sent.isBlank()
                && sent.length() <= MAX_LENGTH
                && sent.chars().noneMatch(Character::isISOControl);
        return usable ? sent.strip() : UUID.randomUUID().toString();
    }
}
