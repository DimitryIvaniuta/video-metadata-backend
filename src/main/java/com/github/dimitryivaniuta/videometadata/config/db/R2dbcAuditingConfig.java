package com.github.dimitryivaniuta.videometadata.config.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

/**
 * Enables Spring Data R2DBC auditing and provides a reactive auditor that
 * resolves the current username from the Spring Security context.
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcAuditingConfig {

    /**
     * Supplies the current auditor (username) for auditing fields like {@code createdBy} / {@code lastModifiedBy}.
     * Falls back to {@code "system"} when no authenticated principal is present.
     *
     * @return reactive auditor supplier
     */
    @Bean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .defaultIfEmpty("system");
    }
}
