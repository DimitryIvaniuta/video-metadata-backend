package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.service.ExternalVideoAggregationService;
import com.github.dimitryivaniuta.videometadata.service.ExternalVideoProviderClient;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ExternalVideoMetadata;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ExternalVideoSource;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Default reactive implementation that fans out to provider clients, with optional resilience & caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalVideoAggregationServiceImpl implements ExternalVideoAggregationService {

    /** All registered provider clients, keyed by source enum. */
    private final Map<ExternalVideoSource, ExternalVideoProviderClient> clients;

    /**
     * Fetch one video metadata.
     */
    @Override
    @Cacheable(cacheNames = "extVideoMeta", key = "#source.name() + ':' + #externalId")
    @CircuitBreaker(name = "extVideo", fallbackMethod = "fallbackMono")
    @Retry(name = "extVideo")
    @RateLimiter(name = "extVideo")
    @Bulkhead(name = "extVideo")
    public Mono<ExternalVideoMetadata> getOne(ExternalVideoSource source, String externalId) {
        ExternalVideoProviderClient client = clients.get(source);
        if (client == null) {
            return Mono.error(new IllegalArgumentException("No client for source " + source));
        }
        return client.fetchById(externalId)
                .timeout(Duration.ofSeconds(5))
                .switchIfEmpty(Mono.error(new IllegalStateException("Video not found: " + source + ":" + externalId)));
    }

    /**
     * Fetch many concurrently. Uses same cache/resilience under the hood.
     */
    @Override
    public Flux<ExternalVideoMetadata> getMany(List<VideoRequest> requests) {
        return Flux.fromIterable(requests)
                .flatMap(req -> getOne(req.source(), req.externalId()));
    }

    /* ------------------- Resilience fallbacks ------------------- */

    private Mono<ExternalVideoMetadata> fallbackMono(ExternalVideoSource source, String externalId, Throwable ex) {
        log.warn("Fallback for {}:{} due to {}", source, externalId, ex.toString());
        return Mono.error(ex); // or return a stub
    }
}
