package com.github.dimitryivaniuta.videometadata.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for generating a secure random JWT secret key.
 * <p>
 * Generates a 256‑bit (32‑byte) key suitable for HMAC‑SHA256,
 * and encodes it in Base64 for use as the JWT_SECRET environment variable.
 * </p>
 */
@Slf4j
public final class JwtSecretUtil {

    // 32 bytes * 8 = 256 bits
    private static final int SECRET_BYTE_LENGTH = 32;

    // SecureRandom for cryptographic strength
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Prevent instantiation
    private JwtSecretUtil() { }

    /**
     * Generates a new random 256‑bit key and returns it as a Base64‑encoded string.
     *
     * @return Base64‑encoded 256‑bit secret
     */
    public static String generateBase64Secret() {
        byte[] secret = new byte[SECRET_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    public static void main(String[] args) {
        log.info("Secret key: {}", JwtSecretUtil.generateBase64Secret());
    }
}
