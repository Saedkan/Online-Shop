package org.example.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA repository: Spring generates the implementation. */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByBusinessKey(String businessKey);

    int countByBusinessKey(String businessKey);
}
