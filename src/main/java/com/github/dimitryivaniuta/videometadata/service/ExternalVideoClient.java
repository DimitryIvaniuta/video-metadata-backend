package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstraction for fetching video metadata from an external provider.
 * Implementations must handle provider‑specific API calls, error mapping,
 * timeouts, and retries, then normalize to the common {@link Video} entity.
 */
public interface ExternalVideoClient {
    /**
     * Fetches metadata for the given external video ID.
     * <p>
     * The returned {@link Mono} should complete with:
     * <ul>
     *   <li>a fully populated {@link Video} if the ID is found and mapping succeeds, or</li>
     *   <li>an error signal (e.g. {@link RuntimeException}) if the video is not found,
     *       the API returns an error status, or a mapping failure occurs.</li>
     * </ul>
     *
     * @param externalId the provider‑specific ID of the video to fetch
     * @return a {@link Mono} emitting the fetched {@link Video} or an error
     */
    Mono<Video> fetchVideoMetadata(String externalId);
}
