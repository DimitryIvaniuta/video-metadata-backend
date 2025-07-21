package com.github.dimitryivaniuta.videometadata.web.dto.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Rate limiting telemetry returned to callers.
 */
public record RateLimitMetadata(
        @JsonProperty("capacity") long capacity,
        @JsonProperty("remaining_tokens") long remainingTokens,
        @JsonProperty("refill_at") Instant refillAt,
        @JsonProperty("reset_after_ms") long resetAfterMillis,
        @JsonProperty("limited") boolean limited
) {}