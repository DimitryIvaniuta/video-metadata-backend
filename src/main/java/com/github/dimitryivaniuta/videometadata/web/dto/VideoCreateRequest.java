package com.github.dimitryivaniuta.videometadata.web.dto;


import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;

/**
 * Client request for manual video creation.
 */
public record VideoCreateRequest(
        String title,
        String description,
        VideoProvider provider,
        Long durationMillis,
        Long durationSeconds,
        String uploadDateTimeIso,
        VideoCategory category
) { }
