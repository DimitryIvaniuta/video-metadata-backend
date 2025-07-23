package com.github.dimitryivaniuta.videometadata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables @PreAuthorize / @PostAuthorize on both
 * reactive and non-reactive beans.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    // Default setup is sufficient for JWT roles checks.
}
