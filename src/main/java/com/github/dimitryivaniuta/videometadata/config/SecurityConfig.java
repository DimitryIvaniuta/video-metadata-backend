package com.github.dimitryivaniuta.videometadata.config;

import com.github.dimitryivaniuta.videometadata.security.JwtAuthorizationFilter;
import com.github.dimitryivaniuta.videometadata.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configures application security: JWT login endpoint open, all /api/**
 * protected by JwtAuthorizationFilter, BCrypt password encoding, and
 * reactive authentication management.
 */
@Configuration
@EnableWebFluxSecurity
//@EnableMethodSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(
            final JwtAuthorizationFilter jwtAuthorizationFilter,
            final UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.userDetailsService     = userDetailsService;
    }

    /**
     * Defines the reactive security filter chain.
     *
     * @param http the ServerHttpSecurity to configure
     * @return the built SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                                // **Allow POST /auth/login** (handler path)
                                .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                // Swagger UI
                                .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                                // Everything else requires authentication
                                .anyExchange().authenticated()
                )
                .addFilterAt(
                        jwtAuthorizationFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }

    /**
     * Reactive authentication manager using the custom UserDetailsService and BCrypt.
     *
     * @return the ReactiveAuthenticationManager bean
     */
    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager mgr =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        mgr.setPasswordEncoder(passwordEncoder());
        return mgr;
    }

    /**
     * Password encoder bean using BCrypt for secure password hashing.
     *
     * @return the PasswordEncoder
     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // use the logging decorator so we can see raw vs. hash
        return new LoggingPasswordEncoder();
    }
}
