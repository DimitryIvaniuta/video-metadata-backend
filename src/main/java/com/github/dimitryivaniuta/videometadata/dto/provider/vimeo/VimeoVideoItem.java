package com.github.dimitryivaniuta.videometadata.dto.provider.vimeo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vimeo video item details (partial).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoVideoItem(
        @JsonProperty("uri") String uri,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        // Duration in seconds (Vimeo)
        @JsonProperty("duration") Integer duration,
        @JsonProperty("created_time") String createdTime,
        @JsonProperty("release_time") String releaseTime,
        @JsonProperty("modified_time") String modifiedTime,
        @JsonProperty("status") String status
) {
    public String extractVideoId() {
        if (uri == null) return null;
        int idx = uri.lastIndexOf('/');
        return (idx >= 0 && idx < uri.length() - 1) ? uri.substring(idx + 1) : null;
    }
}