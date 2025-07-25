package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for updating a user's profile (username and/or email).
 *
 * @param username new username, non‑blank
 * @param email    new email (must be valid)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateProfileRequest(
        @JsonProperty("username")
        @NotBlank(message = "Username must not be blank")
        String username,

        @JsonProperty("email")
        @Email(message = "Email must be valid")
        String email
) {
    @JsonCreator
    public UpdateProfileRequest { }
}