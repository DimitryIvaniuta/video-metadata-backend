package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.service.VideoImportService;
import com.github.dimitryivaniuta.videometadata.service.VideoService;
import com.github.dimitryivaniuta.videometadata.web.dto.VideoResponse;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Video metadata API.
 */
@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoImportService importService;
    private final VideoService       videoService;

    public VideoController(
            VideoImportService importService,
            VideoService videoService
    ) {
        this.importService = importService;
        this.videoService  = videoService;
    }

    /**
     * Trigger a background import of video metadata.
     * <p>Only admins may call this.</p>
     */
//    @PostMapping("/import")
//    @PreAuthorize("hasRole('ADMIN')")
//    public Mono<ResponseEntity<Void>> importVideos() {
//        return Mono.fromFuture(importService.importVideosAsync())
//                .map(v -> ResponseEntity.accepted().<Void>build());
//    }
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> triggerImport(
            @RequestParam(required = false) String youtubePlaylistId,
            @RequestParam(required = false) String vimeoUserId) {

        return authenticatedUserAccessor.currentUsername()
                .flatMap(userService::findByUsernameMono) // implement returning Mono<User>
                .map(user -> user.getId())
                .defaultIfEmpty(-1L)
                .flatMap(userId -> Mono.fromFuture(
                        importService.importVideosAsync(youtubePlaylistId, vimeoUserId, userId))
                )
                .thenReturn(ResponseEntity.accepted().build());
    }
    /**
     * List videos with optional filters, pagination & sorting.
     * <p>Users or admins may call this.</p>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Mono<ResponseEntity<Page<VideoResponse>>> listVideos(
            @RequestParam(required = false) String source,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate uploadDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate uploadDateTo,
            @RequestParam(required = false) Long minDuration,
            @RequestParam(required = false) Long maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadDate,desc") String[] sort
    ) {
        // Build Sort
        Sort sortObj = Sort.by(
                Sort.Order.by(sort[0].split(",")[0])
                        .with(Sort.Direction.fromString(sort[0].split(",")[1]))
        );
        Pageable pageable = PageRequest.of(page, size, sortObj);

        return videoService.findAll(
                        source, uploadDateFrom, uploadDateTo,
                        minDuration, maxDuration,
                        pageable
                )
                .map(ResponseEntity::ok);
    }

    /**
     * Fetch one video by its ID.
     * <p>Users or admins may call this.</p>
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Mono<ResponseEntity<VideoResponse>> getById(@PathVariable Long id) {
        return videoService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
