package com.github.dimitryivaniuta.videometadata.service;


import com.github.dimitryivaniuta.videometadata.config.ImportProperties;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.event.AsyncErrorEvent;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import io.github.resilience4j.retry.Retry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.retry.RetryBackoffSpec;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

/**
 * Performs the actual import logic (rate‑limiting, duplication, fetch & persist).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoImportProcessor {

    private final VideoService                  videoService;
    private final UserService                   userService;
    private final ReactiveRedisTemplate<String,Object> redis;
    private final ImportProperties              props;
    private final Map<VideoProvider,ExternalVideoClient> clients;
    private final ApplicationEventPublisher     publisher;

    private RetryBackoffSpec retrySpec;
    private Method            asyncMethod;

    @PostConstruct
    public void init() throws NoSuchMethodException {
        retrySpec = Retry.fixedDelay(
                props.getThrottle().getServiceRetry().getMaxAttempts(),
                props.getThrottle().getServiceRetry().getBackoff()
        ).toRetryBackoffSpec();
        asyncMethod = this.getClass()
                .getMethod("processImport", String.class, VideoImportRequest.class);
    }

    /**
     * Invoked asynchronously when an import is requested.
     */
    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public void processImport(String username,
                              VideoImportRequest request) {
        var client = clients.get(request.getProvider());
        if (client == null) {
            log.error("No ExternalVideoClient for {}", request.getProvider());
            return;
        }

        Flux.fromIterable(request.getExternalIds())
                // per‑user rate limit
                .flatMap(extId -> {
                    String key = "import:rate:" + username;
                    return redis.opsForValue().increment(key,1)
                            .flatMap(count -> {
                                if (count == 1) {
                                    return redis.expire(key, props.getPerUser().getWindow())
                                            .thenReturn(count);
                                }
                                return Flux.just(count).single();
                            })
                            .flatMap(count -> {
                                if (count > props.getPerUser().getRateLimitPerUser()) {
                                    return Flux.<Video>error(
                                                    new RuntimeException("Rate limit exceeded"))
                                            .single();
                                }
                                return Flux.just(extId).single();
                            });
                })
                // duplicate suppression
                .flatMap(extId -> {
                    String dupKey = "import:dup:" + username + ":" + extId;
                    return redis.opsForValue()
                            .setIfAbsent(dupKey, "1",
                                    Duration.ofMillis(props.getPerUser().getDuplicateCacheTtlMs()))
                            .flatMap(set -> set
                                    ? Flux.<String>just(extId).single()
                                    : props.getThrottle().isDuplicateJobReject()
                                    ? Flux.<String>error(
                                            new RuntimeException("Duplicate import " + extId))
                                    .single()
                                    : Flux.<String>just(extId).single()
                            );
                })
                // fetch & persist
                .flatMap(extId ->
                                videoService.existsByProviderAndExternalIdMono(
                                        request.getProvider(), extId
                                ).flatMap(exists -> {
                                    if (exists) return Flux.<Video>empty().single();
                                    return client.fetchVideoMetadata(extId)
                                            .retryWhen(retrySpec)
                                            .flatMap(v -> userService
                                                    .findByUsernameMono(username)
                                                    .map((User u) -> { v.setCreatedBy(u); return v; }))
                                            .flatMap(videoService::saveMono);
                                })
                        , props.getThrottle().getMaxConcurrent())
                .doOnError(err -> {
                    log.error("Async import error for {}: {}", username, err.toString());
                    publisher.publishEvent(
                            new AsyncErrorEvent(this, err, asyncMethod,
                                    new Object[]{username, request})
                    );
                })
                .subscribe();
    }
}
