package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO for filtering /api/videos list requests.
 * <p>
 * Supports:
 * <ul>
 *   <li>providers (e.g. YOUTUBE, VIMEO)</li>
 *   <li>categories</li>
 *   <li>date range (uploaded_from, uploaded_to)</li>
 *   <li>duration range (duration_min_ms, duration_max_ms)</li>
 *   <li>title_contains (substring search)</li>
 *   <li>pagination (page, size) and sort</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoFilterRequest(

        //Which providers to include; values matching VideoProvider enum names.
        @JsonProperty("providers") Set<VideoProvider> providers,

        //Which categories to include; values matching VideoCategory enum names.
        @JsonProperty("categories")
        Set<VideoCategory> categories,

        //Earliest upload timestamp (inclusive). ISO‑8601 e.g. 2025‑07‑18T12:00:00Z
        @JsonProperty("uploaded_from")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        ZonedDateTime uploadedFrom,

        //Latest upload timestamp (inclusive). ISO‑8601.
        @JsonProperty("uploaded_to")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        ZonedDateTime uploadedTo,

        //Minimum duration, in milliseconds (>= 0).
        @Min(0)
        @JsonProperty("duration_min_ms")
        Long durationMinMs,

        //Maximum duration, in milliseconds (>= 1).
        @Min(1)
        @JsonProperty("duration_max_ms")
        Long durationMaxMs,

        //Case‑insensitive substring match on title. Max 200 chars.
        @Size(max = 200)
        @JsonProperty("title_contains")
        String titleContains,

        //Zero‑based page index.
        @Min(0)
        @JsonProperty("page")
        Integer page,

        //Page size.
        @Min(1)
        @JsonProperty("size")
        Integer size,

        /**
         * Sort specification, e.g. "uploadDateTime,desc" or "durationMillis,asc".
         * Will need to be parsed into a Spring Data Sort in your controller.
         */
        @Size(max = 100)
        @JsonProperty("sort")
        String sort

) {}
