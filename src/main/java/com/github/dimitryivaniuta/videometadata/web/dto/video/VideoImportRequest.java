package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Collections;
import java.util.Set;

/**
 * Request payload for triggering a background metadata import from an external video provider.
 *
 * <p>Typical use cases:
 * <ul>
 *   <li>Import a list of concrete external video IDs.</li>
 *   <li>Import an entire external playlist/channel (provider-specific).</li>
 *   <li>Force re-import even if duplicates are detected (optional).</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>{@code provider} is required and must be one of {@link VideoProvider}.</li>
 *   <li>You must provide either {@code externalIds} or {@code externalPlaylist} (or both),
 *       depending on how your provider client is implemented.</li>
 *   <li>{@code force} defaults to {@code false} if omitted.</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record VideoImportRequest(

        /**
         * The external provider to pull metadata from (e.g. YOUTUBE, VIMEO, INTERNAL).
         */
        @NotNull
        @JsonProperty("provider")
        VideoProvider provider,

        /**
         * Optional provider-specific playlist/channel identifier.
         * Example: YouTube playlist ID or Vimeo folder/path.
         */
        @Size(max = 200)
        @JsonProperty("external_playlist_id")
        String externalPlaylistId,

        @JsonProperty("playlist_meta")
        JsonNode playlistMeta,

        /**
         * A set of explicit external video IDs to import.
         * Must not be empty if no playlist is provided.
         */
        @JsonProperty("external_ids")
        @Size(max = 500)
        Set<String> externalIds,

        /**
         * If true, skip duplicate checks and forcibly re-import.
         * Default: false.
         */
        @JsonProperty("force")
        Boolean force
) {

    /**
     * Safe accessor that never returns {@code null}; defaults to {@code false}.
     */
    public boolean forced() {
        return force != null && force;
    }

    /**
     * Returns a non-null, possibly empty set of IDs to simplify downstream logic.
     */
    public Set<String> safeExternalIds() {
        return externalIds == null ? Collections.emptySet() : externalIds;
    }
}
