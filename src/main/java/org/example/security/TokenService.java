package org.example.security;

import org.example.handler.TokenResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Service
public class TokenService {

    static final long EXPIRES_IN_SECONDS = 900L;

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public TokenResponse issue(String username, Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(EXPIRES_IN_SECONDS);

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(TokenService::withoutRolePrefix)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(username)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", EXPIRES_IN_SECONDS);
    }

    private static String withoutRolePrefix(String authority) {
        return authority.startsWith("ROLE_") ? authority.substring("ROLE_".length()) : authority;
    }
}
