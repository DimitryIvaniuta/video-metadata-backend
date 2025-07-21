package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.validation.constraints.*;
import java.time.ZonedDateTime;

/**
 * Request payload for creating a new Video record.
 * <p>
 * Only ADMIN users may invoke the corresponding endpoint.
 * Fields are validated for presence and basic constraints.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoCreateRequest(

        /** The provider’s video ID (e.g. YouTube or Vimeo ID). */
        @NotBlank
        @Size(max = 100)
        @JsonProperty("externalId")
        String externalId,

        /** Which provider this video comes from. */
        @NotNull
        @JsonProperty("provider")
        VideoProvider provider,

        /** Human-readable title of the video. */
        @NotBlank
        @Size(max = 255)
        @JsonProperty("title")
        String title,

        /** Duration in milliseconds (>= 0). */
        @NotNull
        @Min(0)
        @JsonProperty("durationMillis")
        Long durationMillis,

        /** Upload timestamp in ISO‑8601 (with timezone). */
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @JsonProperty("uploadDateTime")
        ZonedDateTime uploadDateTime,

        /** Category of the video for internal classification. */
        @NotNull
        @JsonProperty("category")
        VideoCategory category
) {

    /**
     * Converts this DTO into a {@link Video} entity.
     * The {@code createdBy} field must be set separately by the caller,
     * typically using the current authenticated user.
     *
     * @return a new {@link Video} with all fields set except {@code id} and {@code createdBy}.
     */
    public Video toEntity() {
        return Video.builder()
                .externalId(this.externalId)
                .provider(this.provider)
                .title(this.title)
                .durationMillis(this.durationMillis)
                .uploadDateTime(this.uploadDateTime)
                .category(this.category)
                .build();
    }
}
