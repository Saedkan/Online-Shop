package org.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Turns a successful login into a signed JWT. The payload is readable by anyone,
 * so it carries only: iss, sub, iat, exp and roles. No password, no personal data.
 */
@Service
public class TokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    /** The token string and how many seconds it stays valid. */
    public record IssuedToken(String value, long expiresInSeconds) {
        @Override
        public String toString() {
            return "IssuedToken[expiresInSeconds=" + expiresInSeconds + "]"; // never print the token
        }
    }

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public TokenService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public IssuedToken issue(Authentication authentication) {
        Instant now = Instant.now();

        // ROLE_MANAGER -> MANAGER: the claim holds plain role names
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .sorted()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(props.ttl()))
                .claim("roles", List.copyOf(roles))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, props.ttl().toSeconds());
    }
}
