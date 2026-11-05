package org.example.config;

import org.example.domain.ForbiddenRule;
import org.example.domain.Rule;
import org.example.domain.RuleChain;
import org.example.domain.TransitionRule;
import org.example.outbound.DownstreamProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@EnableConfigurationProperties(DownstreamProperties.class)
public class Config {

    @Bean
    public TransitionRule transitionRule() {
        return new TransitionRule();
    }

    @Bean
    public ForbiddenRule forbiddenRule() {
        return new ForbiddenRule();
    }

    // @Primary: three beans implement Rule, the service must get the chain.
    @Bean
    @Primary
    public Rule rule(TransitionRule transitionRule, ForbiddenRule forbiddenRule) {
        return new RuleChain(
                List.of(transitionRule, forbiddenRule)
        );
    }
}
