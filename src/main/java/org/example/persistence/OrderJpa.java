package org.example.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.example.domain.DuplicateOrder;
import org.example.domain.Order;
import org.example.domain.OrderStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter on top of JPA/Hibernate. The entity never leaves this class:
 * callers get the domain record Order.
 */
@Repository
public class OrderJpa {

    private static final String UNIQUE_VIOLATION = "23505";

    private final OrderJpaRepository repository;
    private final EntityManager em;

    public OrderJpa(OrderJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    /** Throws DuplicateOrder (unchecked) when business_key already exists. */
    public void insert(UUID id, String businessKey, OrderStatus status, String title) {
        try {
            // persist + flush: the INSERT must run now, not at commit,
            // otherwise the duplicate would escape this try/catch.
            em.persist(new OrderEntity(id, businessKey, status, title));
            em.flush();
        } catch (PersistenceException | DataIntegrityViolationException ex) {
            // Inside this class the exception is still Hibernate's own (a PersistenceException):
            // Spring converts it to DataIntegrityViolationException only after it leaves the
            // @Repository. Both are caught so it works either way.
            // Only a unique violation (SQLState 23505) is a duplicate.
            // A CHECK or NOT NULL violation must not look like one.
            if (isUniqueViolation(ex)) {
                throw new DuplicateOrder(businessKey);
            }
            throw ex;
        }
    }

    /** Read by the UUID primary key. Does not change anything. */
    public Optional<Order> find(UUID id) {
        return repository.findById(id).map(OrderJpa::toDomain);
    }

    public int count(String businessKey) {
        return repository.countByBusinessKey(businessKey);
    }

    /** Enum column -> enum. Unknown text makes Hibernate throw, it never becomes a new status. */
    public Optional<OrderStatus> findStatus(String businessKey) {
        return repository.findByBusinessKey(businessKey).map(OrderEntity::getStatus);
    }

    private static Order toDomain(OrderEntity e) {
        return new Order(e.getId(), e.getBusinessKey(), e.getStatus(), e.getTitle());
    }

    /** Walks the cause chain down to the JDBC driver's SQLException and checks its SQLState. */
    private static boolean isUniqueViolation(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && UNIQUE_VIOLATION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
