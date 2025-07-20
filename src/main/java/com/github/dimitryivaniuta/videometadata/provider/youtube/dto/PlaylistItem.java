package com.github.dimitryivaniuta.videometadata.provider.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Single playlist item combining snippet & content details. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaylistItem(
        @JsonProperty("snippet") Snippet snippet,
        @JsonProperty("contentDetails") ContentDetails contentDetails
) {}
