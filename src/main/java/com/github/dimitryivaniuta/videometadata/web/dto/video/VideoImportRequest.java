package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request to start an asynchronous import from an external provider.
 *
 * @param provider         Provider enum name (YOUTUBE, VIMEO, ...).
 * @param externalPlaylist Optional playlist / collection identifier for provider.
 * @param externalIds      Explicit list of external video IDs (if importing specific items).
 * @param force            If true, bypass duplicate skip logic (still respects uniqueness constraints).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoImportRequest(
        @Size(max = 50) @JsonProperty("provider") String provider,
        @Size(max = 200) @JsonProperty("external_playlist") String externalPlaylist,
        @JsonProperty("external_ids") Set<String> externalIds,
        @JsonProperty("force") Boolean force
) {}