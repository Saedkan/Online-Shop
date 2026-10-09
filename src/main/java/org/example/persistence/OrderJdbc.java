package org.example.persistence;

import org.example.domain.DuplicateOrder;
import org.example.domain.OrderStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderJdbc {

    private final JdbcTemplate jdbc;

    public OrderJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Throws DuplicateOrder (unchecked) when business_key already exists. */
    public void insert(UUID id, String businessKey, OrderStatus status, String title) {
        try {
            jdbc.update("""
                    INSERT INTO shop_order (id, business_key, status, title)
                    VALUES (?, ?, ?, ?)
                    """, id, businessKey, status.name(), title);
        } catch (DuplicateKeyException ex) {
            // DuplicateKeyException = SQLState 23505 (subclass of DataIntegrityViolationException).
            // A CHECK violation is a different error and must not look like a duplicate.
            throw new DuplicateOrder(businessKey);
        }
    }

    public int count(String businessKey) {
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM shop_order WHERE business_key = ?",
                Integer.class,
                businessKey);
        return n == null ? 0 : n;
    }

    /** Text column -> enum. Unknown text throws, it never becomes a new status. */
    public Optional<OrderStatus> findStatus(String businessKey) {
        List<String> rows = jdbc.queryForList(
                "SELECT status FROM shop_order WHERE business_key = ?",
                String.class,
                businessKey);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        String text = rows.get(0);
        try {
            return Optional.of(OrderStatus.valueOf(text));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unknown status in database: " + text, ex);
        }
    }
}
