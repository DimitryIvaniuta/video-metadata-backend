package com.github.dimitryivaniuta.videometadata.web.dto.provider.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Content details section (duration, video id).
 *
 * @param videoId  External YouTube video ID.
 * @param duration ISO-8601 duration string (e.g., PT4M30S).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContentDetails(
        @JsonProperty("video_id") String videoId,
        @JsonProperty("video_published_at") String videoPublishedAt,
        // ISO-8601 duration (if you call videos.list with part=contentDetails).
        @JsonProperty("duration") String duration
) {
}