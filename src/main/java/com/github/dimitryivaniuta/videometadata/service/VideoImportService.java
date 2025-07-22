package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportProgressResponse;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportSubmissionResponse;
import reactor.core.publisher.Mono;

/**
 * Reactive facade for kicking off and tracking video import jobs.
 *
 * <p>The actual heavy lifting is performed asynchronously by listeners/processors.
 * This service:
 * <ul>
 *   <li>Validates + rate-limits the request;</li>
 *   <li>Creates a submission entry with INITIAL status;</li>
 *   <li>Publishes an event to start import processing;</li>
 *   <li>Exposes read API to query current progress.</li>
 * </ul>
 */
public interface VideoImportService {

    /**
     * Schedules an asynchronous import job for the authenticated user.
     *
     * @param username current user's username (resolved reactively in controller)
     * @param request  provider, external ids/playlist, force flag etc.
     * @return Mono emitting the queued submission descriptor
     */
    Mono<VideoImportSubmissionResponse> startImport(String username, VideoImportRequest request);

    /**
     * Returns current progress / status by submission id.
     *
     * @param submissionId unique id returned by {@link #startImport(String, VideoImportRequest)}
     * @return Mono with current progress snapshot, or empty if unknown id
     */
    Mono<VideoImportProgressResponse> getProgress(String submissionId);
}