package org.example.domain;

public class TransitionRule implements Rule {
    @Override
    public void check(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        boolean validTransition =
                (from == OrderStatus.CART && to == OrderStatus.PAID)
                || (from == OrderStatus.PAID && to == OrderStatus.SHIPPED);

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: " + from + " to " + to
            );
        }
    }
}