package com.github.dimitryivaniuta.videometadata.provider.vimeo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Wraps list results from Vimeo API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoResponseWrapper(
        @JsonProperty("data") List<VimeoVideoDto> data,
        @JsonProperty("total") Integer total
) {}
