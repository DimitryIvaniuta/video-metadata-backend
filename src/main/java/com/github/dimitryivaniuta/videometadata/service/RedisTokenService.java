package com.github.dimitryivaniuta.videometadata.service;

import java.time.Duration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Stores and verifies JWT JTIs in Redis for revocation support.
 */
@Service
public class RedisTokenService {

    private final ReactiveRedisTemplate<String,String> redis;
    private static final String PREFIX = "jwt:jti:";

    public RedisTokenService(
            final ReactiveRedisTemplate<String,String> redis
    ) {
        this.redis = redis;
    }

    /** Saves a JTI with TTL; returns Mono<Boolean>. */
    public Mono<Boolean> storeToken(final String jti, final Duration ttl) {
        return redis.opsForValue().set(PREFIX + jti, "1", ttl);
    }

    /** Checks whether a JTI exists in Redis. */
    public Mono<Boolean> isTokenValid(final String jti) {
        return redis.opsForValue().get(PREFIX + jti)
                .map(v -> true)
                .defaultIfEmpty(false);
    }

    /** Deletes a JTI, revoking its token immediately. */
    public Mono<Long> revokeToken(final String jti) {
        return redis.opsForValue().delete(PREFIX + jti);
    }
}
