package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

/**
 * Request payload for updating an existing Video record.
 * <p>
 * Only ADMIN users may invoke the corresponding endpoint.
 * Fields here replace the existing values on the Video entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoUpdateRequest(

        /** New human-readable title (max 255 chars). */
        @NotBlank
        @Size(max = 255)
        @JsonProperty("title")
        String title,

        /** New duration in milliseconds (>= 0). */
        @NotNull
        @Min(0)
        @JsonProperty("durationMillis")
        Long durationMillis,

        /** New upload timestamp in ISO‑8601 format (with timezone). */
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @JsonProperty("uploadDateTime")
        ZonedDateTime uploadDateTime,

        /** New internal category for classification. */
        @NotNull
        @JsonProperty("category")
        VideoCategory category

) {

    /**
     * Applies the fields of this update request onto an existing Video entity.
     * The supplied Video’s {@code id}, {@code externalId}, {@code provider},
     * and {@code createdBy} remain unchanged.
     *
     * @param existing the Video entity to update
     * @return the same {@code existing} instance with its fields updated
     */
    public Video applyTo(Video existing) {
        existing.setTitle(this.title);
        existing.setDurationMillis(this.durationMillis);
        existing.setUploadDateTime(this.uploadDateTime);
        existing.setCategory(this.category);
        return existing;
    }
}
