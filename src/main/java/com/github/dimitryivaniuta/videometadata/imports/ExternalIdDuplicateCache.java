package com.github.dimitryivaniuta.videometadata.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalIdDuplicateCache {

    private final StringRedisTemplate redis;
    private final ImportThrottleProperties props;

    public boolean seen(String provider, String externalId) {
        String key = key(provider, externalId);
        Boolean exists = redis.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Mark the pair as seen (idempotent).
     */
    public void mark(String provider, String externalId) {
        redis.opsForValue().setIfAbsent(
                key(provider, externalId),
                "1",
                props.getCache().getExternalIdTtl()
        );
    }

    private String key(String provider, String externalId) {
        return "import:seen:" + provider + ":" + externalId;
    }
}
