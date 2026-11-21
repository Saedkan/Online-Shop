package org.example.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.security.TokenService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login is the only call that sees a password. AuthenticationManager checks the name and the
 * BCrypt hash, TokenService signs the token. A wrong name and a wrong password throw the same
 * exception, so the advice answers both with the same 401 and the same text.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokens) {
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Log in and receive a 15 minute JWT")
    @ApiResponse(responseCode = "200", description = "Token issued",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDATION: blank username or password",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED: bad username or password",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        TokenService.IssuedToken token = tokens.issue(authentication);
        return new LoginResponse(token.value(), TOKEN_TYPE, token.expiresInSeconds());
    }
}
