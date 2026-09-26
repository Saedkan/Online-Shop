package org.example.domain;

public interface Rule {
    void check(OrderStatus from, OrderStatus to);
}