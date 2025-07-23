package com.github.dimitryivaniuta.videometadata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT revocation token storage settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "token.redis")
public class TokenProperties {
    private String prefix = "jwt:revoked";
    private long ttlSeconds = 3600;
}
