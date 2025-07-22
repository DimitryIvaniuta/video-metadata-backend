package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import com.github.dimitryivaniuta.videometadata.service.VideoImportService;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportSubmissionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
     * Triggers an asynchronous import job. Only ADMINs may call it.
     * Returns 202 Accepted and a body describing the queued job.
     * POST /api/videos/import
     * <p>
     * Schedules import of metadata for the given external video IDs.
     * Returns 202 Accepted immediately; actual work happens asynchronously.
     *
     * @param request   provider + list of external video IDs to import
     * @return 202 Accepted when import is scheduled
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<VideoImportSubmissionResponse>> importVideos(
            @Valid @RequestBody VideoImportRequest request,
            ServerHttpRequest httpRequest) {

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .switchIfEmpty(Mono.error(new IllegalStateException("No authenticated principal found")))
                .flatMap(username -> importService.startImport(username, request))
                .map(submission -> {
                    URI location = URI.create(httpRequest.getPath().contextPath().value()
                            + "/videos/import/" + submission.submissionId());
                    return ResponseEntity
                            .accepted()
                            .location(location)
                            .body(submission);
                });
    }
}
