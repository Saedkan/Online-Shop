package org.example.domain;

/**
 * Value object for a business key.
 * <p>
 * Memory: {@code new OrderId("ORD-1")} allocates the record on the <b>heap</b>.
 * A local variable is only a reference on the <b>stack</b>. The String
 * {@code value} is also a heap object. Primitives and references live on the
 * stack; objects live on the heap.
 */
public record OrderId(String value) {
    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
    }
}