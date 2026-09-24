package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.example.domain.Rule;
import org.example.domain.TransitionRule;

@Configuration
public class Config {
    @Bean
    public Rule rule() {
        return new TransitionRule();
    }
}