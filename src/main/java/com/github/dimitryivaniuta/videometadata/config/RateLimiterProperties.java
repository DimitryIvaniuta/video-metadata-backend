package com.github.dimitryivaniuta.videometadata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Redis‐based rate limiter settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter.redis")
public class RateLimiterProperties {
    private String prefix = "ratelimit";
    private int defaultCapacity = 100;
    private int refillTokens = 100;
    private Duration refillPeriod = Duration.ofSeconds(60);
}
