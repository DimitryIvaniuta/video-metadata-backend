package com.github.dimitryivaniuta.videometadata.config;

import com.github.dimitryivaniuta.videometadata.event.AsyncErrorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Central configuration for all @Async work.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer, ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    /**
     * The executor on which all @Async methods will run.
     * Tweak pool sizes, queue capacity, and rejection policy here.
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

        // Core threads to keep alive
        exec.setCorePoolSize(4);
        // Max threads when queue is full
        exec.setMaxPoolSize(16);
        // Backlog queue size before spawning up to max
        exec.setQueueCapacity(100);
        // Name threads for easier debugging
        exec.setThreadNamePrefix("vm-async-");
        // If the pool is saturated, abort new tasks
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // Wait for tasks to finish on shutdown
        exec.setAwaitTerminationSeconds(30);
        exec.setWaitForTasksToCompleteOnShutdown(true);

        exec.initialize();
        return exec;
    }

    /**
     * Simple handler for exceptions thrown from @Async void methods.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, args) -> {
            // 1) log immediately
            log.error("Uncaught exception in @Async method {} with arguments {}",
                    method.getName(), args, throwable);

            // 2) publish an application event for further handling
            publisher.publishEvent(new AsyncErrorEvent(this, throwable, method, args));
        };
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

}
