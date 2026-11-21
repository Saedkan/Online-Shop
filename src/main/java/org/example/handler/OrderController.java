package org.example.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.domain.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * HTTP adapter. It chooses the status code; the service still does the work.
 * The path id is the UUID, the business key travels in the body.
 * Every call needs a token (CLERK or MANAGER); the locked action needs MANAGER.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders")
@SecurityRequirement(name = "bearer")
@ApiResponse(responseCode = "401", description = "UNAUTHENTICATED: valid token required",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create one order")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Created. Location points to the new order.",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDATION: blank field, title over 120, body is not JSON",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "DUPLICATE: this business key is already stored",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        UUID id = UUID.randomUUID();
        Order order = service.register(id, request.businessKey(), request.title());
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(OrderResponse.from(order));
    }

}