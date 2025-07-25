package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;

import java.util.Set;

/**
 * Request body for replacing all roles of a user.
 *
 * @param roles the new set of roles (if null or empty, defaults to USER)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplaceRolesRequest(
        @JsonProperty("roles")
        Set<Role> roles
) {
    @JsonCreator
    public ReplaceRolesRequest { }
}