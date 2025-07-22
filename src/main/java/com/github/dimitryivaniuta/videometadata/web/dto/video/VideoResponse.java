package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;

import java.time.ZonedDateTime;

/**
 * Public video response.
 *
 * @param id               Internal ID.
 * @param externalVideoId  Provider's external ID (nullable for internal provider).
 * @param title            Title.
 * @param description      Description / summary (nullable).
 * @param provider         Provider enum name.
 * @param category         Category enum name.
 * @param durationMs       Duration in milliseconds.
 * @param uploadDateTime   Original upload instant (provider timezone normalized to UTC).
 * @param createdByUserId  User ID of importer (nullable if system).
 * @param importedAt       Timestamp when imported into system.
 */
public record VideoResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("external_video_id") String externalVideoId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("provider") VideoProvider provider,
        @JsonProperty("category") VideoCategory category,
        @JsonProperty("duration_ms") long durationMs,
        @JsonProperty("upload_date_time") ZonedDateTime uploadDateTime,
        @JsonProperty("created_by_user_id") Long createdByUserId,
        @JsonProperty("imported_at") ZonedDateTime importedAt
) {
    public static VideoResponse from(com.github.dimitryivaniuta.videometadata.domain.entity.Video v) {
        return new VideoResponse(
                v.getId(),
                v.getExternalVideoId(),
                v.getTitle(),
                v.getDescription(),
                v.getProvider().name(),
                v.getCategory().name(),
                v.getDurationMillis(),
                v.getUploadDateTime(),
                v.getCreatedBy().getId(),
                v.getImportedAt()
        );
    }
}