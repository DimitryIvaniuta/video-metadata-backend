package com.github.dimitryivaniuta.videometadata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the {@code taskExecutor} thread pool
 * used by @Async methods.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "async")
public class AsyncProperties {

    /**
     * Core number of threads in the pool.
     */
    private int corePoolSize = 4;

    /**
     * Maximum allowed number of threads.
     */
    private int maxPoolSize = 16;

    /**
     * Capacity of the work queue before spawning new threads.
     */
    private int queueCapacity = 200;

    /**
     * Prefix applied to the names of threads in this pool.
     */
    private String threadNamePrefix = "AsyncExec-";

    /**
     * Seconds to wait for scheduled tasks to complete on shutdown.
     */
    private int awaitTerminationSeconds = 30;

    /**
     * Whether to wait for scheduled tasks to complete on shutdown.
     */
    private boolean waitForTasksOnShutdown = true;
}
