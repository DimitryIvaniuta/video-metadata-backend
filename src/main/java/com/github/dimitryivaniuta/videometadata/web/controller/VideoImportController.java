package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import com.github.dimitryivaniuta.videometadata.service.VideoImportService;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportSubmissionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;

/**
 * REST controller exposing endpoints to trigger asynchronous video metadata imports.
 * <p>
 * Protected by ROLE_ADMIN—only administrators may invoke imports.
 */
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoImportController {

    /**
     * Service responsible for scheduling and performing background imports.
     */
    private final VideoImportService importService;

    /**
     * POST /api/videos/import
     * <p>
     * Schedules import of metadata for the given external video IDs.
     * Returns 202 Accepted immediately; actual work happens asynchronously.
     *
     * @param principal the currently authenticated user (must have ADMIN role)
     * @param request   provider + list of external video IDs to import
     * @return 202 Accepted when import is scheduled
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> importVideos(Principal principal,
                                                   @RequestBody VideoImportRequest request) {
        importService.submitImport(principal.getName(), request);
        return Mono.just(ResponseEntity.accepted().build());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<VideoImportSubmissionResponse>> importVideos(
            @Valid @RequestBody VideoImportRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        return importService.importVideos(request)
                .map(sub -> {
                    var location = uriBuilder
                            .path("/api/videos/import/{id}")
                            .buildAndExpand(sub.submissionId())
                            .toUri();

                    return ResponseEntity
                            .accepted()
                            .location(location)
                            .body(sub);
                });
    }
}
