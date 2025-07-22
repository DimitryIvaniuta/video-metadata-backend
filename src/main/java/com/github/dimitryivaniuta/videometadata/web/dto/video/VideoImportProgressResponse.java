package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.entity.VideoImportSubmission;
import com.github.dimitryivaniuta.videometadata.domain.model.SubmissionStatus;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Immutable DTO representing the current state of an asynchronous video‑import submission.
 * <p>
 * It is typically returned by <code>GET /api/videos/import/{submissionId}</code>.
 * All fields are intentionally serialized with snake_case to provide a stable external contract.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record VideoImportProgressResponse(

        // Public, client-facing identifier of the submission.
        @JsonProperty("submission_id") String submissionId,
        // Username that initiated the import.
        @JsonProperty("username") String username,
        // Provider name (e.g. YOUTUBE, VIMEO).
        @NotNull @JsonProperty("provider") VideoProvider provider,
        // Whether duplicates check was bypassed.
        @JsonProperty("forced") boolean forced,
        @JsonProperty("external_ids") List<String> externalIds,

        // Current status of the job (QUEUED/RUNNING/FINISHED/FAILED).
        @JsonProperty("status") SubmissionStatus status,
        // Non-null only if the job failed.
        @JsonProperty("error_message") String errorMessage,

        // When the submission was queued.
        @JsonProperty("queued_at") ZonedDateTime queuedAt,
        // When actual processing started (null while QUEUED).
        @JsonProperty("started_at") ZonedDateTime startedAt,
        // When processing ended (null until FINISHED/FAILED).
        @JsonProperty("finished_at") ZonedDateTime finishedAt,
        // When the submission was updated.
        @JsonProperty("updated_at") ZonedDateTime updatedAt,
        // Import Statistics
        @JsonProperty("statistics") ImportStatisticsDto statistics
) {

    /**
     * Builds a response from an entity. Never returns {@code null}.
     *
     * @param s persistent entity
     * @return mapped DTO
     * @throws NullPointerException if {@code s} is null
     */
    public static VideoImportProgressResponse fromEntity(final VideoImportSubmission s) {
        Objects.requireNonNull(s, "VideoImportSubmission must not be null");
        ImportStatisticsDto stats = ImportStatisticsDto.builder()
                .totalRequested(s.getStatistics() == null ? 0 : s.getStatistics().getTotalRequested())
                .acceptedCount(s.getStatistics() == null ? 0 : s.getStatistics().getAcceptedCount())
                .skippedDuplicates(s.getStatistics() == null ? 0 : s.getStatistics().getSkippedDuplicates())
                .succeededCount(s.getStatistics() == null ? 0 : s.getStatistics().getSucceededCount())
                .failedCount(s.getStatistics() == null ? 0 : s.getStatistics().getFailedCount()).build();
        return new VideoImportProgressResponse(
                s.getSubmissionId(),
                s.getUsername(),
                s.getProvider(),
                s.isForced(),
                List.copyOf(s.getExternalIds()),
                s.getStatus(),
                s.getErrorMessage(),
                s.getQueuedAt(),
                // If you store startedAt separately, use it. Otherwise null/queuedAt depending on status.
                null,
                s.getFinishedAt(),
                s.getUpdatedAt(),
                ImportStatisticsDto.from(s.getStatistics())
        );
    }

    /**
     * Convenience percentage in [0..100]. Returns 0 if {@code totalRequested} is 0.
     */
    @JsonProperty("progress_percent")
    public int progressPercent() {
        int total = statistics.totalRequested();
        if (total == 0) {
            return 0;
        }
        int processed = statistics.acceptedCount() + statistics.skippedDuplicates();
        return Math.min(100, (processed * 100) / total);
    }
}
