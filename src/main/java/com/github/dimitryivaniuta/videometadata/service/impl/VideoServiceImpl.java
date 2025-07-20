package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.persistence.repository.VideoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link VideoService}.
 * <p>
 * Provides dynamic filtering using a simple JPQL builder to avoid
 * heavyweight Specification complexity. Statistics are computed with
 * aggregate JPQL queries for efficiency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements com.github.dimitryivaniuta.videometadata.service.impl.VideoService {

    // Alphabetically sorted fields
    private final EntityManager   entityManager;
    private final VideoRepository videoRepository;

    // ---------------- Synchronous API ----------------

    @Override
    @Transactional
    public Video save(Video video) {
        Objects.requireNonNull(video, "video must not be null");
        Video persisted = videoRepository.save(video);
        log.debug("Saved video id={} provider={} externalId={}",
                persisted.getId(), persisted.getProvider(), persisted.getExternalVideoId());
        return persisted;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!videoRepository.existsById(id)) {
            log.debug("Delete requested for non-existing video id={}", id);
            return;
        }
        videoRepository.deleteById(id);
        log.info("Deleted video id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Video> search(VideoProvider provider,
                              ZonedDateTime from,
                              ZonedDateTime to,
                              Long minDurationMillis,
                              Long maxDurationMillis,
                              VideoCategory category,
                              Pageable pageable) {

        StringBuilder jpql = new StringBuilder("SELECT v FROM Video v WHERE 1=1");
        StringBuilder count = new StringBuilder("SELECT COUNT(v) FROM Video v WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        appendFilter(jpql, count, params, "provider", provider);
        appendFilter(jpql, count, params, "category", category);
        if (from != null) {
            jpql.append(" AND v.uploadDateTime >= :from");
            count.append(" AND v.uploadDateTime >= :from");
            params.put("from", from);
        }
        if (to != null) {
            jpql.append(" AND v.uploadDateTime < :to");
            count.append(" AND v.uploadDateTime < :to");
            params.put("to", to);
        }
        if (minDurationMillis != null) {
            jpql.append(" AND v.duration >= :minDur");
            count.append(" AND v.duration >= :minDur");
            params.put("minDur", java.time.Duration.ofMillis(minDurationMillis));
        }
        if (maxDurationMillis != null) {
            jpql.append(" AND v.duration <= :maxDur");
            count.append(" AND v.duration <= :maxDur");
            params.put("maxDur", java.time.Duration.ofMillis(maxDurationMillis));
        }

        // Sorting
        if (pageable.getSort().isSorted()) {
            jpql.append(" ORDER BY ");
            List<String> clauses = new ArrayList<>();
            for (Sort.Order o : pageable.getSort()) {
                clauses.add("v." + o.getProperty() + " " + o.getDirection().name());
            }
            jpql.append(String.join(", ", clauses));
        } else {
            jpql.append(" ORDER BY v.uploadDateTime DESC");
        }

        TypedQuery<Video> query = entityManager.createQuery(jpql.toString(), Video.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(count.toString(), Long.class);
        params.forEach((k, v) -> { query.setParameter(k, v); countQuery.setParameter(k, v); });

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Video> content = query.getResultList();
        long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }

    private void appendFilter(StringBuilder jpql, StringBuilder count, Map<String, Object> params,
                              String field, Object value) {
        if (value != null) {
            jpql.append(" AND v.").append(field).append(" = :").append(field);
            count.append(" AND v.").append(field).append(" = :").append(field);
            params.put(field, value);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<VideoProvider, Long> countPerProvider() {
        String q = "SELECT v.provider, COUNT(v) FROM Video v GROUP BY v.provider";
        List<Object[]> rows = entityManager.createQuery(q, Object[].class).getResultList();
        return rows.stream()
                .collect(Collectors.toMap(r -> (VideoProvider) r[0], r -> (Long) r[1]));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<VideoProvider, Long> averageDurationPerProvider() {
        // Uses milliseconds extract by Duration (converted to seconds for AVG at DB if needed)
        String q = "SELECT v.provider, AVG(EXTRACT(EPOCH FROM v.duration) * 1000) FROM Video v GROUP BY v.provider";
        List<Object[]> rows = entityManager.createQuery(q, Object[].class).getResultList();
        Map<VideoProvider, Long> result = new EnumMap<>(VideoProvider.class);
        for (Object[] r : rows) {
            VideoProvider provider = (VideoProvider) r[0];
            Double avgMs = (Double) r[1];
            result.put(provider, avgMs == null ? 0L : avgMs.longValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProviderAndExternalId(VideoProvider provider, String externalVideoId) {
        return videoRepository.existsByProviderAndExternalVideoId(provider, externalVideoId);
    }

    // ---------------- Reactive Wrappers ----------------

    @Override
    public Mono<Video> saveMono(Video video) {
        return Mono.fromCallable(() -> save(video))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Video> findByIdMono(Long id) {
        return Mono.fromCallable(() -> findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Video not found id=" + id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteByIdMono(Long id) {
        return Mono.fromRunnable(() -> deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<Video> searchFlux(VideoProvider provider,
                                  ZonedDateTime from,
                                  ZonedDateTime to,
                                  Long minDurationMillis,
                                  Long maxDurationMillis,
                                  VideoCategory category,
                                  Pageable pageable) {
        return Mono.fromCallable(() -> search(provider, from, to, minDurationMillis, maxDurationMillis, category, pageable))
                .flatMapMany(page -> Flux.fromIterable(page.getContent()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Map<VideoProvider, Long>> countPerProviderMono() {
        return Mono.fromCallable(this::countPerProvider)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Map<VideoProvider, Long>> averageDurationPerProviderMono() {
        return Mono.fromCallable(this::averageDurationPerProvider)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> existsByProviderAndExternalIdMono(VideoProvider provider, String externalVideoId) {
        return Mono.fromCallable(() -> existsByProviderAndExternalId(provider, externalVideoId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
