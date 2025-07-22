package com.github.dimitryivaniuta.videometadata.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Reactive Redis-backed token bucket limiter returning rich metadata.
 *
 * LUA returns: { allowed(1/0), remaining, resetEpochMillis }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveRedisRateLimiter implements ReactiveRateLimiter {

    private final ReactiveStringRedisTemplate redis;

    private static final String LUA = """
        local key               = KEYS[1]
        local capacity          = tonumber(ARGV[1])
        local refill_tokens     = tonumber(ARGV[2])
        local refill_period_ms  = tonumber(ARGV[3])
        local now_ms            = tonumber(ARGV[4])
        local requested         = tonumber(ARGV[5])
        
        local data    = redis.call('HMGET', key, 'tokens', 'ts')
        local tokens  = tonumber(data[1])
        local last_ts = tonumber(data[2])
        
        if tokens == nil or last_ts == nil then
            tokens  = capacity
            last_ts = now_ms
        end
        
        -- refill
        local elapsed = now_ms - last_ts
        if elapsed > 0 then
            local refill_count = math.floor(elapsed / refill_period_ms) * refill_tokens
            if refill_count > 0 then
                tokens  = math.min(capacity, tokens + refill_count)
                last_ts = now_ms
            end
        end
        
        local allowed = 0
        if tokens >= requested then
            tokens  = tokens - requested
            allowed = 1
        end
        
        -- persist
        redis.call('HMSET', key, 'tokens', tokens, 'ts', last_ts)
        -- keep key around for at least 2 refill periods
        redis.call('PEXPIRE', key, refill_period_ms * 2)
        
        -- compute resetAt
        local missing      = capacity - tokens
        local rate_per_ms  = refill_tokens / refill_period_ms
        local reset_in_ms
        if rate_per_ms == 0 then
            reset_in_ms = refill_period_ms
        else
            reset_in_ms = math.floor(missing / rate_per_ms)
        end
        
        return {allowed, tokens, now_ms + reset_in_ms}
        """;

    private static final RedisScript<List> SCRIPT = RedisScript.of(LUA, List.class);

    @Override
    public Mono<RateLimitResult> tryConsume(String key,
                                            int tokens,
                                            int capacity,
                                            int refillTokens,
                                            Duration refillPeriod) {

        Objects.requireNonNull(key, "key must not be null");
        if (tokens <= 0 || capacity <= 0 || refillTokens <= 0) {
            return Mono.error(new IllegalArgumentException("Invalid bucket parameters"));
        }

        long now = System.currentTimeMillis();
        long periodMs = refillPeriod.toMillis();

        return redis.execute(SCRIPT,
                        List.of(key),
                        String.valueOf(capacity),
                        String.valueOf(refillTokens),
                        String.valueOf(periodMs),
                        String.valueOf(now),
                        String.valueOf(tokens))
                .single()
                .map(vals -> {
                    @SuppressWarnings("unchecked")
                    List<Number> v = (List<Number>) vals;
                    boolean allowed = v.get(0).intValue() == 1;
                    long remaining  = v.get(1).longValue();
                    Instant resetAt = Instant.ofEpochMilli(v.get(2).longValue());
                    return new RateLimitResult(allowed, remaining, resetAt);
                })
                .onErrorResume(ex -> {
                    log.error("Rate limiter script failed: {}", ex.getMessage(), ex);
                    // fail-closed (deny) or fail-open? choose policy. Here: deny.
                    return Mono.just(new RateLimitResult(false, 0, Instant.now()));
                });
    }
}
