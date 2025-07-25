package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for changing a user's password.
 *
 * @param password the new raw password, non‑blank
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangePasswordRequest(
        @JsonProperty("password")
        @NotBlank(message = "Password must not be blank")
        String password
) {
    @JsonCreator
    public ChangePasswordRequest { }
}