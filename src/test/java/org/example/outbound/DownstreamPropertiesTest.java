package org.example.outbound;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves app.downstream is bound from the dev profile yaml, not hardcoded in Java.
 * Does not open a socket to port 9090 and does not need PostgreSQL.
 */
@SpringBootTest(classes = DownstreamPropertiesTest.TestApp.class)
@ActiveProfiles("dev")
class DownstreamPropertiesTest {

    @Autowired
    private DownstreamProperties downstream;

    @Test
    void bindsDevBaseUrlAndTimeout() {
        assertThat(downstream.baseUrl()).isEqualTo("http://localhost:9090");
        assertThat(downstream.timeout()).isEqualTo(Duration.ofSeconds(2));
    }

    @SpringBootApplication(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @EnableConfigurationProperties(DownstreamProperties.class)
    static class TestApp {
    }
}
