package com.github.dimitryivaniuta.videometadata.security.token;


import reactor.core.publisher.Mono;

/**
 * Issues and verifies JWT-based authentication tokens (access & refresh).
 */
public interface AuthTokenService {

    /**
     * Generates a new access/refresh token pair for the given principal.
     *
     * @param principal principal identifier (typically username or user ID)
     * @return mono with token pair
     */
    Mono<TokenPair> issueTokens(String principal);

    /**
     * Exchanges a valid refresh token for a new token pair.
     *
     * @param refreshToken incoming refresh token
     * @return mono with new token pair
     */
    Mono<TokenPair> refresh(String refreshToken);

    /**
     * Validates a raw JWT (access or refresh) and returns its claims if valid.
     *
     * @param token raw JWT
     * @return mono with parsed claims
     */
    Mono<TokenClaims> validate(String token);
}
