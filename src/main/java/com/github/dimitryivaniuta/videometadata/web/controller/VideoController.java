package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.service.VideoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Reactive REST controller for video metadata.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/videos", produces = "application/json")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * Create a new video by fetching metadata from an external provider.
     * ADMIN only.
     */
    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<VideoResponse>> create(
            @Valid @RequestBody CreateVideoRequest req
    ) {
        return videoService.createVideo(req.getProvider(), req.getExternalVideoId())
                .map(VideoResponse::from)
                .map(resp -> {
                    URI loc = URI.create("/api/videos/" + resp.id());
                    return ResponseEntity.created(loc).body(resp);
                });
    }

    /**
     * Refresh metadata for an existing video.
     * ADMIN only.
     */
    @PatchMapping(path = "/{id}/metadata")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<VideoResponse>> refreshMetadata(@PathVariable Long id) {
        return videoService.updateMetadata(id)
                .map(VideoResponse::from)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Delete a video.
     * ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return videoService.delete(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    /**
     * List videos filtered by title substring, with offset/limit pagination.
     * Authenticated users.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Flux<VideoResponse> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") @PositiveOrZero int offset,
            @RequestParam(defaultValue = "20") @Positive int limit
    ) {
        int cap = Math.min(limit, 200);
        return videoService.searchByTitle(q, offset, cap)
                .map(VideoResponse::from);
    }

    /**
     * Get a single video by ID.
     * Authenticated users.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<VideoResponse>> getById(@PathVariable Long id) {
        return videoService.getById(id)
                .map(VideoResponse::from)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Video statistics: count & average duration by provider.
     * Authenticated users.
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public Mono<VideoStatsResponse> stats() {
        return videoService.getStatistics();
    }

    /*==========================================================
      DTOs
     ==========================================================*/

    @Value
    public static class CreateVideoRequest {
        @Positive(message = "ownerId must be positive")
        Long ownerId;

        VideoProvider provider;

        @NotBlank(message = "externalVideoId must not be blank")
        String externalVideoId;
    }

    @Value
    public static class VideoResponse {
        Long id;
        String title;
        String description;
        long durationMillis;
        Instant uploadDateTime;
        String externalVideoId;
        VideoProvider provider;
        Instant createdAt;
        Instant updatedAt;

        public static VideoResponse from(Video v) {
            return new VideoResponse(
                    v.getId(),
                    v.getTitle(),
                    v.getDescription(),
                    v.getDuration().toMillis(),
                    v.getUploadDateTime().toInstant(),
                    v.getExternalVideoId(),
                    VideoProvider.valueOf(v.getProvider().name()),
                    v.getCreatedAt(),
                    v.getUpdatedAt()
            );
        }
    }

    @Value
    public static class ProviderStat {
        VideoProvider provider;
        long count;
        double avgDurationMillis;
    }

    @Value
    public static class VideoStatsResponse {
        long totalVideos;
        List<ProviderStat> byProvider;

        public static VideoStatsResponse of(long total, List<ProviderStat> list) {
            return new VideoStatsResponse(total, list);
        }
    }
}
