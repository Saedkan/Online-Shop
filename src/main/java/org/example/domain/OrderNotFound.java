package org.example.domain;

import java.util.UUID;

/**
 * Unchecked on purpose, same as DuplicateOrder. Plain Java, no framework imports.
 * Only the web advice knows that this means 404.
 */
public class OrderNotFound extends RuntimeException {
    public OrderNotFound(UUID id) {
        super("order not found: " + id);
    }
}
