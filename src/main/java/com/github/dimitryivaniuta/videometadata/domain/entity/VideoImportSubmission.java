package com.github.dimitryivaniuta.videometadata.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import com.github.dimitryivaniuta.videometadata.domain.model.SubmissionStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Persistent record describing an asynchronous video import submission.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_import_submissions",
        indexes = {
                @Index(name = "idx_video_import_submissions_username", columnList = "username"),
                @Index(name = "idx_video_import_submissions_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_video_import_submissions_submission_id", columnNames = "submission_id")
        })
@DynamicUpdate
public class VideoImportSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_UNIQUE_ID")
    @SequenceGenerator(name = "VM_UNIQUE_ID", sequenceName = "VM_UNIQUE_ID", allocationSize = 10)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "error_message")
    private String errorMessage;

    /** New optional playlist identifier from the external provider. */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "external_playlist_id", length = 255)
    private String externalPlaylistId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_playlist_meta", columnDefinition = "jsonb")
    private JsonNode externalPlaylistMeta;   // com.fasterxml.jackson.databind.JsonNode

    @Column(name = "finished_at")
    private ZonedDateTime finishedAt;

    @Column(name = "forced", nullable = false)
    private boolean forced;

    /**
     * All external video identifiers the client asked to import.
     * Stored in a separate collection table.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "video_import_submission_external_ids",
            joinColumns = @JoinColumn(name = "submission_fk")
    )
    @Column(name = "external_id", length = 200, nullable = false)
    @OrderColumn(name = "ord") // optional: preserve original order
    private Set<String> externalIds = new LinkedHashSet<>();

    @Column(name = "provider", nullable = false, length = 50)
    private VideoProvider provider;

    @Column(name = "queued_at", nullable = false, updatable = false)
    private ZonedDateTime queuedAt;

    @Column(name = "started_at")
    private ZonedDateTime startedAt;

    /** Grouped counters. */
    @Embedded
    private ImportStatistics statistics;

    /**
     * Stored as ordinal (smallint). Beware of reordering enum constants!
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status;

    @Column(name = "submission_id", nullable = false, length = 64, updatable = false)
    private String submissionId;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "username", nullable = false, length = 150)
    private String username;

    @PrePersist
    void prePersist() {
        ZonedDateTime now = ZonedDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = ZonedDateTime.now();
    }

}
