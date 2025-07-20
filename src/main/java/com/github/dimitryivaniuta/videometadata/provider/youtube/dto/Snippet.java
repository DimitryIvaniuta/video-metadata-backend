package com.github.dimitryivaniuta.videometadata.provider.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents 'snippet' part: title, description, publishedAt. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Snippet(
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("publishedAt") String publishedAt
) {}
