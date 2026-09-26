package org.example.handler;

import org.springframework.stereotype.Service;
import org.example.domain.OrderStatus;
import org.example.domain.Rule;

@Service
public class OrderService {
    private final Rule rule;

    public OrderService(Rule rule) {
        this.rule = rule;
    }

    public OrderStatus move(OrderStatus from, OrderStatus to) {
        rule.check(from, to);
        return to;
    }
}