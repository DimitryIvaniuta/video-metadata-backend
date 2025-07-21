package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Video} entities.
 * <p>
 * Supports dynamic filtering via {@link JpaSpecificationExecutor},
 * plus custom aggregation queries.
 */
@Repository
public interface VideoRepository
        extends JpaRepository<Video, Long>,
        JpaSpecificationExecutor<Video> {

    /**
     * Count total videos grouped by provider.
     *
     * @return a list of Object arrays where
     *         index 0 = {@link VideoProvider}, index 1 = count (Long)
     */
    @Query("SELECT v.provider, COUNT(v) FROM Video v GROUP BY v.provider")
    List<Object[]> countByProvider();

    /**
     * Compute average duration (in ms) grouped by provider.
     *
     * @return a list of Object arrays where
     *         index 0 = {@link VideoProvider}, index 1 = average duration (Double)
     */
    @Query("SELECT v.provider, AVG(v.durationMillis) FROM Video v GROUP BY v.provider")
    List<Object[]> averageDurationByProvider();

    /**
     * Check existence of a video by provider and its external ID.
     *
     * @param provider        the video source
     * @param externalVideoId the external provider’s video ID
     * @return true if a matching record exists
     */
    boolean existsByProviderAndExternalVideoId(VideoProvider provider, String externalVideoId);
}
