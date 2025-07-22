package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.config.ImportProperties;
import com.github.dimitryivaniuta.videometadata.config.RateLimiterProperties;
import com.github.dimitryivaniuta.videometadata.domain.entity.VideoImportSubmission;
import com.github.dimitryivaniuta.videometadata.domain.model.SubmissionStatus;
import com.github.dimitryivaniuta.videometadata.domain.repository.VideoImportSubmissionRepository;
import com.github.dimitryivaniuta.videometadata.event.VideoImportRequestedEvent;
import com.github.dimitryivaniuta.videometadata.exception.RateLimitedException;
import com.github.dimitryivaniuta.videometadata.ratelimit.RateLimitMetadata;
import com.github.dimitryivaniuta.videometadata.ratelimit.ReactiveRateLimiter;
import com.github.dimitryivaniuta.videometadata.service.VideoImportService;
import com.github.dimitryivaniuta.videometadata.util.SubmissionIdGenerator;
import com.github.dimitryivaniuta.videometadata.web.dto.video.ImportStatisticsDto;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportProgressResponse;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportSubmissionResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link VideoImportService}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Check per-user throttling/rate-limit (via Redis bucket or your limiter)</li>
 *   <li>Persist a new {@link VideoImportSubmission} row with INITIAL status</li>
 *   <li>Publish {@link VideoImportRequestedEvent} to kick async processing</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoImportServiceImpl implements VideoImportService {

    private final ApplicationEventPublisher         publisher;
    private final ImportProperties importProps;
    private final RateLimiterProperties rateLimiterProperties;
    private final SubmissionIdGenerator submissionIdGenerator;
    private final VideoImportSubmissionRepository submissionRepo;
    private final ReactiveRateLimiter rateLimiter;

    @Override
    public Mono<VideoImportSubmissionResponse> startImport(final String username,
                                                           final VideoImportRequest request) {

        Objects.requireNonNull(username, "username must not be null");

        // 1) Per-user rate limiting
        return rateLimiter.tryConsume("import:" + username,
                        importProps.getPerUser().getRateLimitPerUser(),
                        rateLimiterProperties.getDefaultCapacity(),
                        rateLimiterProperties.getRefillTokens(),
                        importProps.getPerUser().getWindow())
                .flatMap(meta -> {
                    if (!meta.allowed()) {
                        return Mono.error(new RateLimitedException(username, meta));
                    }
                    // 2) Persist submission row
                    String submissionId = submissionIdGenerator.nextId();
                    ZonedDateTime now = ZonedDateTime.now();

                    VideoImportSubmission entity = VideoImportSubmission.builder()
                            .submissionId(submissionId)
                            .username(username)
                            .provider(request.provider())
                            .externalIds(new LinkedHashSet<>(request.externalIds()))
                            .externalPlaylistId(request.externalPlaylistId())
                            .forced(Boolean.TRUE.equals(request.force()))
                            .queuedAt(now)
                            .status(SubmissionStatus.QUEUED)
                            .build();

                    return Mono.fromCallable(() -> submissionRepo.save(entity))      // blocking JPA
                            .subscribeOn(Schedulers.boundedElastic())             // move to worker pool
                            .map(VideoImportServiceImpl::buildResponse)                   // replace thenReturn(...)
                            .doOnSuccess(resp -> {
                                publisher.publishEvent(
                                        new VideoImportRequestedEvent(this, submissionId, username, request)
                                );
                                log.info("Import queued id={} user={} provider={}", submissionId, username, request.provider());
                            });
                });
    }

    @Override
    public Mono<VideoImportProgressResponse> getProgress(String submissionId) {
        return Mono.fromCallable(() -> submissionRepo.findBySubmissionId(submissionId))   // blocking
                .subscribeOn(Schedulers.boundedElastic())                              // run off event-loop
                .flatMap(opt -> opt.map(VideoImportServiceImpl::toProgressResponse)
                        .map(Mono::just)
                        .orElseGet(Mono::empty));
    }

    // ----------------------- Helpers -----------------------

    private static VideoImportSubmissionResponse buildResponse(VideoImportSubmission s) {
        return VideoImportSubmissionResponse.builder()
                .externalPlaylist(s.getExternalPlaylistId())
                .externalPlaylist(s.getExternalPlaylistId())
                .queuedAt(s.getQueuedAt())
                .forced(s.isForced())
                .submissionId(s.getSubmissionId())
                .provider(s.getProvider())
                .build();
    }

    private static VideoImportProgressResponse toProgressResponse(VideoImportSubmission s) {
        return VideoImportProgressResponse.builder()
                .submissionId(s.getSubmissionId())
                .username(s.getUsername())
                .provider(s.getProvider())
                .forced(s.isForced())
                .externalIds(List.copyOf(s.getExternalIds()))
                .status(s.getStatus())
                .errorMessage(s.getErrorMessage())
                .queuedAt(s.getQueuedAt())
                .startedAt(s.getStartedAt())
                .finishedAt(s.getFinishedAt())
                .updatedAt(s.getUpdatedAt())
                .statistics(ImportStatisticsDto.from(s.getStatistics()))
                .build();
    }

}
