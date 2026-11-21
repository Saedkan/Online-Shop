package org.example.handler;

import org.example.domain.*;
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

    @Transactional
    public Order register(UUID id, String businessKey, String title) {
        String key = new OrderId(businessKey).value();
        orders.insert(id, key, OrderStatus.CART, title);
        notifier.registered(key);
        return new Order(id, key, OrderStatus.CART, title);
    }

    @Transactional(readOnly = true)
    public Order get(UUID id) {
        return orders.find(id).orElseThrow(() -> new OrderNotFound(id));
    }

    @Transactional
    public Order update(UUID id, String title) {
        Order current = orders.find(id).orElseThrow(() -> new OrderNotFound(id));
        requireCart(current, "updated");
        return orders.updateTitle(id, title);
    }

    @Transactional
    public void delete(UUID id) {
        Order current = orders.find(id).orElseThrow(() -> new OrderNotFound(id));
        requireCart(current, "deleted");
        orders.delete(id);
        log.info("order {} deleted ({})", id, current.businessKey());
    }

    @PreAuthorize("hasRole('" + Roles.MANAGER + "')")
    @Transactional
    public Order pay(UUID id, String actor) {
        Order order = orders.find(id).orElseThrow(() -> new OrderNotFound(id));

        // 1. Проверяем бизнес-правило перехода (CART -> PAID)
        rule.check(order.status(), OrderStatus.PAID);

        // 2. Обновляем статус в базе (метод void)
        orders.updateStatus(id, OrderStatus.PAID);

        // 3. Логируем действие для аудита
        log.info("order {} paid by {}", order.businessKey(), actor);

        // 4. Возвращаем обновлённый Order (собираем вручную)
        return new Order(
                order.id(),
                order.businessKey(),
                OrderStatus.PAID,
                order.title()
        );
    }

    private static void requireCart(Order order, String action) {
        if (order.status() != OrderStatus.CART) {
            // Возвращаем IllegalStateException, чтобы тесты прошли
            throw new IllegalStateException(
                    "only CART orders can be " + action + "; current status is " + order.status()
            );
        }
    }

    @Transactional
    public void insertTwice(String businessKey) {
        String key = new OrderId(businessKey).value();
        orders.insert(UUID.randomUUID(), key, OrderStatus.CART, "First attempt");
        orders.insert(UUID.randomUUID(), key, OrderStatus.CART, "Second attempt");
    }
}