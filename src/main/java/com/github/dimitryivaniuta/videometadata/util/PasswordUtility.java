package com.github.dimitryivaniuta.videometadata.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility for encoding raw passwords into BCrypt hashes and verifying them.
 * <p>
 * Provides two simple static methods:
 * <ul>
 *   <li>{@link #encodePassword(String)} – generates a BCrypt hash of a raw password.</li>
 *   <li>{@link #matches(String, String)} – checks a raw password against a stored BCrypt hash.</li>
 * </ul>
 * </p>
 */
@Slf4j
public final class PasswordUtility {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    // Prevent instantiation
    private PasswordUtility() { }

    /**
     * Encode a raw password into a BCrypt hash.
     *
     * @param rawPassword the plain‐text password
     * @return a BCrypt‐hashed representation suitable for storage
     */
    public static String encodePassword(final String rawPassword) {
        return ENCODER.encode(rawPassword);
    }


    /**
     * Verify a raw password against a stored BCrypt hash.
     *
     * @param rawPassword    the plain‐text password to verify
     * @param encodedPassword the stored BCrypt hash
     * @return {@code true} if the raw password matches the hash, {@code false} otherwise
     */
    public static boolean matches(final String rawPassword, final String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        String rawPassword = "userpass";
        String encodedPassword = encodePassword(rawPassword);
        log.info("Encoded password: {}", encodePassword(rawPassword));
        log.info("Matches password: {}", matches(rawPassword, encodedPassword));
    }
}
