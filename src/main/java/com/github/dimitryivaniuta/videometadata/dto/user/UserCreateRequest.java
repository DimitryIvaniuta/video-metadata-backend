package com.github.dimitryivaniuta.videometadata.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import java.util.Set;

/**
 * Request for creating a new user.
 *
 * @param username Unique username.
 * @param email    Optional email (if used for notifications / login).
 * @param password Raw password to encode server-side.
 * @param roles    Desired roles (ADMIN, USER, etc.). If null -> default USER.
 */
public record UserCreateRequest(
        @NotBlank @Size(max = 150) String username,
        @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 200) String password,
        Set<String> roles
) {}