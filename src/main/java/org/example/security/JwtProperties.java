package org.example.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT signing settings. The secret lives only in profile yaml (or CSS_JWT_SECRET in prod).
 * A value shorter than 32 characters fails at startup.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private String issuer;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    void requireValidSecret() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("app.jwt.issuer must be set");
        }
    }
}
