package org.example.handler;

import com.jayway.jsonpath.JsonPath;
import org.example.domain.DuplicateOrder;
import org.example.domain.Order;
import org.example.domain.OrderNotFound;
import org.example.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice: HTTP contract only. The service is mocked; PostgreSQL does not start.
 */
@WebMvcTest(controllers = OrderController.class)
@Import(ApiExceptionHandler.class)
class OrderControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(service.register(any(UUID.class), eq("ORD-1"), eq("Laptop")))
                .thenAnswer(invocation -> new Order(
                        invocation.getArgument(0),
                        "ORD-1",
                        OrderStatus.CART,
                        "Laptop"));

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"ORD-1","title":"Laptop"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessKey").value("ORD-1"))
                .andExpect(jsonPath("$.status").value("CART"))
                .andExpect(jsonPath("$.title").value("Laptop"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        assertThat(result.getResponse().getHeader("Location")).endsWith("/api/v1/orders/" + id);
    }

    @Test
    void blankTitleIs400AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"ORD-1","title":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(containsString("title")));

        verify(service, never()).register(any(), any(), any());
    }

    @Test
    void missingOrderIs404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new OrderNotFound(id));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateIs409() throws Exception {
        when(service.register(any(UUID.class), eq("ORD-1"), eq("Again")))
                .thenThrow(new DuplicateOrder("ORD-1"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"ORD-1","title":"Again"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void businessKeyInPathIs400() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", "ORD-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(containsString("id must be a UUID")));
    }

    @Test
    void correlationHeaderIsEchoedOn400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(ApiExceptionHandler.CORRELATION_HEADER, "caller-corr-42")
                        .content("""
                                {"businessKey":"","title":"Laptop"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.correlationId").value("caller-corr-42"));
    }
}
