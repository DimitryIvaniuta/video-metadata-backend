package com.github.dimitryivaniuta.videometadata.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Login request payload carrying user credentials.
 *
 * @param username Non-blank username (case-sensitive).
 * @param password Non-blank raw password.
 */
public record LoginRequest(
        @NotBlank @Size(max = 150) @JsonProperty("username") String username,
        @NotBlank @Size(max = 200) @JsonProperty("password") String password
) {}
