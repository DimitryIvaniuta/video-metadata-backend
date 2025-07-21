package com.github.dimitryivaniuta.videometadata.web.dto.auth;

import java.time.Instant;

/**
 * Logout response acknowledging token revocation / session termination.
 *
 * @param message   Human-readable message.
 * @param timestamp Instant of logout processing.
 */
public record LogoutResponse(
        String message,
        Instant timestamp
) {}
