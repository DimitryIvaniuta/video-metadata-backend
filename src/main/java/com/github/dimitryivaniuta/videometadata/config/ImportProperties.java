package com.github.dimitryivaniuta.videometadata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration properties for video import and throttling behavior.
 * Concurrency, retry, throttle, per‑user rate limits, and duplicate suppression.
 * <p>
 * Binds properties under <code>app.import</code>.
 */
@Configuration
@ConfigurationProperties(prefix = "app.import")
@Getter
@Setter
public class ImportProperties {

    /**
     * Number of parallel imports to dispatch concurrently.
     */
    private int concurrencyLimit = 4;

    /**
     * Number of retry attempts on external‐API fetch failures.
     */
    private int maxRetries = 3;

    /**
     * Backoff duration between retries.
     */
    private Duration backoffSeconds = Duration.ofSeconds(1);

    /**
     * Throttling settings to control submission rate and queue sizing.
     */
    private Throttle throttle = new Throttle();

    /** Per‑user rate‑limit settings. */
    private PerUser perUser = new PerUser();

    @Getter
    @Setter
    public static class Throttle {

        /**
         * Maximum number of import jobs running at the same time.
         */
        private int maxConcurrent = 2;

        /**
         * Maximum number of jobs allowed to queue before reject.
         * Zero means no queuing—excess jobs are rejected immediately.
         */
        private int maxQueued = 0;

        /**
         * Time window over which to count import submissions.
         */
        private Duration rateLimitWindow = Duration.ofSeconds(60);

        /**
         * Maximum number of import job starts permitted within the rate‐limit window.
         */
        private int rateLimitMaxRequests = 20;

        /**
         * If true, duplicate submissions for the same external ID within
         * the window are rejected; if false, they will join the existing job.
         */
        private boolean duplicateJobReject = true;

        /**
         * Additional service‐level retry settings, applied after provider‐level retries.
         */
        private ServiceRetry serviceRetry = new ServiceRetry();
    }

    @Getter
    @Setter
    public static class ServiceRetry {

        /**
         * Enable or disable service‐level retry behavior.
         */
        private boolean enabled = true;

        /**
         * Maximum retry attempts at the service layer.
         */
        private int maxAttempts = 2;

        /**
         * Backoff duration between service‐level retry attempts.
         */
        private Duration backoff = Duration.ofMillis(800);
    }

    @Getter @Setter
    public static class PerUser {
        /**
         * Max import submissions allowed per user in the window.
         */
        private int rateLimitPerUser = 5;

        /**
         * Sliding window for counting per‑user submissions.
         */
        private Duration window = Duration.ofSeconds(60);

        /**
         * TTL for “duplicate job” markers per (user,externalId), in ms.
         */
        private long duplicateCacheTtlMs = 600_000L;
    }
}
