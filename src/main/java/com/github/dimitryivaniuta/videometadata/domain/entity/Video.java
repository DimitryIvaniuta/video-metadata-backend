package com.github.dimitryivaniuta.videometadata.domain.entity;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Entity representing a single video metadata record ingested from external or internal providers.
 * <p>
 * Persistence & Domain Rules:
 * <ul>
 *   <li>Primary key generated from a shared sequence {@code VM_UNIQUE_ID} (Flyway-defined).</li>
 *   <li>Uniqueness enforced on {@code (provider, external_video_id)} to prevent duplicate imports.</li>
 *   <li>Duration stored as milliseconds (BIGINT) for efficient numeric aggregation.</li>
 *   <li>Upload date/time preserved with time zone in {@code uploadDateTime}.</li>
 *   <li>Provider & category persisted as ordinals (int) per specification (see caution below).</li>
 *   <li>Lazy relation to the {@link User} who triggered import (may be {@code null} for system imports).</li>
 * </ul>
 * <b>Caution:</b> Ordinal enum persistence is brittle if enum declaration order changes. Consider switching
 * to {@link EnumType#STRING} if schema evolvability outweighs minimal storage concerns.
 */
@Entity
@Table(
        name = "videos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_video_provider_external_id",
                        columnNames = {"provider", "external_video_id"})
        },
        indexes = {
                @Index(name = "idx_videos_provider", columnList = "provider"),
                @Index(name = "idx_videos_upload_date_time", columnList = "upload_date_time"),
                @Index(name = "idx_videos_category", columnList = "category")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = {"createdBy"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {


    /**
     * Surrogate primary key generated from sequence {@code VM_UNIQUE_ID}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_UNIQUE_ID")
    @SequenceGenerator(
            name = "VM_UNIQUE_ID",
            sequenceName = "VM_UNIQUE_ID",
            allocationSize = 10
    )
    private Long id;

    /**
     * Video category (ordinal persisted).
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "category", nullable = false)
    @NotNull
    private VideoCategory category;

    /**
     * The external‐provider’s video identifier.
     */
    @Column(name = "external_id", nullable = false, length = 100)
    private String externalId;

    /**
     * User who initiated import / creation (nullable, lazy).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id",
            foreignKey = @ForeignKey(name = "fk_videos_created_by_user"))
    private User createdBy;

    /**
     * Human-readable description (nullable, large).
     */
    @Lob
    @Column(name = "description")
    private String description;

    /**
     * Duration in milliseconds (non-negative).
     */
    @Column(name = "duration_ms", nullable = false)
    private long durationMillis;

    /**
     * External provider-specific video identifier (nullable for INTERNAL provider).
     */
    @Column(name = "external_video_id", length = 255)
    private String externalVideoId;

    /**
     * Instant when the video metadata was imported into the system (set by service layer).
     */
    @Column(name = "imported_at", nullable = false, updatable = false)
    private Instant importedAt;

    /**
     * Provider enum (ordinal persisted): YOUTUBE, VIMEO, INTERNAL, etc.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "provider", nullable = false)
    @NotNull
    private VideoProvider provider;

    /**
     * Original upload date/time with zone (provider normalized to UTC zone or provided zone).
     */
    @Column(name = "upload_date_time",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @NotNull
    private ZonedDateTime uploadDateTime;

    /**
     * Title (required).
     */
    @Column(name = "title", nullable = false, length = 255)
    @NotBlank
    private String title;

    /* ======================================================================
       Auditing Fields
       ====================================================================== */

    /**
     * Creation timestamp (automatic by Spring Data JPA auditing).
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last update timestamp (automatic by Spring Data JPA auditing).
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /* ======================================================================
       Convenience Methods
       ====================================================================== */

    /**
     * @return Duration view of {@link #durationMillis}.
     */
    public Duration getDuration() {
        return Duration.ofMillis(durationMillis);
    }

    /**
     * Sets duration from a {@link Duration}, null interpreted as zero.
     *
     * @param duration duration to apply; if null sets 0 ms.
     */
    public void setDuration(Duration duration) {
        this.durationMillis = (duration == null) ? 0L : duration.toMillis();
    }

    /**
     * Initializes system-managed timestamps if not already set. Call from service layer
     * on initial persist if auditing is not yet triggered (defensive).
     */
    public void initTimestampsIfNecessary() {
        Instant now = Instant.now();
        if (importedAt == null) {
            importedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /* ======================================================================
       Equality / HashCode (by id)
       ====================================================================== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> effectiveClass = (this instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : getClass();
        Class<?> otherEffectiveClass = (o instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        if (effectiveClass != otherEffectiveClass) return false;
        Video other = (Video) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return (this instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
