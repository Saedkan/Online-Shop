package org.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Laptop-only users. Passwords are BCrypt via the delegating encoder. No user table this lab.
 */
@Configuration
@Profile("dev")
public class DevUsersConfig {

    /** Shared classroom password for both roles. Shown on the auth page as dev-only. */
    public static final String DEV_PASSWORD = "css-lab5";

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails clerk = User.builder()
                .username("clerk-dev")
                .password(passwordEncoder.encode(DEV_PASSWORD))
                .roles("CLERK")
                .build();
        UserDetails manager = User.builder()
                .username("manager-dev")
                .password(passwordEncoder.encode(DEV_PASSWORD))
                .roles("MANAGER")
                .build();
        return new InMemoryUserDetailsManager(clerk, manager);
    }
}
