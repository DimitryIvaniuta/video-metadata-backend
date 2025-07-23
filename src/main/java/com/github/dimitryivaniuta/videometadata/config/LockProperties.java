package com.github.dimitryivaniuta.videometadata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Redis‐based distributed lock settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "lock.redis")
public class LockProperties {
    private String prefix = "lock";
    private Duration ttl = Duration.ofSeconds(30);
    private Duration extendBeforeExpiry = Duration.ofSeconds(5);
}
