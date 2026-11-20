package org.example.handler;

import org.example.domain.Order;
import org.example.domain.OrderStatus;
import org.example.security.ApiAccessDeniedHandler;
import org.example.security.ApiAuthenticationEntryPoint;
import org.example.security.SecurityConfig;
import org.example.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice for Lab 5 rules: 401 without token, 403 for CLERK on pay, MANAGER succeeds,
 * /api/me, and health open without a token.
 */
@WebMvcTest(controllers = {
        OrderController.class,
        MeController.class,
        AuthController.class,
        SecurityWebTest.HealthStub.class
})
@Import({
        OrderController.class,
        MeController.class,
        AuthController.class,
        SecurityWebTest.HealthStub.class,
        ApiExceptionHandler.class,
        SecurityConfig.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class
})
class SecurityWebTest {

    /** Stand-in for actuator so the chain rule can be asserted without a full Boot context. */
    @RestController
    static class HealthStub {
        @GetMapping("/actuator/health")
        ResponseEntity<Map<String, String>> health() {
            return ResponseEntity.ok(Map.of("status", "UP"));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void getOrderWithoutTokenIs401AndDoesNotCallService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("valid token required"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());

        verify(service, never()).get(any());
    }

    @Test
    void clerkOnPayIs403AndDoesNotCallService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/orders/{id}/pay", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLERK"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("not allowed for your role"));

        verify(service, never()).pay(any(), any());
    }

    @Test
    void managerOnPayCallsService() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.pay(eq(id), eq("manager-dev")))
                .thenReturn(new Order(id, "ORD-9", OrderStatus.PAID, "Laptop"));

        mockMvc.perform(post("/api/v1/orders/{id}/pay", id)
                        .with(jwt().jwt(j -> j.subject("manager-dev"))
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(service).pay(id, "manager-dev");
    }

    @Test
    void meReturnsUsernameFromToken() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(jwt().jwt(j -> j.subject("clerk-dev"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLERK"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("clerk-dev"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_CLERK"));
    }

    @Test
    void healthIsOpenWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void createIsAllowedForClerk() throws Exception {
        when(service.register(any(UUID.class), eq("ORD-2"), eq("Phone")))
                .thenAnswer(invocation -> new Order(
                        invocation.getArgument(0), "ORD-2", OrderStatus.CART, "Phone"));

        mockMvc.perform(post("/api/v1/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLERK")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"ORD-2","title":"Phone"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CART"));
    }
}
