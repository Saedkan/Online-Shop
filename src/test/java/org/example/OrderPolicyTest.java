package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class OrderPolicyTest {

    private final OrderPolicy policy = new OrderPolicy();

    @ParameterizedTest
    @CsvSource({
            // legal
            "CART, PAID",
            "PAID, SHIPPED",

            // forbidden
            "DELIVERED, RETURNED",
            "SHIPPED, CART"
    })
    void testStatusTransition(String from, String to) {
        OrderId id = new OrderId("ORD-123");
        OrderStatus fromStatus = OrderStatus.valueOf(from);
        OrderStatus toStatus = OrderStatus.valueOf(to);

        if (isAllowed(fromStatus, toStatus)) {
            assertEquals(toStatus, policy.move(id, fromStatus, toStatus));
        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> policy.move(id, fromStatus, toStatus));
        }
    }

    private boolean isAllowed(OrderStatus from, OrderStatus to) {
        return (from == OrderStatus.CART && to == OrderStatus.PAID) ||
                (from == OrderStatus.PAID && to == OrderStatus.SHIPPED);
    }

    @Test
    void testNullIdThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.move(null, OrderStatus.CART, OrderStatus.PAID));
    }

    @Test
    void testBlankIdThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderId("   "));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderId(""));
    }

    @Test
    void testNullIdInConstructorThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderId(null));
    }

    @Test
    void testInvalidTransitionThrowsException() {
        OrderId id = new OrderId("ORD-123");
        assertThrows(IllegalArgumentException.class,
                () -> policy.move(id, OrderStatus.CART, OrderStatus.DELIVERED));
    }
}
