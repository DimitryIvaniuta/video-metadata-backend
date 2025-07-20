package com.github.dimitryivaniuta.videometadata.rate;

import java.time.Instant;

/**
 * Returned alongside responses that are influenced by rate limiting.
 *
 * @param capacity         Total bucket capacity.
 * @param remainingTokens  Tokens left after this operation.
 * @param refillAt         Instant when bucket will be refilled next (approximation).
 * @param resetAfterMillis Milliseconds until refill (client convenience).
 * @param limited          Whether the current request was limited (false if just informational).
 */
public record RateLimitMetadata(
        long capacity,
        long remainingTokens,
        Instant refillAt,
        long resetAfterMillis,
        boolean limited
) {}