package com.github.dimitryivaniuta.videometadata.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generates submission ids using RFC‑4122 random UUIDs (v4).
 * Backed by a DB UNIQUE constraint for absolute safety.
 */
@Component
public class SubmissionIdGenerator {

    /**
     * @return a new lowercase UUID without dashes, e.g. "3f9b6f0a2a9f4a0bbd2e7d6a0b4ce4d2"
     */
    public String nextId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}