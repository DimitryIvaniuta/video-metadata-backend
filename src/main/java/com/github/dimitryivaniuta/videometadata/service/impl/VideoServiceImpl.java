package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.domain.repository.VideoRepository;
import com.github.dimitryivaniuta.videometadata.service.VideoService;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link VideoService}, providing both
 * blocking and reactive APIs for video metadata operations.
 */
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    /**
     * JPA repository for Video entities, including Specification support.
     */
    private final VideoRepository videoRepository;

    // --- Blocking APIs ---

    /**
     * {@inheritDoc}
     */
    @Override
    public Video save(Video video) {
        return videoRepository.save(video);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new EntityNotFoundException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Video> search(VideoProvider provider,
                              ZonedDateTime from,
                              ZonedDateTime to,
                              Long minDurationMillis,
                              Long maxDurationMillis,
                              VideoCategory category,
                              Pageable pageable) {
        Specification<Video> spec = buildSpecification(provider, from, to, minDurationMillis, maxDurationMillis, category);
        return videoRepository.findAll(spec, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<VideoProvider, Long> countPerProvider() {
        return videoRepository.countByProvider().stream()
                .collect(Collectors.toMap(
                        row -> (VideoProvider) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<VideoProvider, Long> averageDurationPerProvider() {
        return videoRepository.averageDurationByProvider().stream()
                .collect(Collectors.toMap(
                        row -> (VideoProvider) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByProviderAndExternalId(VideoProvider provider, String externalVideoId) {
        return videoRepository.existsByProviderAndExternalVideoId(provider, externalVideoId);
    }

    // --- Reactive APIs (wrap blocking calls) ---

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Video> saveMono(Video video) {
        return Mono.fromCallable(() -> save(video))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Video> findByIdMono(Long id) {
        return Mono.fromCallable(() -> findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> deleteByIdMono(Long id) {
        return Mono.fromRunnable(() -> deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Video> searchFlux(VideoProvider provider,
                                  ZonedDateTime from,
                                  ZonedDateTime to,
                                  Long minDurationMillis,
                                  Long maxDurationMillis,
                                  VideoCategory category,
                                  Pageable pageable) {
        return Mono.fromCallable(() -> search(provider, from, to, minDurationMillis, maxDurationMillis, category, pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Map<VideoProvider, Long>> countPerProviderMono() {
        return Mono.fromCallable(this::countPerProvider)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Map<VideoProvider, Long>> averageDurationPerProviderMono() {
        return Mono.fromCallable(this::averageDurationPerProvider)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Boolean> existsByProviderAndExternalIdMono(VideoProvider provider, String externalVideoId) {
        return Mono.fromCallable(() -> existsByProviderAndExternalId(provider, externalVideoId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // --- Private helper to build dynamic JPA Specification ---

    /**
     * Create a {@link Specification} filtering by the given criteria.
     *
     * @param provider          optional video provider
     * @param from              optional earliest upload time
     * @param to                optional latest upload time
     * @param minDurationMillis optional minimum duration in ms
     * @param maxDurationMillis optional maximum duration in ms
     * @param category          optional video category
     * @return combined specification
     */
    private Specification<Video> buildSpecification(VideoProvider provider,
                                                    ZonedDateTime from,
                                                    ZonedDateTime to,
                                                    Long minDurationMillis,
                                                    Long maxDurationMillis,
                                                    VideoCategory category) {
        Specification<Video> spec = Specification.where(null);

        if (provider != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("provider"), provider));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("uploadDateTime"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("uploadDateTime"), to));
        }
        if (minDurationMillis != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("durationMillis"), minDurationMillis));
        }
        if (maxDurationMillis != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("durationMillis"), maxDurationMillis));
        }
        if (category != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), category));
        }
        return spec;
    }
}
