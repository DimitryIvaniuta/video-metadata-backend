package com.github.dimitryivaniuta.videometadata.security.token;

/**
 * Unchecked wrapper for JWT-related errors.
 */
public class AuthTokenException extends RuntimeException {
    public AuthTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    public AuthTokenException(String message) {
        super(message);
    }
}