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
     * Average duration (ms) grouped by provider.
     *
     * @return Flux of projection rows (provider, avg)
     */
    @Query("""
           SELECT provider, AVG(duration_ms) AS avg
           FROM videos
           GROUP BY provider
           """)
    Flux<ProviderAvgDurationRow> averageDurationByProvider();

    /**
     * Check existence of a video by provider and external ID.
     *
     * @param provider        provider enum
     * @param externalVideoId external id from the provider
     * @return Mono true if exists
     */
    Mono<Boolean> existsByProviderAndExternalVideoId(VideoProvider provider, String externalVideoId);


    /**
     * Count videos grouped by provider.
     *
     * @return Flux of projection rows (provider, count)
     */
    @Query("""
           SELECT provider, COUNT(*) AS cnt
           FROM videos
           GROUP BY provider
           """)
    Flux<ProviderCountRow> countByProvider();
}
