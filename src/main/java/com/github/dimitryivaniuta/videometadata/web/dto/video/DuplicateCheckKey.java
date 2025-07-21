package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object used for duplicate detection keys (e.g. Redis).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DuplicateCheckKey(
        @JsonProperty("provider") String provider,
        @JsonProperty("external_video_id") String externalVideoId
) {
    public String redisKey(String prefix) {
        return prefix + ":" + provider + ":" + externalVideoId;
    }
}