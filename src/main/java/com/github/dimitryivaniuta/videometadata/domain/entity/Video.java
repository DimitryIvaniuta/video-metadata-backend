package com.github.dimitryivaniuta.videometadata.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity representing metadata of a video.
 */
@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_UNIQUE_ID")
    @SequenceGenerator(
            name           = "VM_UNIQUE_ID",
            sequenceName   = "VM_UNIQUE_ID",
            allocationSize = 1
    )
    private Long id;

    /** Title of the video. */
    private String title;

    /** External platform source (e.g., YouTube, Vimeo). */
    private String source;

    /**
     * Duration of the video, stored as BIGINT (milliseconds) via converter.
     */
    @Column(nullable = false)
    private Duration duration;

    /** Timestamp when the video was uploaded. */
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;
}
