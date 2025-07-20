package com.github.dimitryivaniuta.videometadata.domain.model;

/**
 * Video category taxonomy.
 * NOTE: Persisted by ORDINAL. **Never reorder or insert in the middle.**
 * Append new values ONLY at the end to preserve database correctness.
 */
public enum VideoCategory {
    EDUCATION,
    ENTERTAINMENT,
    MUSIC,
    SPORTS,
    NEWS,
    TECHNOLOGY,
    GAMING,
    BUSINESS,
    OTHER,
    UNSPECIFIED
}