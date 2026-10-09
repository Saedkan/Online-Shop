package org.example.persistence;

import org.example.config.Application;
import org.example.domain.DuplicateOrder;
import org.example.domain.OrderStatus;
import org.example.handler.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Needs a running PostgreSQL (docker compose up -d).
 * NO @Transactional on this class or its methods: every assertion must see
 * what production would really have committed.
 */
@SpringBootTest(classes = Application.class)
class OrderDatabaseTest {

    @Autowired
    private OrderService service;

    @Autowired
    private OrderJdbc orders;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() throws Exception {
        jdbc.execute("DROP TABLE IF EXISTS shop_order");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/schema.sql")));
    }

    @Test
    void secondStatementRollsBack() {
        assertThrows(DuplicateOrder.class,
                () -> service.insertTwice("ORD-1042"));

        assertEquals(0, orders.count("ORD-1042"));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        service.register(id1, "ORD-1042", "Laptop");
        assertThrows(DuplicateOrder.class,
                () -> service.register(id2, "ORD-1042", "Again"));

        assertEquals(1, orders.count("ORD-1042"));
    }

    @Test
    void statusIsStoredAndReadBack() {
        service.register(UUID.randomUUID(), "ORD-7", "Phone");

        assertEquals(Optional.of(OrderStatus.CART), orders.findStatus("ORD-7"));
        assertEquals(Optional.empty(), orders.findStatus("ORD-404"));
    }

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
