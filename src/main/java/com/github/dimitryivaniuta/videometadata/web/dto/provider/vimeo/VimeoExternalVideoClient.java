package com.github.dimitryivaniuta.videometadata.web.dto.provider.vimeo;

import com.github.dimitryivaniuta.videometadata.config.ProvidersProperties;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.service.ExternalVideoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Fetches video metadata from Vimeo’s API.
 */
@Component
@RequiredArgsConstructor
public class VimeoExternalVideoClient implements ExternalVideoClient {

    private final ProvidersProperties props;
    private final WebClient.Builder webClientBuilder;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Video> fetchVideoMetadata(String externalId) {
        return webClientBuilder
                .baseUrl(props.getVimeo().getBaseUrl())
                .build()
                .get()
                .uri("/videos/{id}", externalId)
                .headers(h -> h.setBearerAuth(props.getVimeo().getAccessToken()))
                .retrieve()
                .bodyToMono(VimeoVideoResponse.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                .map(resp -> {
                    Video v = Video.builder()
                            .externalId(externalId)
                            .provider(VideoProvider.VIMEO)
                            .title(resp.getName())
                            // Vimeo returns duration in seconds; convert to ms
                            .durationMillis(resp.getDuration() * 1_000L)
                            .uploadDateTime(resp.getCreatedTime())
                            .build();
                    return v;
                });
    }
}
