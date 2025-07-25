package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.domain.repository.projection.ProviderAvgDurationRow;
import com.github.dimitryivaniuta.videometadata.domain.repository.projection.ProviderCountRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive repository for {@link Video} entities.
 * <p>
 * Note: JPQL/JPA constructs are not supported in R2DBC. Use native SQL in {@code @Query}
 * or a separate custom repository with {@link org.springframework.data.r2dbc.core.R2dbcEntityTemplate}.
 */
@Repository
public interface VideoRepository extends R2dbcRepository<Video, Long>, VideoRepositoryCustom  {

    /**
     * Compute the average duration (ms) per provider.
     *
     * @return a {@link Flux} of rows containing provider + average duration
     */
    @Query("""
           SELECT provider, AVG(duration_ms) AS avg
           FROM videos
           GROUP BY provider
           """)
    Flux<ProviderAvgDurationRow> averageDurationByProvider();

    /**
     * Check if a given (provider, externalVideoId) pair already exists.
     *
     * @param provider        enum identifying the source
     * @param externalVideoId provider‑specific video ID
     * @return {@link Mono} emitting true if a matching video exists
     */
    Mono<Boolean> existsByProviderAndExternalVideoId(VideoProvider provider, String externalVideoId);


    /**
     * Count videos per provider.
     *
     * @return a {@link Flux} of rows containing provider + count
     */
    @Query("""
           SELECT provider, COUNT(*) AS cnt
           FROM videos
           GROUP BY provider
           """)
    Flux<ProviderCountRow> countByProvider();

    /**
     * Fetch all videos owned by a specific user.
     *
     * @param createdByUserId internal user ID (foreign key)
     * @return {@link Flux} of videos for that owner
     */
    Flux<Video> findAllByCreatedByUserId(Long createdByUserId);

    /**
     * Case‑insensitive search by title with manual pagination.
     *
     * @param fragment substring to match within title
     * @param offset   zero‑based starting row offset
     * @param limit    maximum number of rows to return
     * @return {@link Flux} of matching videos
     */
    @Query("""
           SELECT *
             FROM videos
            WHERE LOWER(title) LIKE LOWER(CONCAT('%', :fragment, '%'))
         ORDER BY title ASC
            OFFSET :offset
             LIMIT :limit
           """)
    Flux<Video> searchByTitle(String fragment, int offset, int limit);


}
