package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.config.ProvidersProperties;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.web.dto.provider.youtube.ContentDetails;
import com.github.dimitryivaniuta.videometadata.web.dto.provider.youtube.PlaylistItem;
import com.github.dimitryivaniuta.videometadata.web.dto.provider.youtube.YouTubePlaylistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Fetches video metadata from YouTube via the Data API v3.
 */
@Component
@RequiredArgsConstructor
public class YouTubeExternalVideoClient implements ExternalVideoClient {

    private final ProvidersProperties props;
    private final WebClient.Builder webClientBuilder;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Video> fetchVideoMetadata(String externalId) {
        return webClientBuilder
                .baseUrl(props.getYoutube().getBaseUrl())
                .build()
                .get()
                .uri(uri -> uri
                        .path("/youtube/v3/videos")
                        .queryParam("part", "snippet,contentDetails")
                        .queryParam("id", externalId)
                        .queryParam("key", props.getYoutube().getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(YouTubePlaylistResponse.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                .flatMap(resp -> {
                    if (resp.getItems().isEmpty()) {
                        return Mono.error(
                                new VideoNotFoundException("YouTube", externalId));
                    }
                    PlaylistItem item = resp.getItems().get(0);
                    ContentDetails cd = item.getContentDetails();
                    Video v = Video.builder()
                            .externalId(externalId)
                            .provider(VideoProvider.YOUTUBE)
                            .title(item.getSnippet().getTitle())
                            // Duration.parse handles ISO‑8601 durations like PT1H2M10S
                            .durationMillis(Duration.parse(cd.getDuration()).toMillis())
                            .uploadDateTime(item.getSnippet().getPublishedAt())
                            .build();
                    return Mono.just(v);
                });
    }

    /** Thrown when the external provider returns no data for the given ID. */
    public static class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String provider, String id) {
            super(provider + " returned no video for ID " + id);
        }
    }
}
