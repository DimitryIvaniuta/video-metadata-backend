package com.github.dimitryivaniuta.videometadata.domain.entity;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Entity representing metadata of a video.
 */
@Entity
@Table(name = "videos")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Video {

    /** Primary key. */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "VM_UNIQUE_ID"
    )
    @SequenceGenerator(
            name = "VM_UNIQUE_ID",
            sequenceName = "VM_UNIQUE_ID",
            allocationSize = 1
    )
    private Long id;

    /** Category / taxonomy for the video (ordinal persisted). */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private VideoCategory category = VideoCategory.UNSPECIFIED;

    /** User id (foreign or external reference) that created/imported this record. */
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    /** Video description. */
    @Column(nullable = false)
    private String description = "";

    /**
     * Duration of the video, stored as BIGINT (milliseconds) via converter.
     */
    @Column(nullable = false)
    private Duration duration;

    /** Origin provider (ordinal persisted). */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private VideoProvider provider = VideoProvider.UNSPECIFIED;

    /** External platform source (e.g., YouTube, Vimeo). */
    @Column(nullable = false)
    private String source;

    /** Title of the video. */
    @Column(nullable = false)
    private String title;

    /**
     * Exact publish timestamp with zone (stored as TIMESTAMPTZ in PostgreSQL).
     * Use UTC when source provides offset; retain offset if meaningful.
     */
    @Column(name = "upload_datetime", nullable = false)
    private ZonedDateTime uploadDateTime;
}
