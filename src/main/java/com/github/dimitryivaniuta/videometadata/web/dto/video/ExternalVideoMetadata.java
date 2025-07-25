package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import lombok.extern.jackson.Jacksonized;
import lombok.Builder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;

/**
 * Immutable external video metadata fetched from a provider.
 *
 * <p>Use the {@link #builder()} to create instances, and map directly
 * to your domain {@link Video} via {@link #mapExternalToVideo(Long)} or update
 * an existing {@link Video} via {@link #applyMetadata(Video)}.</p>
 *
 * @param externalId   provider‑specific video ID
 * @param provider     which {@link VideoProvider} provided this data
 * @param title        video title
 * @param description  video description
 * @param duration     video duration
 * @param publishedAt  publication timestamp
 * @param channelName  uploader or channel name
 * @param thumbnailUrl URL of the thumbnail image
 * @param raw          raw response map (optional, for debugging/audit)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Jacksonized
public record ExternalVideoMetadata(
        String externalId,
        VideoProvider provider,
        String title,
        String description,
        Duration duration,
        Instant publishedAt,
        String channelName,
        String thumbnailUrl,
        Map<String, Object> raw
) {

    /**
     * Creates a new Video entity from external metadata.
     *
     * @param ownerId internal user ID who owns the video
     * @return populated Video (with auditing/import timestamps initialized)
     */
    public Video mapExternalToVideo(Long ownerId) {
        // Convert Instant → ZonedDateTime (using system default; adjust if you have a specific zone)
        ZonedDateTime uploadTime = ZonedDateTime.ofInstant(this.publishedAt(), ZoneId.systemDefault());

        Video v = Video.builder()
                .createdByUserId(ownerId)
                .externalVideoId(this.externalId())      // provider-specific ID
                .externalId(this.externalId())           // your own externalId field; adjust if you want a different value
                .provider(VideoProvider.valueOf(this.provider().name()))
                .title(this.title())
                .description(this.description())
                .durationMillis(this.duration().toMillis())
                .uploadDateTime(uploadTime)
                .importedAt(Instant.now())
                // default category; choose an appropriate enum or pass in as a parameter
                .category(VideoCategory.GENERAL)
                .build();

        // Ensure createdAt/updatedAt/importedAt are non-null for auditing
        v.initTimestampsIfMissing();
        return v;
    }

    /**
     * Applies fresh external metadata onto an existing Video entity.
     *
     * @param existing the persisted Video to update
     */
    public void applyMetadata(Video existing) {
        // Verify it’s the same video
        if (!this.externalId().equals(existing.getExternalVideoId())) {
            throw new IllegalArgumentException("Metadata ID mismatch: "
                    + this.externalId() + " vs " + existing.getExternalVideoId());
        }

        existing.setTitle(this.title());
        existing.setDescription(this.description());
        existing.setDuration(this.duration()); // uses the convenience setter
        existing.setUploadDateTime(ZonedDateTime.ofInstant(this.publishedAt(), ZoneId.systemDefault()));
        // provider/externalVideoId/externalId shouldn’t change

        // Touch the updatedAt timestamp
        existing.setUpdatedAt(Instant.now());
    }
}
