package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ExternalVideoMetadata;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Reactive API for managing video metadata entities.
 * <p>
 * All methods are non‑blocking and return Reactor types.
 */
public interface VideoService {

    /**
     * Creates and persists a new {@link Video} for the given owner and external reference.
     * <p>
     * Fetches metadata from the external provider then saves the entity.
     *
     * @param provider      external video provider (e.g. YOUTUBE)
     * @param externalId  provider‑specific video identifier
     * @return Mono emitting the saved {@link Video}
     */
    Mono<Video> createVideo(VideoProvider provider, String externalId);

    /**
     * Retrieves a video by its internal ID.
     *
     * @param id video ID
     * @return Mono emitting the {@link Video} or empty if not found
     */
    Mono<Video> getById(Long id);

    /**
     * Lists all videos owned by the given user.
     *
     * @param ownerId owner user ID
     * @return Flux emitting all videos for that owner
     */
    Flux<Video> getByOwner(Long ownerId);

    /**
     * Refreshes metadata for an existing video from the external provider and updates the stored entity.
     *
     * @param videoId internal video ID
     * @return Mono emitting the updated {@link Video}
     */
    Mono<Video> updateMetadata(Long videoId);

    /**
     * Deletes a video by its ID.
     *
     * @param id video ID
     * @return Mono signaling completion
     */
    Mono<Void> delete(Long id);

    /**
     * Searches videos by title substring (case‑insensitive).
     *
     * @param fragment title fragment
     * @param offset   zero‑based row offset
     * @param limit    maximum rows to return
     * @return Flux emitting matching videos
     */
    Flux<Video> searchByTitle(String fragment, int offset, int limit);

    /**
     * Bulk fetch metadata for multiple external video references without persisting.
     *
     * @param requests list of (source, externalId) pairs
     * @return Flux emitting fetched {@link ExternalVideoMetadata}
     */
    Flux<ExternalVideoMetadata> fetchExternalMetadata(List<VideoRequest> requests);

    /**
     * Reactive wrapper for {@link #averageDurationPerProvider()}.
     *
     * @return a {@link Mono} emitting the provider->average duration map
     */
    Mono<Map<VideoProvider, Long>> averageDurationPerProviderMono();

    /**
     * Reactive wrapper for {@link #existsByProviderAndExternalId(VideoProvider, String)}.
     *
     * @param provider        the video source
     * @param externalVideoId the external ID
     * @return a {@link Mono} emitting {@code true} if the video exists
     */
    Mono<Boolean> existsByProviderAndExternalIdMono(VideoProvider provider,
                                                    String externalVideoId);
}
