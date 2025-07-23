package com.github.dimitryivaniuta.videometadata.web.dto.video;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Immutable external video metadata DTO.
 */
@Value
@Builder
public class ExternalVideoMetadata {
    String externalId;
    ExternalVideoSource source;
    String title;
    String description;
    Duration duration;
    Instant publishedAt;
    String channelName;
    String thumbnailUrl;
    Map<String, Object> raw; // Optional raw response for debugging/audit
}
