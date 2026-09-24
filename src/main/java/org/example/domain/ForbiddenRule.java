package org.example.domain;

public class ForbiddenRule implements Rule {
    @Override
    public void check(OrderStatus from, OrderStatus to) {
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
    }
}