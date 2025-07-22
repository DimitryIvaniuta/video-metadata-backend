package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.ZonedDateTime;


/**
 * Response returned immediately after submission of a video import task.
 *
 * @param submissionId    Server-side correlation ID (UUID or internal).
 * @param provider        Provider of import.
 * @param forced          Whether force flag was applied.
 * @param queuedAt        Timestamp when queued.
 */
@Builder
public record VideoImportSubmissionResponse(
        @JsonProperty("submission_id") String submissionId,
        @NotNull @JsonProperty("provider") VideoProvider provider,
        @JsonProperty("forced") boolean forced,
        @JsonProperty("queued_at") ZonedDateTime queuedAt,
        @JsonProperty("external_playlist") String externalPlaylist
) {}