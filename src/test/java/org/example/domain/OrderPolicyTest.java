package org.example.domain;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderPolicyTest {

    private final OrderPolicy policy = new OrderPolicy();

    @Nested
    class StatusTransitions {

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
                assertThrows(IllegalStateException.class,
                        () -> policy.move(id, fromStatus, toStatus));
            }
        }

        @Test
        void testInvalidTransitionThrowsException() {
            OrderId id = new OrderId("ORD-123");
            assertThrows(IllegalStateException.class,
                    () -> policy.move(id, OrderStatus.CART, OrderStatus.DELIVERED));
        }

        @Disabled("SHIPPED → DELIVERED is not in the lab transition table yet")
        @Test
        void shippedCanBecomeDelivered() {
            OrderId id = new OrderId("ORD-123");
            assertEquals(OrderStatus.DELIVERED,
                    policy.move(id, OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        }

        private boolean isAllowed(OrderStatus from, OrderStatus to) {
            return (from == OrderStatus.CART && to == OrderStatus.PAID) ||
                    (from == OrderStatus.PAID && to == OrderStatus.SHIPPED);
        }
    }

    @Nested
    class OrderIdValidation {

        @Test
        void testNullIdThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> policy.move(null, OrderStatus.CART, OrderStatus.PAID));
        }

        @Test
        void rejectsNullAndBlankIds() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new OrderId(null)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new OrderId("")),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new OrderId("   "))
            );
        }
    }
}
