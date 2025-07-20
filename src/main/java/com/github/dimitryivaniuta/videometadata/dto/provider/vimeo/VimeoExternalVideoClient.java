package com.github.dimitryivaniuta.videometadata.dto.provider.vimeo;


import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.provider.ExternalVideoClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Fetches videos from Vimeo API.
 */
@Component
@RequiredArgsConstructor
public class VimeoExternalVideoClient implements ExternalVideoClient {

    private final WebClient webClient;

    public VimeoExternalVideoClient(
            @Qualifier("vimeoWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    @Override
    @CircuitBreaker(name = "vimeoCB", fallbackMethod = "fallback")
    @Retry(name = "vimeoRetry")
    @RateLimiter(name = "vimeoRL")
    public Flux<Video> fetchVideos(String userId) {
        return webClient.get()
                .uri("/users/{userId}/videos", userId)
                .retrieve()
                .bodyToMono(com.github.dimitryivaniuta.videometadata.service.VimeoExternalVideoClient.VimeoResponse.class)
                .flatMapMany(resp -> Flux.fromIterable(resp.data()))
                .map(item -> {
                    Video v = new Video();
                    v.setSource("Vimeo");
                    v.setTitle(item.name());
                    v.setDescription(item.description());
                    v.setUploadDateTime();setUploadDate(
                            LocalDate.parse(item.createdTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    );
                    // Vimeo gives seconds directly
                    v.setDuration(item.duration());
                    return v;
                });
    }

    private Flux<Video> fallback(String userId, Throwable t) {
        return Flux.empty();
    }

    // --- Inner DTOs for Jackson binding ---

    private static record VimeoResponse(java.util.List<com.github.dimitryivaniuta.videometadata.service.VimeoExternalVideoClient.VimeoItem> data) {}
    private static record VimeoItem(
            String name,
            String description,
            String createdTime,
            long   duration
    ) {}
}
