package com.github.dimitryivaniuta.videometadata.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures WebClients for each external provider.
 */
@Configuration
//@EnableConfigurationProperties(ProvidersProperties.class)
public class ExternalApiConfig {

    @Bean("youtubeWebClient")
    public WebClient youtubeWebClient(WebClient.Builder builder,
                                      ProvidersProperties props) {
        return builder
                .baseUrl(props.getYoutube().getBaseUrl())
                .build();
    }

    @Bean("vimeoWebClient")
    public WebClient vimeoWebClient(WebClient.Builder builder,
                                    ProvidersProperties props) {
        return builder
                .baseUrl(props.getVimeo().getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + props.getVimeo().getAccessToken())
                .build();
    }
}
