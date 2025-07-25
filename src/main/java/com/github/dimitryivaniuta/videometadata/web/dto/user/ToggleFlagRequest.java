package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic request body for toggling a boolean flag on a user
 * (e.g. enabled or locked).
 *
 * @param value new boolean value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ToggleFlagRequest(
        @JsonProperty("value")
        boolean value
) {
    @JsonCreator
    public ToggleFlagRequest { }
}