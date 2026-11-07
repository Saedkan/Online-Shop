package org.example.handler;

import org.example.domain.Order;

import java.util.UUID;

/** What the caller sees. Not the JPA entity: the table shape stays private. */
public record OrderResponse(UUID id, String businessKey, String status, String title) {

    static OrderResponse from(Order order) {
        return new OrderResponse(order.id(), order.businessKey(), order.status().name(), order.title());
    }
}
