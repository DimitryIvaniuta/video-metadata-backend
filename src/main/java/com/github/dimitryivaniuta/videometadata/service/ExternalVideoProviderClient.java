package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ExternalVideoMetadata;
import reactor.core.publisher.Mono;

/**
 * Reactive client for a single external video provider (YouTube, Vimeo, etc.).
 */
public interface ExternalVideoProviderClient {

    /**
     * Fetches full metadata for a video by its external ID.
     *
     * @param externalId provider-specific video id
     * @return mono with metadata or empty if not found
     */
    Mono<ExternalVideoMetadata> fetchById(String externalId);

    /**
     * @return which provider this client represents.
     */
    VideoProvider provider();
}