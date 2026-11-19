package org.example.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.example.handler.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** No web layer: issue a token, then decode it with the same secret. */
class TokenServiceTest {

    @Test
    void issuesHs256TokenThatDecoderReadsBack() {
        String secret = "test-secret-at-least-32-bytes-ok!";
        assertThat(secret.length()).isGreaterThanOrEqualTo(32);

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setIssuer("online-shop");

        TokenService tokenService = new TokenService(encoder, properties);
        TokenResponse issued = tokenService.issue(
                "manager-dev",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        assertThat(issued.tokenType()).isEqualTo("Bearer");
        assertThat(issued.expiresIn()).isEqualTo(900L);

        Jwt jwt = decoder.decode(issued.accessToken());
        assertThat(jwt.getSubject()).isEqualTo("manager-dev");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("online-shop");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("MANAGER");
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
    }
}
