package org.example.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of PUT /api/v1/orders/{id}. Only the title is mutable; business key and status stay put.
 */
public record UpdateOrderRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 120, message = "size must be between {min} and {max}") String title
) {
}
