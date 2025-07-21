package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.service.VideoService;
import com.github.dimitryivaniuta.videometadata.web.dto.video.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Reactive REST controller for Video CRUD, search, and statistics.
 */
@RestController
@RequestMapping("/videos")
@Validated
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * Create a new Video record manually.
     * ADMIN only.
     *
     * @param req the create request payload
     * @return 201 Created with Location header and body
     */
    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<VideoResponse>> create(
            @Valid @RequestBody VideoCreateRequest req
    ) {
        return videoService
                .saveMono(req.toEntity())
                .map(VideoResponse::from)
                .map(resp -> {
                    URI loc = URI.create("/videos/" + resp.id());
                    return ResponseEntity
                            .created(loc)
                            .body(resp);
                });
    }

    /**
     * Update an existing Video.
     * ADMIN only.
     *
     * @param id  the video ID
     * @param req fields to update
     * @return 200 OK with updated body, or 404 if not found
     */
    @PutMapping(
            path = "/{id}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<VideoResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody VideoUpdateRequest req
    ) {
        return videoService.findByIdMono(id)
                .flatMap(existing -> {
                    existing.setTitle(req.title());
                    existing.setDurationMillis(req.durationMillis());
                    existing.setUploadDateTime(req.uploadDateTime());
                    existing.setCategory(req.category());
                    // any other updatable fields
                    return videoService.saveMono(existing);
                })
                .map(VideoResponse::from)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Delete a Video by ID.
     * ADMIN only.
     *
     * @param id the video ID
     * @return 204 No Content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return videoService.findByIdMono(id)
                .flatMap(v -> videoService.deleteByIdMono(id)
                        .thenReturn(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * List videos with optional filter, pagination, and sorting.
     * Authenticated users.
     *
     * @param filter   filter criteria
     * @param pageable page & sort
     * @return flux of matching VideoResponse
     */
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Flux<VideoResponse> list(
            @Valid VideoFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return videoService.searchFlux(
                        filter.provider(),
                        filter.uploadDateFrom(),
                        filter.uploadDateTo(),
                        filter.minDurationMillis(),
                        filter.maxDurationMillis(),
                        filter.category(),
                        pageable
                )
                .map(VideoResponse::from);
    }

    /**
     * Fetch a single video by ID.
     * Authenticated users.
     *
     * @param id the video ID
     * @return 200 OK with body, or 404 if not found
     */
    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<VideoResponse>> getById(@PathVariable Long id) {
        return videoService.findByIdMono(id)
                .map(VideoResponse::from)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve video statistics (total count & average duration per provider).
     * Results should be cached.
     * Authenticated users.
     *
     * @return Mono of VideoStatsResponse
     */
    @GetMapping(path = "/stats", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Mono<VideoStatsResponse> stats() {
        return videoService.getStatistics();
    }
}

