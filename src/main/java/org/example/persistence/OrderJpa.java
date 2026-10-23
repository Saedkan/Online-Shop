package org.example.persistence;

import jakarta.persistence.EntityManager;
import org.example.domain.DuplicateOrder;
import org.example.domain.OrderStatus;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter on top of JPA/Hibernate. Same methods as the old OrderJdbc,
 * so the service layer does not change shape.
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
        } catch (RuntimeException ex) {
            // flush() throws Hibernate's exception here. Spring wraps it as
            // DataIntegrityViolationException only after this method returns.
            if (isUniqueViolation(ex)) {
                throw new DuplicateOrder(businessKey);
            }
            throw ex;
        }
    }

    public int count(String businessKey) {
        return repository.countByBusinessKey(businessKey);
    }

    /** Enum column -> enum. Unknown text makes Hibernate throw, it never becomes a new status. */
    public Optional<OrderStatus> findStatus(String businessKey) {
        return repository.findByBusinessKey(businessKey).map(OrderEntity::getStatus);
    }

    private static boolean isUniqueViolation(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve
                    && UNIQUE_VIOLATION.equals(cve.getSQLState())) {
                return true;
            }
            if (t instanceof SQLException sql && UNIQUE_VIOLATION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
