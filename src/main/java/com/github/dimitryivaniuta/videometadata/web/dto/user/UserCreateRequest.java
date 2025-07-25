package com.github.dimitryivaniuta.videometadata.web.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

/**
 * Request for creating a new user.
 *
 * @param username Unique username.
 * @param email    Optional email (if used for notifications / login).
 * @param password Raw password to encode server-side.
 * @param roles    Desired roles (ADMIN, USER, etc.). If null -> default USER.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCreateRequest(
        @JsonProperty("username")
        @NotBlank(message = "Username must not be blank")
        String username,

        @JsonProperty("email")
        @Email(message = "Email must be valid")
        String email,

        @JsonProperty("password")
        @NotBlank(message = "Password must not be blank")
        String password,

        @JsonProperty("roles")
        Set<Role> roles
) {
    @JsonCreator
    public UserCreateRequest { }
}