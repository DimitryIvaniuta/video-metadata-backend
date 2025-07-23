package com.github.dimitryivaniuta.videometadata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Video listing & statistics caching settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "video")
public class VideoProperties {
    private Stats stats = new Stats();
    private int defaultPageSize = 20;
    private int maxPageSize = 100;

    @Data
    public static class Stats {
        private long cacheTtlMs = 300_000;
    }
}
