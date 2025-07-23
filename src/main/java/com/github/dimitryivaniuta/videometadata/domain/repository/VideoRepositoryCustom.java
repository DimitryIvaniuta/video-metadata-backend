package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

/**
 * Custom reactive search API that replaces JPA Specifications.
 */
public interface VideoRepositoryCustom {

    Flux<Video> search(VideoProvider provider,
                       ZonedDateTime from,
                       ZonedDateTime to,
                       Long minDurationMs,
                       Long maxDurationMs,
                       VideoCategory category,
                       Pageable pageable);

    Mono<Long> countSearch(VideoProvider provider,
                           ZonedDateTime from,
                           ZonedDateTime to,
                           Long minDurationMs,
                           Long maxDurationMs,
                           VideoCategory category);
}
