package com.github.dimitryivaniuta.videometadata.util;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Generates compact, time‑ordered IDs: base64url( millis || 80 random bits ).
 *  - Sortable by creation time
 *  - No third‑party libs
 */
@Component
public class SubmissionIdTimeGenerator {

    private static final SecureRandom RNG = new SecureRandom();

    public String nextId() {
        long millis = Instant.now().toEpochMilli();
        byte[] random = new byte[10]; // 80 random bits
        RNG.nextBytes(random);

        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES + random.length);
        buf.putLong(millis).put(random);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf.array());
    }
}