package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
class VideoRepositoryCustomImpl implements VideoRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Video> search(VideoProvider provider,
                              ZonedDateTime from,
                              ZonedDateTime to,
                              Long minDurationMs,
                              Long maxDurationMs,
                              VideoCategory category,
                              Pageable pageable) {

        Criteria criteria = Criteria.empty();

        if (provider != null) {
            criteria = criteria.and("provider").is(provider);
        }
        if (from != null) {
            criteria = criteria.and("upload_date").greaterThanOrEquals(from);
        }
        if (to != null) {
            criteria = criteria.and("upload_date").lessThanOrEquals(to);
        }
        if (minDurationMs != null) {
            criteria = criteria.and("duration_ms").greaterThanOrEquals(minDurationMs);
        }
        if (maxDurationMs != null) {
            criteria = criteria.and("duration_ms").lessThanOrEquals(maxDurationMs);
        }
        if (category != null) {
            criteria = criteria.and("category").is(category);
        }

        Query query = Query.query(criteria)
                .with(pageable)
                .limit(pageable.getPageSize())
                .offset((long) pageable.getPageNumber() * pageable.getPageSize());

        return template.select(query, Video.class);
    }

    @Override
    public Mono<Long> countSearch(VideoProvider provider,
                                  ZonedDateTime from,
                                  ZonedDateTime to,
                                  Long minDurationMs,
                                  Long maxDurationMs,
                                  VideoCategory category) {

        Criteria criteria = Criteria.empty();

        if (provider != null) {
            criteria = criteria.and("provider").is(provider);
        }
        if (from != null) {
            criteria = criteria.and("upload_date").greaterThanOrEquals(from);
        }
        if (to != null) {
            criteria = criteria.and("upload_date").lessThanOrEquals(to);
        }
        if (minDurationMs != null) {
            criteria = criteria.and("duration").greaterThanOrEquals(minDurationMs);
        }
        if (maxDurationMs != null) {
            criteria = criteria.and("duration").lessThanOrEquals(maxDurationMs);
        }
        if (category != null) {
            criteria = criteria.and("category").is(category);
        }

        return template.count(Query.query(criteria), Video.class);
    }
}
