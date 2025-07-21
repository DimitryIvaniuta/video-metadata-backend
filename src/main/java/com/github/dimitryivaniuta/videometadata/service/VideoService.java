package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service API for video metadata operations, exposing both
 * blocking and reactive method variants.
 */
public interface VideoService {

    // ---------- Synchronous (blocking) API ----------

    /**
     * Persist a {@link Video} entity.
     *
     * @param video the entity to save
     * @return the saved {@link Video}
     */
    Video save(Video video);

    /**
     * Find a video by its ID.
     *
     * @param id the video ID
     * @return an {@link Optional} containing the {@link Video} if found
     */
    Optional<Video> findById(Long id);

    /**
     * Delete a video by its ID.
     *
     * @param id the video ID
     * @throws jakarta.persistence.EntityNotFoundException if not found
     */
    void deleteById(Long id);

    /**
     * Search for videos matching optional criteria, paginated.
     *
     * @param provider          optional {@link VideoProvider} filter
     * @param from              optional earliest upload timestamp
     * @param to                optional latest upload timestamp
     * @param minDurationMillis optional minimum duration in milliseconds
     * @param maxDurationMillis optional maximum duration in milliseconds
     * @param category          optional {@link VideoCategory} filter
     * @param pageable          pagination and sort parameters
     * @return a {@link Page} of matching {@link Video} entities
     */
    Page<Video> search(VideoProvider provider,
                       ZonedDateTime from,
                       ZonedDateTime to,
                       Long minDurationMillis,
                       Long maxDurationMillis,
                       VideoCategory category,
                       Pageable pageable);

    /**
     * Count total videos per provider.
     *
     * @return a map from {@link VideoProvider} to count
     */
    Map<VideoProvider, Long> countPerProvider();

    /**
     * Compute average video duration (ms) per provider.
     *
     * @return a map from {@link VideoProvider} to average duration in ms
     */
    Map<VideoProvider, Long> averageDurationPerProvider();

    /**
     * Check existence of a video by provider and external ID.
     *
     * @param provider        the video source
     * @param externalVideoId the external ID
     * @return {@code true} if such a video record exists
     */
    boolean existsByProviderAndExternalId(VideoProvider provider,
                                          String externalVideoId);


    // ---------- Reactive (non‐blocking) API ----------

    /**
     * Reactive wrapper for {@link #save(Video)}.
     *
     * @param video the video to save
     * @return a {@link Mono} emitting the saved {@link Video}
     */
    Mono<Video> saveMono(Video video);

    /**
     * Reactive wrapper for {@link #findById(Long)}.
     *
     * @param id the video ID
     * @return a {@link Mono} emitting the found {@link Video}, or error if not found
     */
    Mono<Video> findByIdMono(Long id);

    /**
     * Reactive wrapper for {@link #deleteById(Long)}.
     *
     * @param id the video ID
     * @return a {@link Mono} signaling completion
     */
    Mono<Void> deleteByIdMono(Long id);

    /**
     * Reactive wrapper for {@link #search(VideoProvider, ZonedDateTime, ZonedDateTime, Long, Long, VideoCategory, Pageable)}.
     *
     * @param provider          optional provider filter
     * @param from              optional earliest upload
     * @param to                optional latest upload
     * @param minDurationMillis optional minimum duration
     * @param maxDurationMillis optional maximum duration
     * @param category          optional category filter
     * @param pageable          pagination parameters
     * @return a {@link Flux} emitting the matching {@link Video} entities
     */
    Flux<Video> searchFlux(VideoProvider provider,
                           ZonedDateTime from,
                           ZonedDateTime to,
                           Long minDurationMillis,
                           Long maxDurationMillis,
                           VideoCategory category,
                           Pageable pageable);

    /**
     * Reactive wrapper for {@link #countPerProvider()}.
     *
     * @return a {@link Mono} emitting the provider→count map
     */
    Mono<Map<VideoProvider, Long>> countPerProviderMono();

    /**
     * Reactive wrapper for {@link #averageDurationPerProvider()}.
     *
     * @return a {@link Mono} emitting the provider→average duration map
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
