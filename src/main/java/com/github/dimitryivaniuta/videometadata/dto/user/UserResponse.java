package com.github.dimitryivaniuta.videometadata.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;

import java.time.Instant;
import java.util.Set;

/**
 * User response DTO returned to clients.
 *
 * @param id        Technical unique identifier.
 * @param username  Username.
 * @param email     Email (may be null).
 * @param roles     Granted roles (canonical form).
 * @param enabled   Active flag.
 * @param createdAt Creation timestamp.
 * @param updatedAt Last modification timestamp.
 */
public record UserResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("roles") Set<String> roles,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
    /**
     * Factory helper from entity.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleSet(), // assuming method returning Set<String>
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}