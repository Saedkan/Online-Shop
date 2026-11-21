
package org.example.security;

import org.example.handler.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    @Test
    void issuesHs256TokenThatDecoderReadsBack() {
        String secret = "test-secret-at-least-32-bytes-ok!";
        assertThat(secret.length()).isGreaterThanOrEqualTo(32);

        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        JwtProperties properties = new JwtProperties(
                secret,
                "online-shop",
                Duration.ofSeconds(900)
        );

        TokenService tokenService = new TokenService(encoder, properties);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "manager-dev",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
                );

        TokenService.IssuedToken issued = tokenService.issue(authentication);

        assertThat(issued.value()).isNotBlank();
        assertThat(issued.expiresInSeconds()).isEqualTo(900L);

        Jwt jwt = decoder.decode(issued.value());

        assertThat(jwt.getSubject()).isEqualTo("manager-dev");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("online-shop");
        assertThat(jwt.getClaimAsStringList("roles"))
                .containsExactly("MANAGER");
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
    }
}

