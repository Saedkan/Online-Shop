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
 * Two classroom users for the laptop. There is no user table in this lab.
 * The encoder hashes the passwords (BCrypt) when the application starts: no {noop}, no plain text stored.
 * User.roles("CLERK") stores the authority ROLE_CLERK.
 */
@Configuration
@Profile("dev")
public class DevUsersConfig {

    @Bean
    public UserDetailsService devUsers(PasswordEncoder encoder) {
        UserDetails clerk = User.withUsername("clerk")
                .password(encoder.encode("clerk-dev"))
                .roles(Roles.CLERK)
                .build();
        UserDetails manager = User.withUsername("manager")
                .password(encoder.encode("manager-dev"))
                .roles(Roles.MANAGER)
                .build();
        return new InMemoryUserDetailsManager(clerk, manager);
    }
}
