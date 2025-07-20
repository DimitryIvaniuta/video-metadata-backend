package com.github.dimitryivaniuta.videometadata.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

/**
 * Video query filter request used in REST or GraphQL.
 *
 * All fields are optional. Null = ignore criterion.
 *
 * @param providers       Restrict to provider enum names (YOUTUBE, VIMEO, INTERNAL).
 * @param categories      Restrict to category enum names.
 * @param uploadedFrom    Inclusive lower bound upload timestamp.
 * @param uploadedTo      Exclusive upper bound upload timestamp.
 * @param durationMinMs   Minimum duration (ms).
 * @param durationMaxMs   Maximum duration (ms).
 * @param titleContains   Case-insensitive substring filter.
 * @param page            Zero-based page index (default 0).
 * @param size            Page size (default from config).
 * @param sort            Sort string (e.g., "uploadDateTime,DESC").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoFilterRequest(
        @JsonProperty("providers") Set<String> providers,
        @JsonProperty("categories") Set<String> categories,
        @JsonProperty("uploaded_from") Instant uploadedFrom,
        @JsonProperty("uploaded_to") Instant uploadedTo,
        @Min(0) @JsonProperty("duration_min_ms") Long durationMinMs,
        @Min(1) @JsonProperty("duration_max_ms") Long durationMaxMs,
        @Size(max = 200) @JsonProperty("title_contains") String titleContains,
        @Min(0) @JsonProperty("page") Integer page,
        @Min(1) @JsonProperty("size") Integer size,
        @Size(max = 100) @JsonProperty("sort") String sort
) {}