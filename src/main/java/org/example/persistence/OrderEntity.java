package org.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.example.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA mapping of the shop_order table. The schema is owned by Flyway
 * (db/migration), Hibernate only validates it (ddl-auto: validate).
 */
@Entity
@Table(name = "shop_order")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(name = "business_key", nullable = false, unique = true)
    private String businessKey;

    // Stored as text ('CART', 'PAID', ...), matches the CHECK constraint.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String title;

    // Filled by the database default now(); read-only for Hibernate.
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected OrderEntity() {
        // required by JPA
    }

    public OrderEntity(UUID id, String businessKey, OrderStatus status, String title) {
        this.id = id;
        this.businessKey = businessKey;
        this.status = status;
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
