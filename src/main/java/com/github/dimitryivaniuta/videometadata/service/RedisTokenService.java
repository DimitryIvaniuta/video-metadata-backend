package com.github.dimitryivaniuta.videometadata.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Persists and revokes JWT IDs (JTIs) in Redis to support token revocation.
 */
@Service
public class RedisTokenService {

    private static final String PREFIX = "jwt:jti:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisTokenService(final ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Stores a JTI in Redis with a TTL equal to the token lifespan.
     *
     * @param jti the JWT ID to store
     * @param ttl time‑to‑live for this token
     * @return Mono emitting true if stored successfully
     */
    public Mono<Boolean> storeToken(final String jti, final Duration ttl) {
        return redisTemplate
                .opsForValue()
                .set(PREFIX + jti, "valid", ttl);
    }

    /**
     * Checks whether a given JTI is still present (i.e. not revoked/expired).
     *
     * @param jti the JWT ID to check
     * @return Mono emitting true if the token is valid
     */
    public Mono<Boolean> isTokenValid(final String jti) {
        return redisTemplate
                .opsForValue()
                .get(PREFIX + jti)
                .map(val -> true)
                .defaultIfEmpty(false);
    }

    /**
     * Revokes a token by deleting its JTI from Redis.
     *
     * @param jti the JWT ID to delete
     * @return Mono emitting true if the key was deleted
     */
    public Mono<Boolean> revokeToken(final String jti) {
        return redisTemplate
                .opsForValue()
                .delete(PREFIX + jti);
    }
}
