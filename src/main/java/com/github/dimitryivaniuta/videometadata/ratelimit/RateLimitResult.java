package com.github.dimitryivaniuta.videometadata.ratelimit;

import java.time.Instant;

/**
 * Metadata returned by the limiter after each attempt.
 *
 * @param allowed          true if the request was accepted
 * @param remainingTokens  remaining tokens after this call
 * @param resetAt          instant when the bucket will be full again
 */
public record RateLimitResult(boolean allowed, long remainingTokens, Instant resetAt) { }