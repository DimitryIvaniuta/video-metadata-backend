package com.github.dimitryivaniuta.videometadata.provider.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageInfo(
        @JsonProperty("totalResults") Integer totalResults,
        @JsonProperty("resultsPerPage") Integer resultsPerPage
) {}
