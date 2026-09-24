package org.example.domain;

public class TransitionRule implements Rule {
    @Override
    public void check(OrderStatus from, OrderStatus to) {
        if (from == OrderStatus.CART && to == OrderStatus.PAID) {
            return;
        }
        if (from == OrderStatus.PAID && to == OrderStatus.SHIPPED) {
            return;
        }
    }
}