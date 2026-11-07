package org.example.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Application lives in org.example.config, so scan the whole org.example tree
// (handler, persistence, outbound), not only org.example.config.
// JPA repositories and entities are registered in PersistenceConfig, not here:
// @EnableJpaRepositories on this class would break @WebMvcTest slice tests.
@SpringBootApplication(scanBasePackages = "org.example")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
