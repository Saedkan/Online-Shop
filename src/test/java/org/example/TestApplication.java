package org.example;

import org.springframework.boot.SpringBootConfiguration;

/**
 * Anchor for {@code @WebMvcTest} slices. The main {@code Application} lives under
 * {@code org.example.config}, which package search from handler tests does not always reach.
 */
@SpringBootConfiguration
public class TestApplication {
}
