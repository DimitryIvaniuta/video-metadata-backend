package com.github.dimitryivaniuta.videometadata.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dimitryivaniuta.videometadata.domain.model.SubmissionStatus;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.domain.repository.projection.SubmissionExternalId;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reactive entity describing an asynchronous video import submission.
 * Mapped for Spring Data R2DBC.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
@Table("video_import_submissions")
public class VideoImportSubmission {

    @Id
    private Long id;

    /**
     * Public identifier returned to clients.
     */
    @Column("submission_id")
    private String submissionId;

    @Column("username")
    private String username;

    @Column("provider")
    private VideoProvider provider;

    @Column("forced")
    private boolean forced;

    @Column("status")
    private SubmissionStatus status;

    // Timestamps
    @Column("queued_at")
    private ZonedDateTime queuedAt;

    @Column("started_at")
    private ZonedDateTime startedAt;

    @Column("finished_at")
    private ZonedDateTime finishedAt;

    @Column("created_at")
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private ZonedDateTime updatedAt;

    @Version
    @Column("version")
    private Long version;

    // Playlist info
    @Column("external_playlist_id")
    private String externalPlaylistId;

    @Column("external_playlist_meta")
    private JsonNode externalPlaylistMeta; // JSONB

    // Statistics flattened (or use a converter if you prefer one column)
    @Column("total_requested")
    private int totalRequested;

    @Column("requested_count")
    private int requestedCount;

    @Column("accepted_count")
    private int acceptedCount;

    @Column("skipped_duplicates")
    private int skippedDuplicates;

    @Column("succeeded_count")
    private int succeededCount;

    @Column("failed_count")
    private int failedCount;

    @Column("error_message")
    private String errorMessage;

    /**
     * The set of external IDs requested. Stored in separate table.
     */
    @MappedCollection(idColumn = "submission_fk")
    @Builder.Default
    private Set<SubmissionExternalId> externalIds = new LinkedHashSet<>();

    /* Convenience to update stats */
    public void applyStats(ImportStatistics s) {
        this.requestedCount     = s.getRequestedCount();
        this.acceptedCount      = s.getAcceptedCount();
        this.skippedDuplicates  = s.getSkippedDuplicates();
        this.succeededCount     = s.getSucceededCount();
        this.failedCount        = s.getFailedCount();
    }
}
