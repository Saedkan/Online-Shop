package org.example.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body of POST /api/v1/orders. Validation lives here, never in domain.
 * The messages are written out so the text does not change with the JVM's language.
 */
public record CreateOrderRequest(
        @NotBlank(message = "must not be blank") String businessKey,
        @NotBlank(message = "must not be blank")
        @Size(max = 120, message = "size must be between {min} and {max}") String title
) {
}
