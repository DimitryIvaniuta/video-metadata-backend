package com.github.dimitryivaniuta.videometadata.web.dto;


import com.github.dimitryivaniuta.videometadata.domain.model.Role;

/**
 * Payload for creating or updating a user.
 *
 * @param username login name, must be unique
 * @param password plain‐text password (will be BCrypt‐hashed)
 * @param role     one of {@link Role}
 */
public record UserRequest(
        String username,
        String password,
        Role role
) {}
