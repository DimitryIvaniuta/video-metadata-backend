package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service exposing synchronous & reactive operations on {@link Video}.
 * Synchronous methods are used within transactional boundaries; reactive
 * wrappers offload blocking JPA access to bounded elastic threads.
 */
public interface VideoService {

    // ----------- Synchronous (blocking) API -----------

    /**
     * Saves a video (create or update).
     * @param video entity
     * @return persisted entity
     */
    Video save(Video video);

    /**
     * Finds a video by id.
     * @param id primary key
     * @return optional video
     */
    Optional<Video> findById(Long id);

    /**
     * Deletes a video by id (idempotent).
     * @param id video id
     */
    void deleteById(Long id);

    /**
     * Queries videos with optional filters.
     *
     * @param provider optional provider filter
     * @param from optional upload date lower bound (inclusive)
     * @param to optional upload date upper bound (exclusive)
     * @param minDurationMillis optional minimum duration in ms
     * @param maxDurationMillis optional maximum duration in ms
     * @param category optional category
     * @param pageable pagination + sorting
     * @return page of videos
     */
    Page<Video> search(VideoProvider provider,
                       ZonedDateTime from,
                       ZonedDateTime to,
                       Long minDurationMillis,
                       Long maxDurationMillis,
                       VideoCategory category,
                       Pageable pageable);

    /**
     * Statistics: total videos per provider.
     * @return map provider -> count
     */
    Map<VideoProvider, Long> countPerProvider();

    /**
     * Statistics: average duration (ms) per provider.
     * @return map provider -> average duration ms
     */
    Map<VideoProvider, Long> averageDurationPerProvider();

    /**
     * Existence check by provider + external ID.
     * @param provider provider
     * @param externalVideoId unique id from provider
     * @return true if a video already exists
     */
    boolean existsByProviderAndExternalId(VideoProvider provider, String externalVideoId);

    // ----------- Reactive wrappers -----------

    Mono<Video> saveMono(Video video);

    Mono<Video> findByIdMono(Long id);

    Mono<Void> deleteByIdMono(Long id);

    Flux<Video> searchFlux(VideoProvider provider,
                           ZonedDateTime from,
                           ZonedDateTime to,
                           Long minDurationMillis,
                           Long maxDurationMillis,
                           VideoCategory category,
                           Pageable pageable);

    Mono<Map<VideoProvider, Long>> countPerProviderMono();

    Mono<Map<VideoProvider, Long>> averageDurationPerProviderMono();

    Mono<Boolean> existsByProviderAndExternalIdMono(VideoProvider provider, String externalVideoId);
}
