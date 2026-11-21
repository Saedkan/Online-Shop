package org.example.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Bound from yaml (app.jwt.*). The secret is NOT in application.yaml: dev has a classroom value,
 * prod reads CSS_JWT_SECRET. A secret shorter than 32 characters stops the application at startup.
 */
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "must be at least 32 characters") String secret,
        @NotBlank String issuer,
        @NotNull Duration ttl
) {
}
