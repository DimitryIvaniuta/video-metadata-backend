package com.github.dimitryivaniuta.videometadata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Import job throttling & retry configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "imports")
@Getter
@Setter
public class ImportThrottleProperties {

    /**
     * Maximum concurrent running import jobs.
     */
    private int maxConcurrent = 2;

    /**
     * Maximum queued jobs awaiting execution (0 = no queue, reject immediately).
     */
    private int maxQueued = 0;

    /**
     * Sliding window size for request counting.
     */
    private Duration rateLimitWindow = Duration.ofSeconds(60);

    /**
     * Maximum accepted job starts inside the window.
     */
    private int rateLimitMaxRequests = 20;

    /**
     * Reject if identical parameter job is already in-flight (true) or join (false).
     */
    private boolean duplicateJobReject = true;

    /**
     * Service-level retry settings (on top of provider-level retries).
     */
    private ServiceRetry serviceRetry = new ServiceRetry();

    @Getter
    @Setter
    public static class ServiceRetry {
        private boolean enabled = true;
        private int maxAttempts = 2;
        private Duration backoff = Duration.ofMillis(800);
    }
}
