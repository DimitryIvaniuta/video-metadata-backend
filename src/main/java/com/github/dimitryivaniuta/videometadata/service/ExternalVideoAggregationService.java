package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ExternalVideoMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Aggregates metadata from one or more external providers.
 */
public interface ExternalVideoAggregationService {

    /**
     * Fetches metadata for a single (provider, id) pair.
     */
    Mono<ExternalVideoMetadata> getOne(VideoProvider provider, String externalId);

    /**
     * Fetches metadata for many (provider, id) pairs concurrently.
     */
    Flux<ExternalVideoMetadata> getMany(List<VideoRequest> requests);

    /**
     * Simple pair struct for requests.
     */
    record VideoRequest(VideoProvider provider, String externalId) {}
}
