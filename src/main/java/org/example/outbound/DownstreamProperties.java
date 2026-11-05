package org.example.outbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Where the downstream team lives and how long we wait for it. Bound from yaml
 * (app.downstream.base-url, app.downstream.timeout). Lab 6 makes the call; this lab only binds the values.
 */
@ConfigurationProperties(prefix = "app.downstream")
@Validated
public record DownstreamProperties(
        @NotBlank String baseUrl,
        @NotNull Duration timeout
) {
}
