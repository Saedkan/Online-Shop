package org.example.handler;

import org.example.domain.DuplicateOrder;
import org.example.domain.ForbiddenRule;
import org.example.domain.OrderNotifier;
import org.example.domain.OrderStatus;
import org.example.domain.Rule;
import org.example.domain.RuleChain;
import org.example.domain.TransitionRule;
import org.example.persistence.OrderJdbc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/** Plain unit test: no Spring, no database. The outbound port is a Mockito mock. */
class OrderServiceTest {

    private OrderJdbc orders;
    private OrderNotifier notifier;   // mocked outbound port
    private OrderService service;

    @BeforeEach
    void setUp() {
        Rule rule = new RuleChain(
                List.of(
                        new TransitionRule(),
                        new ForbiddenRule()
                )
        );
        orders = mock(OrderJdbc.class);
        notifier = mock(OrderNotifier.class);
        service = new OrderService(rule, orders, notifier);
    }

    @Test
    void cartCanBecomePaid() {
        assertEquals(OrderStatus.PAID,
                service.move(OrderStatus.CART, OrderStatus.PAID));
    }

    @Test
    void paidCanBecomeShipped() {
        assertEquals(OrderStatus.SHIPPED,
                service.move(OrderStatus.PAID, OrderStatus.SHIPPED));
    }

    @Test
    void cartCannotBecomeDelivered() {
        assertThrows(IllegalStateException.class,
                () -> service.move(OrderStatus.CART, OrderStatus.DELIVERED));
    }

    @Test
    void shippedCannotReturnToCart() {
        assertThrows(IllegalStateException.class,
                () -> service.move(OrderStatus.SHIPPED, OrderStatus.CART));
    }

    @Test
    void deliveredCannotBecomeReturnedDirectly() {
        assertThrows(IllegalStateException.class,
                () -> service.move(OrderStatus.DELIVERED, OrderStatus.RETURNED));
    }

    @Test
    void registerInsertsAndNotifiesThroughThePort() {
        UUID id = UUID.randomUUID();

        service.register(id, "ORD-19", "Laptop");

        verify(orders).insert(id, "ORD-19", OrderStatus.CART, "Laptop");
        verify(notifier).registered("ORD-19");
    }

    @Test
    void duplicateDoesNotNotify() {
        UUID id = UUID.randomUUID();
        doThrow(new DuplicateOrder("ORD-19"))
                .when(orders).insert(any(), any(), any(), any());

        assertThrows(DuplicateOrder.class,
                () -> service.register(id, "ORD-19", "Again"));

        verify(notifier, never()).registered(any());
    }

    @Test
    void blankBusinessKeyIsRejectedBeforeAnyIo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(UUID.randomUUID(), "  ", "Laptop"));

        verifyNoInteractions(orders, notifier);
    }
}
