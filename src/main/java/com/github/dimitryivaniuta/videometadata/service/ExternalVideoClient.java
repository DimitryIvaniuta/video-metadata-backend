package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import reactor.core.publisher.Flux;

/**
 * Abstraction for fetching video metadata from an external source.
 */
public interface ExternalVideoClient {
    /**
     * Fetch metadata, given an optional identifier (playlistId or userId).
     */
    Flux<Video> fetchVideos(String identifier);
}
