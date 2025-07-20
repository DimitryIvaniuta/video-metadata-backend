package com.github.dimitryivaniuta.videometadata.dto.video;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

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
        @JsonProperty("provider") String provider,
        @JsonProperty("total_videos") long totalVideos,
        @JsonProperty("average_duration_ms") long averageDurationMs,
        @JsonProperty("category_counts_json") String categoryCountsJson,
        @JsonProperty("generated_at") Instant generatedAt
) {
}