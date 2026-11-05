package org.example.handler;

import org.example.domain.Order;
import org.example.domain.OrderId;
import org.example.domain.OrderNotFound;
import org.example.domain.OrderNotifier;
import org.example.domain.OrderStatus;
import org.example.domain.Rule;
import org.example.persistence.OrderJpa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final Rule rule;
    private final OrderJpa orders;
    private final OrderNotifier notifier;

    public OrderService(Rule rule, OrderJpa orders, OrderNotifier notifier) {
        this.rule = rule;
        this.orders = orders;
        this.notifier = notifier;
    }

    public OrderStatus move(OrderStatus from, OrderStatus to) {
        rule.check(from, to);
        return to;
    }

    /**
     * One transaction per call. A duplicate key throws DuplicateOrder; the first row stays.
     * Returns the order that was stored, so the web layer can answer 201 with a body.
     */
    @Transactional
    public Order register(UUID id, String businessKey, String title) {
        String key = new OrderId(businessKey).value(); // rejects null / blank
        orders.insert(id, key, OrderStatus.CART, title);
        notifier.registered(key);
        return new Order(id, key, OrderStatus.CART, title);
    }

    /** Read only. A missing row throws OrderNotFound; it never registers or moves anything. */
    @Transactional(readOnly = true)
    public Order get(UUID id) {
        return orders.find(id).orElseThrow(() -> new OrderNotFound(id));
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
