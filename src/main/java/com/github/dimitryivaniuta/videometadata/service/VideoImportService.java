package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.config.ImportProperties;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import com.github.dimitryivaniuta.videometadata.event.AsyncErrorEvent;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

/**
 * Service responsible for orchestrating background imports of video metadata
 * from external providers.  Submits import jobs asynchronously, applies
 * concurrency limits, retries, de-duplication, and publishes any async errors
 * as {@link AsyncErrorEvent}s.
 * Orchestrates background video metadata imports:
 * - per-user rate limiting
 * - duplicate suppression
 * - global concurrency throttling
 * - external API retries
 * - persistence via VideoService
 * - publishes AsyncErrorEvent on failures
 */
@Service
@RequiredArgsConstructor
@Order  // ensure this bean is initialized before any listeners
@Slf4j
public class VideoImportService implements InitializingBean {

    private final VideoService videoService;
    private final UserService userService;
    private final ReactiveRedisTemplate<String, Object> redis;
    private final ImportProperties props;
    private final ApplicationEventPublisher publisher;
    private final Map<VideoProvider, ExternalVideoClient> clientMap;

    private RetryBackoffSpec retrySpec;
    private Method submitMethod;

    @Override
    public void afterPropertiesSet() throws NoSuchMethodException {
        this.retrySpec = Retry.fixedDelay(
                props.getMaxRetries(),
                props.getBackoffSeconds()
        ).toRetryBackoffSpec();
        this.submitMethod = this.getClass()
                .getMethod("submitImport", String.class, VideoImportRequest.class);
    }

    /**
     * Schedule an asynchronous import for the given user and request.
     * @param username the current user's username
     * @param request  provider + external IDs
     */
    @Async("taskExecutor")
    public void submitImport(String username, VideoImportRequest request) {
        VideoProvider provider = request.getProvider();
        var client = clientMap.get(provider);
        if (client == null) {
            throw new IllegalStateException("No client for provider: " + provider);
        }

        // Per-user rate limiting
        String userKey = "import:rate:" + username;
        Long count = redis.opsForValue()
                .increment(userKey, 1)
                .flatMap(c -> {
                    if (c == 1) {
                        return redis.expire(userKey, props.getPerUser().getWindow())
                                .thenReturn(c);
                    }
                    return Mono.just(c);
                })
                .block(Duration.ofSeconds(5));
        if (count != null && count > props.getPerUser().getRateLimitPerUser()) {
            throw new RateLimitExceededException(username, props.getPerUser().getRateLimitPerUser());
        }

        Flux.fromIterable(request.getExternalIds())
                .flatMap(extId -> {
                    String dupKey = "import:dup:" + username + ":" + extId;
                    return redis.opsForValue()
                            .setIfAbsent(dupKey, "1", Duration.ofMillis(props.getPerUser().getDuplicateCacheTtlMs()))
                            .flatMap(set -> {
                                if (!set && props.getThrottle().isDuplicateJobReject()) {
                                    return Mono.error(new DuplicateImportException(extId));
                                }
                                return importSingle(username, client, provider, extId);
                            });
                }, props.getThrottle().getMaxConcurrent())
                .doOnError(err -> log.error("Import stream error: {}", err.getMessage()))
                .onErrorContinue((err, obj) ->
                        publisher.publishEvent(new AsyncErrorEvent(
                                this, err, submitMethod, new Object[]{username, request}
                        ))
                )
                .subscribe();
    }




    /**
     * Import a single video ID for the given user.
     * @param username   current user's name
     * @param client     provider-specific client
     * @param provider   video provider enum
     * @param externalId external video ID
     * @return completion Mono
     */
    private Mono<Void> importSingle(String username,
                                    ExternalVideoClient client,
                                    VideoProvider provider,
                                    String externalId) {
        return videoService.existsByProviderAndExternalIdMono(provider, externalId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.empty();
                    }
                    return client.fetchVideoMetadata(externalId)
                            .retryWhen(retrySpec)
                            .flatMap(video -> userService
                                    .findByUsernameMono(username)
                                    .map(user -> {
                                        video.setCreatedBy(user);
                                        return video;
                                    })
                            )
                            .flatMap(videoService::saveMono)
                            .then();
                });
    }

    /** Indicates a user exceeded their import rate. */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String user, int limit) {
            super("User " + user + " exceeded rate limit of " + limit);
        }
    }

    /** Indicates a duplicate import was rejected. */
    public static class DuplicateImportException extends RuntimeException {
        public DuplicateImportException(String externalId) {
            super("Duplicate import rejected for ID: " + externalId);
        }
    }
}
