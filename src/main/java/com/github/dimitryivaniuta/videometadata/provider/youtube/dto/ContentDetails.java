package com.github.dimitryivaniuta.videometadata.provider.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents the 'contentDetails' part of a YouTube playlistItem. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContentDetails(
        @JsonProperty("videoId") String videoId,
        @JsonProperty("videoPublishedAt") String videoPublishedAt
) {}
