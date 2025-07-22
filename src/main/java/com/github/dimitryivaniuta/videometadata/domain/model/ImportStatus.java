package com.github.dimitryivaniuta.videometadata.domain.model;

/**
 * Lifecycle of a video import submission.
 */
public enum ImportStatus {
    QUEUED,
    RUNNING,
    PARTIAL_SUCCESS,
    COMPLETED,
    FAILED
}