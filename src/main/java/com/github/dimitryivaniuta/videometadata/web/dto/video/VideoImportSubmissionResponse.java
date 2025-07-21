package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Response returned immediately after submission of a video import task.
 *
 * @param submissionId    Server-side correlation ID (UUID or internal).
 * @param acceptedCount   Number of new incoming video references accepted to queue.
 * @param skippedDuplicates Number of detected duplicates not queued.
 * @param provider        Provider of import.
 * @param forced          Whether force flag was applied.
 * @param queuedAt        Timestamp when queued.
 */
public record VideoImportSubmissionResponse(
        @JsonProperty("submission_id") String submissionId,
        @JsonProperty("accepted_count") int acceptedCount,
        @JsonProperty("skipped_duplicates") int skippedDuplicates,
        @JsonProperty("provider") String provider,
        @JsonProperty("forced") boolean forced,
        @JsonProperty("queued_at") Instant queuedAt
) {}