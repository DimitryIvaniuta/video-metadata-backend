package com.github.dimitryivaniuta.videometadata.rate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Reactive distributed token bucket limiter (same semantics as imperative version).
 * Designed for use inside reactive request flows without blocking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveRedisRateLimiter {

    /**
     * Lua script (same logic as blocking variant), returns 1 or 0.
     */
    private static final String LUA_TOKEN_BUCKET = """
        local key             = KEYS[1]
        local capacity        = tonumber(ARGV[1])
        local intervalMillis  = tonumber(ARGV[2])
        local refillTokens    = tonumber(ARGV[3])
        local nowMillis       = tonumber(ARGV[4])
        local requested       = tonumber(ARGV[5])
        local ttlSeconds      = tonumber(ARGV[6])

        local data = redis.call('HMGET', key, 'tokens', 'ts')
        local tokens = tonumber(data[1])
        local lastTs = tonumber(data[2])

        if tokens == nil then
            tokens = capacity
            lastTs = nowMillis
        end

        local elapsed = nowMillis - lastTs
        if elapsed > 0 and elapsed >= intervalMillis then
            local intervals = math.floor(elapsed / intervalMillis)
            local add = intervals * refillTokens
            tokens = math.min(capacity, tokens + add)
            lastTs = lastTs + (intervals * intervalMillis)
        end

        local allowed = 0
        if tokens >= requested then
            tokens = tokens - requested
            allowed = 1
        end

        redis.call('HMSET', key, 'tokens', tokens, 'ts', lastTs)
        if ttlSeconds > 0 then
            redis.call('EXPIRE', key, ttlSeconds)
        end
        return allowed
        """;

    // Alphabetically sorted fields
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Consume from global bucket (example defaults).
     */
    public Mono<Boolean> tryConsumeGlobal(int tokens) {
        return tryConsume("import:global", tokens,
                100, Duration.ofSeconds(1), 100, Duration.ofMinutes(10));
    }

    /**
     * Consume from per-user bucket.
     */
    public Mono<Boolean> tryConsumeUser(String userId, int tokens) {
        return tryConsume("import:user:" + userId, tokens,
                20, Duration.ofSeconds(1), 20, Duration.ofMinutes(30));
    }

    /**
     * Generic bucket consumption.
     */
    public Mono<Boolean> tryConsume(@NonNull String key,
                                    int tokens,
                                    int capacity,
                                    @NonNull Duration refillInterval,
                                    int refillAmount,
                                    Duration ttl) {

        if (tokens <= 0 || capacity <= 0 || refillAmount <= 0) {
            return Mono.error(new IllegalArgumentException("Invalid bucket parameters"));
        }
        long ttlSeconds = ttl != null ? ttl.toSeconds() : 0;

        return serverTimeMillis()
                .flatMap(now -> evalTokenBucket(
                        key,
                        capacity,
                        refillInterval.toMillis(),
                        refillAmount,
                        now,
                        tokens,
                        ttlSeconds
                ))
                .map(r -> r == 1L);
    }

    private Mono<Long> evalTokenBucket(String key,
                                       long capacity,
                                       long intervalMillis,
                                       long refillTokens,
                                       long nowMillis,
                                       long requested,
                                       long ttlSeconds) {

        var bytesScript = LUA_TOKEN_BUCKET.getBytes(StandardCharsets.UTF_8);
        var keys = List.of(key.getBytes(StandardCharsets.UTF_8));
        var args = List.of(
                String.valueOf(capacity).getBytes(StandardCharsets.UTF_8),
                String.valueOf(intervalMillis).getBytes(StandardCharsets.UTF_8),
                String.valueOf(refillTokens).getBytes(StandardCharsets.UTF_8),
                String.valueOf(nowMillis).getBytes(StandardCharsets.UTF_8),
                String.valueOf(requested).getBytes(StandardCharsets.UTF_8),
                String.valueOf(ttlSeconds).getBytes(StandardCharsets.UTF_8)
        );

        return redisTemplate.execute(connection ->
                        connection.scriptingCommands()
                                .eval(bytesScript, ReturnType.INTEGER, keys, args))
                .single()
                .onErrorResume(ex -> {
                    log.error("RateLimiter script error: {}", ex.getMessage(), ex);
                    // Conservative fallback: deny (or allow -> choose policy)
                    return Mono.just(0L);
                });
    }

    /**
     * Acquire server time (milliseconds) reactively.
     */
    private Mono<Long> serverTimeMillis() {
        ReactiveRedisConnectionFactory cf = redisTemplate.getConnectionFactory();
        if (cf == null) {
            return Mono.error(new IllegalStateException("No RedisConnectionFactory"));
        }
        return cf.getReactiveConnection()
                .serverCommands()
                .time()
                .map(time -> (time.getFirst() * 1000L) + (time.getSecond() / 1000L));
    }

    /**
     * Return type mapping for scripting (Redis integer).
     */
    private enum ReturnType {
        INTEGER
    }
}
