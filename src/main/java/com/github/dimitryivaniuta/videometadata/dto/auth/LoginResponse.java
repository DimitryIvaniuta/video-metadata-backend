package com.github.dimitryivaniuta.videometadata.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Successful login response containing issued JWT token and metadata.
 *
 * @param token            Access JWT (Bearer token) – HS256 signed.
 * @param expiresAt        Epoch instant when the access token expires.
 * @param refreshToken     Optional refresh token (if implemented).
 * @param refreshExpiresAt Expiration of refresh token (if provided).
 * @param username         Authenticated username.
 * @param roles            Comma separated roles granted (e.g. "ROLE_ADMIN,ROLE_USER").
 */
public record LoginResponse(
        @JsonProperty("access_token") String token,
        @JsonProperty("expires_at") Instant expiresAt,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("refresh_expires_at") Instant refreshExpiresAt,
        @JsonProperty("username") String username,
        @JsonProperty("roles") String roles
) {}