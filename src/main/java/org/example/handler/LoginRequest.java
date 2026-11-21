package org.example.handler;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/v1/auth/login. The only request that carries a password. */
public record LoginRequest(
        @NotBlank(message = "must not be blank") String username,
        @NotBlank(message = "must not be blank") String password
) {
    /** A record prints all its fields by default. Never let the password reach a log line. */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}
