package org.example.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/** The signing side (login) and the checking side (every API call) share one secret. */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    /** Signs tokens at login. HS256. */
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(props.secret())));
    }

    /** Checks signature (HS256), expiry and issuer on every call. */
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties props) {
        return decoder(props.secret(), props.issuer());
    }

    static SecretKey secretKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    static NimbusJwtDecoder decoder(String secret, String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // default validators = expiry / not-before, plus the issuer must be this product
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
