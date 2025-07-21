package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request for updating fields of an existing user. Any nullable component is ignored if null.
 *
 * @param email    New email (optional).
 * @param password New raw password (optional).
 * @param roles    New roles (optional).
 * @param enabled  Toggle active flag (optional).
 */
public record UserUpdateRequest(
        @Email @Size(max = 255) @JsonProperty("email") String email,
        @Size(min = 8, max = 200) @JsonProperty("password") String password,
        @JsonProperty("roles") Set<String> roles,
        @JsonProperty("enabled") Boolean enabled
) {}