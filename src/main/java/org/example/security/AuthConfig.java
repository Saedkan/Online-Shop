package org.example.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Who may log in: the password encoder and the manager that checks name + hash. */
@Configuration
public class AuthConfig {

    /** Delegating encoder = BCrypt by default. Hashes look like {bcrypt}$2a$10$... */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Checks the user name and the BCrypt hash. Only the dev profile ships users (DevUsersConfig).
     * Without a user store (prod, this lab) nobody can log in, and the app still starts.
     */
    @Bean
    public AuthenticationManager authenticationManager(ObjectProvider<UserDetailsService> users,
                                                       PasswordEncoder passwordEncoder) {
        UserDetailsService userDetails = users.getIfAvailable(() -> username -> {
            throw new UsernameNotFoundException("no user store configured");
        });

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetails);
        provider.setPasswordEncoder(passwordEncoder);
        // unknown user and wrong password both surface as BadCredentialsException (the default)
        return new ProviderManager(provider);
    }
}
