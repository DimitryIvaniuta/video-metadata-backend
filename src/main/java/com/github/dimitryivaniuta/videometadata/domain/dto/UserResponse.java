package com.github.dimitryivaniuta.videometadata.domain.dto;

import com.github.dimitryivaniuta.videometadata.domain.entity.Role;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;

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
) {
    /**
     * Convert a JPA User entity into this DTO.
     *
     * @param u the User entity
     * @return the corresponding UserResponse
     */
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getRole(),
                u.getUsername()
        );
    }
}