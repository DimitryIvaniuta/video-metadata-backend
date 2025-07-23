package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Embedded DTO mirroring {@link ImportStatistics}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record ImportStatisticsDto(
        // How many items the client asked to import (IDs or playlist items).
        @JsonProperty("total_requested") int totalRequested,
        // Successfully persisted new videos.
        @JsonProperty("accepted_count") int acceptedCount,
        // Skipped because already present or rejected as duplicates.
        @JsonProperty("skipped_duplicates") int skippedDuplicates,
        // How many items succeeded
        @JsonProperty("succeeded_count") int succeededCount,
        // How many items failed
        @JsonProperty("failed_count") int failedCount
) {
    public static ImportStatisticsDto from(ImportStatistics s) {
        return ImportStatisticsDto.builder()
                .totalRequested(s.getRequestedCount())
                .acceptedCount(s.getAcceptedCount())
                .skippedDuplicates(s.getSkippedDuplicates())
                .succeededCount(s.getSucceededCount())
                .failedCount(s.getFailedCount()).build();
    }
}