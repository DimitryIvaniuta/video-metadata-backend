package com.github.dimitryivaniuta.videometadata.security.token;

import java.time.Instant;

/** Immutable pair of issued tokens. */
public record TokenPair(String accessToken, String refreshToken, Instant accessExpiresAt, Instant refreshExpiresAt) {}
