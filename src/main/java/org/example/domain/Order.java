package org.example.domain;

import java.util.UUID;

/**
 * The order as the domain sees it. Plain Java: no Spring, no JPA, no Jackson.
 * The web layer maps it to a response record, the persistence layer maps the entity to it.
 */
public record Order(UUID id, String businessKey, OrderStatus status, String title) {
}
