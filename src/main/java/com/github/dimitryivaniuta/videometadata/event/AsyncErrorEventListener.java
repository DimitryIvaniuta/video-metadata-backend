package com.github.dimitryivaniuta.videometadata.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Centralized listener for async errors. Could emit metrics or
 * forward to an alerting system (e.g., Sentry, Slack webhook).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncErrorEventListener {

    @EventListener
    public void onAsyncError(AsyncErrorEvent event) {
        log.error("Async method failure: method={} argsCount={} ex={}",
                event.getMethod().getName(),
                event.getArgs().length,
                event.getThrowable().toString(),
                event.getThrowable()
        );
        // (Optional) Increment Micrometer counter:
        // Metrics.counter("async.errors", "method", event.getMethod().getName()).increment();
    }
}

