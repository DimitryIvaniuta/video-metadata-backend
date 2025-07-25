package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import lombok.Builder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * User response DTO returned to clients.
 *
 * @param id        Technical unique identifier.
 * @param username  Username.
 * @param email     Email (perhaps null).
 * @param roles     Granted roles (canonical form).
 * @param enabled   Active flag.
 * @param createdAt Creation timestamp.
 * @param updatedAt Last modification timestamp.
 */
@Builder
public record UserResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("roles") Set<Role> roles,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("lastLoginAt") Instant lastLoginAt
) {
    /**
     * Factory helper from entity.
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(new HashSet<>(user.getRoles()))
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}