package com.github.dimitryivaniuta.videometadata.ratelimit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Wraps a {@link ReactiveRateLimiter} and records Micrometer metrics.
 */
@Component
@RequiredArgsConstructor
public class RateLimiterMetricsBinder implements InitializingBean {

    private final ReactiveRedisRateLimiter redisTemplate;
    private final MeterRegistry registry;

    private Counter allowedCounter;
    private Counter deniedCounter;

    @Override
    public void afterPropertiesSet() {
        this.allowedCounter = Counter.builder("ratelimiter.requests.allowed")
                .description("Allowed requests by rate limiter")
                .register(registry);
        this.deniedCounter = Counter.builder("ratelimiter.requests.denied")
                .description("Denied requests by rate limiter")
                .register(registry);
    }

    public Mono<RateLimitResult> tryConsume(String key, int tokens, int capacity, int refillTokens, Duration refillPeriod) {
        return redisTemplate.tryConsume(key, tokens, capacity, refillTokens, refillPeriod)
                .doOnNext(res -> {
                    if (res.allowed()) allowedCounter.increment();
                    else deniedCounter.increment();
                });
    }
}
