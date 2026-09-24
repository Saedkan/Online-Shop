package org.example.config;

import org.example.domain.ForbiddenRule;
import org.example.domain.Rule;
import org.example.domain.RuleChain;
import org.example.domain.TransitionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Config {

    @Bean
    public Rule transitionRule() {
        return new TransitionRule();
    }

    @Bean
    public Rule forbiddenRule() {
        return new ForbiddenRule();
    }

    @Bean
    public Rule rule(Rule transitionRule, Rule forbiddenRule) {
        return new RuleChain(
                List.of(transitionRule, forbiddenRule)
        );
    }
}