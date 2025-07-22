package com.github.dimitryivaniuta.videometadata.ratelimit;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive token-bucket style limiter.
 */
public interface ReactiveRateLimiter {

    /**
     * Try to consume {@code tokens} from the bucket for {@code key}.
     *
     * @param key            unique bucket key
     * @param tokens         tokens to consume
     * @param capacity       max tokens held
     * @param refillTokens   tokens refilled every {@code refillPeriod}
     * @param refillPeriod   period between refills
     * @return Mono with result (allowed + metadata)
     */
    Mono<RateLimitResult> tryConsume(String key,
                                     int tokens,
                                     int capacity,
                                     int refillTokens,
                                     Duration refillPeriod);
}
