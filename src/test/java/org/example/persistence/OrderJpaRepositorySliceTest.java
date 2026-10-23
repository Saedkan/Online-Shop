package org.example.persistence;

import org.example.config.Application;
import org.example.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test: JPA / Hibernate / repository only, not the full application.
 * Uses the real PostgreSQL from application.yml (no H2).
 * Needs {@code docker compose up -d}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = Application.class)
class OrderJpaRepositorySliceTest {

    @Autowired
    private OrderJpaRepository repository;

    @Test
    void savesAndFindsByBusinessKey() {
        UUID id = UUID.randomUUID();
        repository.saveAndFlush(new OrderEntity(id, "ORD-SLICE", OrderStatus.CART, "Slice"));

        assertThat(repository.findByBusinessKey("ORD-SLICE"))
                .isPresent()
                .get()
                .extracting(OrderEntity::getId, OrderEntity::getStatus, OrderEntity::getTitle)
                .containsExactly(id, OrderStatus.CART, "Slice");
        assertThat(repository.countByBusinessKey("ORD-SLICE")).isEqualTo(1);
        assertThat(repository.findByBusinessKey("ORD-MISSING")).isEmpty();
    }
}
