package com.github.dimitryivaniuta.videometadata.web.dto.provider.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Individual playlist item referencing a video.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaylistItem(
        @JsonProperty("kind") String kind,
        @JsonProperty("etag") String etag,
        @JsonProperty("id") String id,
        @JsonProperty("snippet") Snippet snippet,
        @JsonProperty("contentDetails") ContentDetails contentDetails,
        @JsonProperty("status") Object status // optional; kept generic
) {}