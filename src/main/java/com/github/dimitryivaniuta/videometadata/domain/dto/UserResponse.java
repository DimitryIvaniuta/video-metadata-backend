package com.github.dimitryivaniuta.videometadata.domain.dto;

import com.github.dimitryivaniuta.videometadata.domain.entity.Role;

/**
 * Representation of a user returned by the API.
 *
 * @param id       database ID
 * @param role     assigned role
 * @param username login name
 */
public record UserResponse(
        Long id,
        Role role,
        String username
) {}