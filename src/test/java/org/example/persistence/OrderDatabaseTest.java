package org.example.persistence;

import org.example.config.Application;
import org.example.domain.DuplicateOrder;
import org.example.domain.OrderStatus;
import org.example.handler.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test: full Spring context + real PostgreSQL.
 * Needs a running PostgreSQL (docker compose up -d).
 * Flyway creates the table on startup from db/migration.
 * NO @Transactional on this class or its methods: every assertion must see
 * what production would really have committed.
 */
@SpringBootTest(classes = Application.class)
class OrderDatabaseTest {

    @Autowired
    private OrderService service;

    @Autowired
    private OrderJpa orders;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE shop_order");
    }

    @Nested
    class Transactions {

        @Test
        void secondStatementRollsBack() {
            assertThrows(DuplicateOrder.class,
                    () -> service.insertTwice("ORD-1042"));

            assertThat(orders.count("ORD-1042")).isZero();
        }

        @Test
        void secondRequestKeepsTheFirst() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();

            service.register(id1, "ORD-1042", "Laptop");
            assertThrows(DuplicateOrder.class,
                    () -> service.register(id2, "ORD-1042", "Again"));

            assertThat(orders.count("ORD-1042")).isEqualTo(1);
        }
    }

    @Nested
    class Persistence {

        @Test
        void statusIsStoredAndReadBack() {
            service.register(UUID.randomUUID(), "ORD-7", "Phone");

            assertThat(orders.findStatus("ORD-7")).contains(OrderStatus.CART);
            assertThat(orders.findStatus("ORD-404")).isEmpty();
        }
    }

    @Nested
    class Constraints {

        @Test
        void checkConstraintRejectsUnknownStatus() {
            assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                    "INSERT INTO shop_order (id, business_key, status, title) VALUES (?, ?, ?, ?)",
                    UUID.randomUUID(), "ORD-8", "LOST", "Bad status"));
        }

        @Test
        void nullBusinessKeyIsRejectedByNotNull() {
            assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                    "INSERT INTO shop_order (id, business_key, status, title) VALUES (?, ?, ?, ?)",
                    UUID.randomUUID(), null, "CART", "No key"));
        }
    }
}
