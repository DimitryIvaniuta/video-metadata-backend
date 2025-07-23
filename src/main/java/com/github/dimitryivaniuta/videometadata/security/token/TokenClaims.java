package com.github.dimitryivaniuta.videometadata.security.token;

import java.time.Instant;
import java.util.Map;

/** Basic view of validated JWT claims. */
public record TokenClaims(String subject, Instant issuedAt, Instant expiresAt, Map<String, Object> additional) {}
