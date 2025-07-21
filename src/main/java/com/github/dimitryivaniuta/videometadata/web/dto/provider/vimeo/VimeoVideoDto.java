package com.github.dimitryivaniuta.videometadata.web.dto.provider.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Minimal Vimeo video metadata DTO. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoVideoDto(
        @JsonProperty("uri") String uri,          // e.g. "/videos/123456789"
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("duration") Long durationSeconds,
        @JsonProperty("created_time") String createdTime
) {}
