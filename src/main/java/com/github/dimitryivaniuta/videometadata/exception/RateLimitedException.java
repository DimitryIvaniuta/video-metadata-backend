package com.github.dimitryivaniuta.videometadata.exception;

import com.github.dimitryivaniuta.videometadata.ratelimit.RateLimitResult;
import lombok.Getter;

/**
 * Thrown when a user hits the per-user import rate limit.
 */
@Getter
public class RateLimitedException extends RuntimeException {
    private final String user;
    private final RateLimitResult result;

    public RateLimitedException(String user, RateLimitResult result) {
        super("User '" + user + "' is rate limited. Remaining=" +
                result.remainingTokens() + ", resetsAt=" + result.resetAt());
        this.user = user;
        this.result = result;
    }
}