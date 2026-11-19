package org.example.handler;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/v1/auth/login. Validation stays in the web package. */
public record LoginRequest(
        @NotBlank(message = "must not be blank") String username,
        @NotBlank(message = "must not be blank") String password
) {
}
