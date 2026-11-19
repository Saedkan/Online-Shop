package org.example.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Me")
public class MeController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Username and authorities from the current token")
    @ApiResponse(responseCode = "200", description = "Identity from the Bearer token",
            content = @Content(schema = @Schema(implementation = MeResponse.class)))
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED: valid token required",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeResponse me(Authentication authentication) {
        return new MeResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}
