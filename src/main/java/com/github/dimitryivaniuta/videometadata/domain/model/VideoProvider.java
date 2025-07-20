package com.github.dimitryivaniuta.videometadata.domain.model;

/**
 * Video provider enumeration.
 * NOTE: Persisted by ORDINAL. **Never reorder or insert in the middle.**
 * Append new values ONLY at the end.
 */
public enum VideoProvider {
    YOUTUBE,
    VIMEO,
    INTERNAL,
    OTHER,
    UNSPECIFIED
}