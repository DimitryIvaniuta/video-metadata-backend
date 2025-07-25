package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.domain.repository.VideoRepository;
import com.github.dimitryivaniuta.videometadata.domain.repository.projection.ProviderAvgDurationRow;
import com.github.dimitryivaniuta.videometadata.security.AuthenticatedUserAccessor;
import com.github.dimitryivaniuta.videometadata.service.ExternalVideoAggregationService;
import com.github.dimitryivaniuta.videometadata.service.VideoService;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ExternalVideoMetadata;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reactive implementation of {@link VideoService}.
 * <p>
 * Uses {@link TransactionalOperator} for operations that write to the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final AuthenticatedUserAccessor authAccessor;

    /** Reactive transaction operator for R2DBC. */
    private final TransactionalOperator tx;

    /** Main repository for Video persistence. */
    private final VideoRepository videoRepository;

    /** Aggregator to fetch metadata from external providers. */
    private final ExternalVideoAggregationService externalService;

    /** Optional: low‑level DatabaseClient for custom video queries (if needed). */
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Video> createVideo(VideoProvider provider, String externalId) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(externalId, "externalId");

        return authAccessor.requireUser()
                .flatMap(user ->
                        externalService.getOne(provider, externalId)
                                .map(meta -> meta.mapExternalToVideo(user.getId()))
                                .flatMap(videoRepository::save)
                )
                .as(tx::transactional)
                .onErrorMap(DuplicateKeyException.class, ex ->
                        new IllegalStateException("Video already exists for user="
                                + /* user ID unknown here, but message suffices */ externalId, ex)
                );
    }

    @Override
    public Mono<Video> getById(Long id) {
        return videoRepository.findById(id);
    }

    @Override
    public Flux<Video> getByOwner(Long ownerId) {
        return videoRepository.findAllByCreatedByUserId(ownerId);
    }

    @Override
    public Mono<Video> updateMetadata(Long videoId) {
        Objects.requireNonNull(videoId, "videoId");
        return videoRepository.findById(videoId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Video not found: " + videoId)))
                .flatMap(existing -> externalService.getOne(VideoProvider.valueOf(existing.getProvider().name()), existing.getExternalId())
                        .flatMap(meta -> {
                            meta.applyMetadata(existing);
                            return videoRepository.save(existing);
                        }))
                .as(tx::transactional);
    }

    @Override
    public Mono<Void> delete(Long id) {
        Objects.requireNonNull(id, "id");
        return videoRepository.deleteById(id)
                .as(tx::transactional);
    }

    @Override
    public Flux<Video> searchByTitle(String fragment, int offset, int limit) {
        String q = fragment == null ? "" : fragment.trim();
        // assuming custom method in repository: Flux<Video> searchByTitle(String, int, int)
        return videoRepository.searchByTitle(q, offset, limit);
    }

    @Override
    public Flux<ExternalVideoMetadata> fetchExternalMetadata(List<VideoRequest> requests) {
        return externalService.getMany(requests.stream()
                .map(r -> new ExternalVideoAggregationService.VideoRequest(
                        r.provider(), r.externalId()))
                .toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Map<VideoProvider, Long>> averageDurationPerProviderMono() {
        return videoRepository.averageDurationByProvider()
                .collectMap(
                        ProviderAvgDurationRow::getProvider,
                        row -> Math.round(row.getAvg())
                );
    }

}
