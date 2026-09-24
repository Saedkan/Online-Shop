package org.example.handler;

import org.example.domain.ForbiddenRule;
import org.example.domain.OrderStatus;
import org.example.domain.Rule;
import org.example.domain.RuleChain;
import org.example.domain.TransitionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        Rule rule = new RuleChain(
                List.of(
                        new TransitionRule(),
                        new ForbiddenRule()
                )
        );

        service = new OrderService(rule);
    }

    @Test
    void cartCanBecomePaid() {
        assertEquals(
                OrderStatus.PAID,
                service.move(
                        OrderStatus.CART,
                        OrderStatus.PAID
                )
        );
    }

    @Test
    void paidCanBecomeShipped() {
        assertEquals(
                OrderStatus.SHIPPED,
                service.move(
                        OrderStatus.PAID,
                        OrderStatus.SHIPPED
                )
        );
    }

    @Test
    void cartCannotBecomeDelivered() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.move(
                        OrderStatus.CART,
                        OrderStatus.DELIVERED
                )
        );
    }

    @Test
    void shippedCannotReturnToCart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.move(
                        OrderStatus.SHIPPED,
                        OrderStatus.CART
                )
        );
    }

    @Test
    void deliveredCannotBecomeReturnedDirectly() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.move(
                        OrderStatus.DELIVERED,
                        OrderStatus.RETURNED
                )
        );
    }
}