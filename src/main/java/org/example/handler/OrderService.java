package org.example.handler;

import org.example.domain.OrderId;
import org.example.domain.OrderNotifier;
import org.example.domain.OrderStatus;
import org.example.domain.Rule;
import org.example.persistence.OrderJdbc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final Rule rule;
    private final OrderJdbc orders;
    private final OrderNotifier notifier;

    public OrderService(Rule rule, OrderJdbc orders, OrderNotifier notifier) {
        this.rule = rule;
        this.orders = orders;
        this.notifier = notifier;
    }

    public OrderStatus move(OrderStatus from, OrderStatus to) {
        rule.check(from, to);
        return to;
    }

    /** One transaction per call. A duplicate key throws DuplicateOrder; the first row stays. */
    @Transactional
    public void register(UUID id, String businessKey, String title) {
        String key = new OrderId(businessKey).value(); // rejects null / blank
        orders.insert(id, key, OrderStatus.CART, title);
        notifier.registered(key);
    }

    /**
     * ONE transaction, two INSERTs with the same key. The second fails with DuplicateOrder
     * (unchecked, not swallowed) -> everything is rolled back, count is 0.
     */
    @Transactional
    public void insertTwice(String businessKey) {
        String key = new OrderId(businessKey).value();
        orders.insert(UUID.randomUUID(), key, OrderStatus.CART, "First attempt");
        orders.insert(UUID.randomUUID(), key, OrderStatus.CART, "Second attempt");
    }
}
