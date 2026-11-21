package org.example.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        ApiSecurityErrors errors = new ApiSecurityErrors(objectMapper);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                // 👇 ДОБАВЛЯЕМ НАСТРОЙКУ OAuth2 Resource Server 👇
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors)
                )
                // 👆 ------------------------------------------ 👆
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/app.css", "/app.js").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        // ВАЖНО: правило для /api/me должно быть ВЫШЕ, чем /api/v1/**
                        // Но у вас путь /api/me не попадает под /api/v1/**, поэтому порядок не критичен,
                        // однако для порядка лучше ставить специфичные пути выше общих.
                        .requestMatchers(HttpMethod.GET, "/api/me")
                        .hasAnyRole(Roles.CLERK, Roles.MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/*/pay")
                        .hasRole(Roles.MANAGER)
                        .requestMatchers("/api/v1/**")
                        .hasAnyRole(Roles.CLERK, Roles.MANAGER)
                        .anyRequest().denyAll()
                );
        return http.build();
    }

    /**
     * Конвертер, который говорит Spring Security:
     * "Бери роли из claim 'roles', а не из 'scope', и добавляй префикс ROLE_".
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // 👈 ваше поле
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");       // 👈 Spring ждет ROLE_

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}