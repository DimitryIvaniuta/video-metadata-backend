package com.github.dimitryivaniuta.videometadata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Binds Redis connection and Lettuce client settings from application.yml.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisPropertiesExtended {

    /**
     * Password for Redis (if set).
     */
    private String password;

    /**
     * Redis database index.
     */
    private int database = 0;

    /**
     * Hostname of the Redis server.
     */
    private String host = "localhost";

    /**
     * Socket connection timeout.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * Port of the Redis server.
     */
    private int port = 6379;

    /**
     * Timeout for Redis commands.
     */
    private Duration timeout = Duration.ofSeconds(60);
}
