package com.github.dimitryivaniuta.videometadata.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Immutable-like value object that groups all numeric progress counters
 * for a video import submission.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportStatistics {

    @Column(name = "total_requested", nullable = false)
    private int totalRequested;

    /** Total external IDs the client asked to import. */
    @Column(name = "requested_count", nullable = false)
    private int requestedCount = 0;

    /** How many were accepted for processing (after basic validation). */
    @Column(name = "accepted_count", nullable = false)
    private int acceptedCount = 0;

    /** Skipped because they already exist (duplicates). */
    @Column(name = "skipped_duplicates", nullable = false)
    private int skippedDuplicates = 0;

    /** Successfully persisted videos. */
    @Column(name = "succeeded_count", nullable = false)
    private int succeededCount = 0;

    /** Failed imports. */
    @Column(name = "failed_count", nullable = false)
    private int failedCount = 0;
}
