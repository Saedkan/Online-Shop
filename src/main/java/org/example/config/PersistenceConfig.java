package org.example.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * scanBasePackages on Application only finds components. Spring Data repositories and JPA
 * entities are scanned separately (from Application's own package by default), so they
 * are pointed at org.example.persistence here. Test slices skip @Configuration classes,
 * so a @WebMvcTest does not need a database.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.example.persistence")
@EntityScan(basePackages = "org.example.persistence")
public class PersistenceConfig {
}
