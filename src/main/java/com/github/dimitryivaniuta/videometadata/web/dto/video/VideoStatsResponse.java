package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;

import java.time.ZonedDateTime;

/**
 * Aggregated statistics per provider/category or global.
 *
 * @param provider           Provider name (or "ALL").
 * @param totalVideos        Count of videos for this provider scope.
 * @param averageDurationMs  Average duration in milliseconds (rounded).
 * @param categoryCountsJson Pre-computed JSON string (category -> count) to avoid nested structures (optional).
 * @param generatedAt        Timestamp the stats snapshot generated.
 */
public record VideoStatsResponse(
        @JsonProperty("provider") VideoProvider provider,
        @JsonProperty("total_videos") long totalVideos,
        @JsonProperty("average_duration_ms") long averageDurationMs,
        @JsonProperty("category_counts_json") String categoryCountsJson,
        @JsonProperty("generated_at") ZonedDateTime generatedAt
) {
}