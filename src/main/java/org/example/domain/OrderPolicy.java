package org.example.domain;

public class OrderPolicy {

    public OrderStatus move(OrderId id, OrderStatus from, OrderStatus to) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        if (from == null && to == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        if (from == OrderStatus.CART && to == OrderStatus.PAID) {
            return to;
        }

        if (from == OrderStatus.PAID && to == OrderStatus.SHIPPED) {
            return to;
        }

        if (from == OrderStatus.DELIVERED && to == OrderStatus.RETURNED) {
            throw new IllegalArgumentException(
                    "Cannot return a delivered order directly. Please use the separate return process."
            );
        }

        if (from == OrderStatus.SHIPPED && to == OrderStatus.CART) {
            throw new IllegalArgumentException(
                    "Cannot move a shipped order back to cart. Order is already in logistics."
            );
        }

        throw new IllegalArgumentException(
                "Invalid order status transition: " + from + " → " + to
        );
    }
}