package com.github.dimitryivaniuta.videometadata.web.dto.provider.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Video snippet portion of YouTube API response.
 *
 * @param title        Title.
 * @param description  Description (may be large).
 * @param publishedAt  Published timestamp (RFC 3339).
 * @param channelId    ID of the channel (optional usage).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Snippet(
        @JsonProperty("published_at") String publishedAt,
        @JsonProperty("channel_id") String channelId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        /**
         * Map of thumbnail name -> object (width, height, url).
         */
        @JsonProperty("thumbnails") Map<String, Object> thumbnails,
        @JsonProperty("channelTitle") String channelTitle,
        @JsonProperty("playlistId") String playlistId,
        @JsonProperty("position") Integer position,
        @JsonProperty("resourceId") ResourceId resourceId
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceId(
            @JsonProperty("kind") String kind,
            @JsonProperty("video_id") String videoId
    ) {}
}