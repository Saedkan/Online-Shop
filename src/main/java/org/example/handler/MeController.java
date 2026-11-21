package org.example.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Answers from the Authentication object the filter put there for THIS request.
 * Nothing here comes from the request body, so a caller cannot type a role.
 */
@RestController
@Tag(name = "Auth")
@SecurityRequirement(name = "bearer")
public class MeController {

    @GetMapping(path = "/api/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Who am I: username and authorities from the token")
    @ApiResponse(responseCode = "200", description = "The caller",
            content = @Content(schema = @Schema(implementation = MeResponse.class)))
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED: valid token required",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeResponse me(Authentication authentication) {
        return new MeResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .sorted()
                        .toList());
    }
}
