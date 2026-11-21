package org.example.handler;

import org.example.domain.InvalidTransition;
import org.example.domain.Order;
import org.example.domain.OrderId;
import org.example.domain.OrderNotFound;
import org.example.domain.OrderNotifier;
import org.example.domain.OrderStatus;
import org.example.domain.Rule;
import org.example.persistence.OrderJpa;
import org.example.security.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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
     * The locked action: CART -> PAID. The URL rule already stops a CLERK; @PreAuthorize repeats the
     * same role here, so the rule still holds if another caller ever reaches this method.
     * The policy decides whether the move is allowed. If not, the row stays as it is.
     *
     * @param actor the authenticated user name (Authentication.getName()), never taken from JSON
     */
    @PreAuthorize("hasRole('" + Roles.MANAGER + "')")
    @Transactional
    public Order pay(UUID id, String actor) {
        Order order = orders.find(id).orElseThrow(() -> new OrderNotFound(id));

        OrderStatus next;
        try {
            next = move(order.status(), OrderStatus.PAID);
        } catch (IllegalStateException ex) {
            // The Lab 2 rules throw IllegalStateException. A named domain error is easier to map to HTTP.
            throw new InvalidTransition(ex.getMessage());
        }

        orders.updateStatus(id, next);
        log.info("order {} moved {} -> {} by {}", order.businessKey(), order.status(), next, actor);
        return new Order(order.id(), order.businessKey(), next, order.title());
    }}
