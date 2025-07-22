package com.github.dimitryivaniuta.videometadata.ratelimit;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("capacity") long capacity,
        @JsonProperty("remaining_tokens") long remainingTokens,
        @JsonProperty("refill_at") Instant refillAt,
        @JsonProperty("reset_after_ms") long resetAfterMillis,
        @JsonProperty("limited") boolean limited
) {
}

