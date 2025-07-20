package com.github.dimitryivaniuta.videometadata.web.dto;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;

import java.time.ZonedDateTime;

/**
 * Video outward representation.
 * Provides duration in seconds (legacy) and milliseconds (high precision).
 */
public record VideoResponse(
        Long id,
        String title,
        String description,
        VideoProvider provider,
        long durationSeconds,
        long durationMillis,
        ZonedDateTime uploadDateTime,
        VideoCategory category,
        Long createdByUserId
) {
    public static VideoResponse from(final Video v) {
        long ms = v.getDuration() == null ? 0L : v.getDuration().toMillis();
        long sec = ms / 1000;
        return new VideoResponse(
                v.getId(),
                v.getTitle(),
                v.getDescription(),
                v.getProvider(),
                sec,
                ms,
                v.getUploadDateTime(),
                v.getCategory(),
                v.getCreatedByUserId()
        );
    }
}