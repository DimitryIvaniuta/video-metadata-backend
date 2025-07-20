package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoCategory;
import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import com.github.dimitryivaniuta.videometadata.imports.ExternalIdDuplicateCache;
import com.github.dimitryivaniuta.videometadata.imports.ImportJobKey;
import com.github.dimitryivaniuta.videometadata.imports.ImportMetrics;
import com.github.dimitryivaniuta.videometadata.imports.RedisDistributedLock;
import com.github.dimitryivaniuta.videometadata.imports.RedisRateLimiter;
import com.github.dimitryivaniuta.videometadata.provider.ExternalVideoClient;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates video imports from external providers.
 * Handles rate limiting, duplicate import prevention, distributed
 * locking, resilience, metrics and asynchronous execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoImportService {

    // Alphabetically sorted dependencies / internal fields
    private final ExternalIdDuplicateCache duplicateCache;
    private final Map<VideoProvider, ExternalVideoClient> providerClients; // injected via config (Map bean)
    private final RedisDistributedLock distributedLock;
    private final RedisRateLimiter rateLimiter;
    private final com.github.dimitryivaniuta.videometadata.service.impl.VideoService videoService;
    private final ImportMetrics metrics;

    /** In‑memory job guard to reject simultaneous identical jobs quickly (local node). */
    private final Set<ImportJobKey> localActiveJobs = ConcurrentHashMap.newKeySet();

    /**
     * Submission response describing outcome.
     */
    @Getter
    @Builder
    public static class ImportSubmissionResponse {
        private final boolean accepted;
        private final String  message;
    }

    /**
     * Submit an import job for selected providers. Null parameters
     * for identifiers are allowed; provider clients can interpret.
     *
     * @param youtubePlaylistId optional playlist id
     * @param vimeoUserId optional vimeo user or channel id
     * @param requestedByUserId user initiating import
     * @return submission response
     */
    public ImportSubmissionResponse submitImport(String youtubePlaylistId,
                                                 String vimeoUserId,
                                                 Long requestedByUserId) {

        // Basic per-user + global rate limiting
        if (!rateLimiter.tryConsumeGlobal(1)) {
            return ImportSubmissionResponse.builder()
                    .accepted(false)
                    .message("Global import rate limit exceeded")
                    .build();
        }
        if (!rateLimiter.tryConsumeUser(requestedByUserId.toString(), 1)) {
            return ImportSubmissionResponse.builder()
                    .accepted(false)
                    .message("User import rate limit exceeded")
                    .build();
        }

        // Compose a job key
        ImportJobKey key = ImportJobKey.of(
                Optional.ofNullable(youtubePlaylistId).orElse(""),
                Optional.ofNullable(vimeoUserId).orElse(""),
                requestedByUserId
        );

        if (!localActiveJobs.add(key)) {
            return ImportSubmissionResponse.builder()
                    .accepted(false)
                    .message("Similar import already running on this node")
                    .build();
        }

        // Acquire distributed lock (to stop other nodes)
        String lockName = "import:" + key.hash();
        if (!distributedLock.tryLock(lockName)) {
            localActiveJobs.remove(key);
            return ImportSubmissionResponse.builder()
                    .accepted(false)
                    .message("Import already running cluster-wide")
                    .build();
        }

        // Async fire‑and‑forget (errors recorded in metrics + logs)
        startAsyncImport(key, youtubePlaylistId, vimeoUserId, requestedByUserId, lockName);
        return ImportSubmissionResponse.builder()
                .accepted(true)
                .message("Import accepted and running asynchronously")
                .build();
    }

    /**
     * Initiates asynchronous import logic.
     */
    @Async("taskExecutor")
    @Retry(name = "videoImportRetry", fallbackMethod = "retryFallback")
    public void startAsyncImport(ImportJobKey key,
                                 String youtubePlaylistId,
                                 String vimeoUserId,
                                 Long requestedByUserId,
                                 String lockName) {

        long startNs = System.nanoTime();
        int imported = 0;
        int skipped = 0;

        try {
            // YOUTUBE
            if (youtubePlaylistId != null && !youtubePlaylistId.isBlank()) {
                imported += importFromProvider(VideoProvider.YOUTUBE,
                        Map.of("playlistId", youtubePlaylistId),
                        requestedByUserId);
            }

            // VIMEO
            if (vimeoUserId != null && !vimeoUserId.isBlank()) {
                imported += importFromProvider(VideoProvider.VIMEO,
                        Map.of("userId", vimeoUserId),
                        requestedByUserId);
            }

            long elapsedMs = Duration.ofNanos(System.nanoTime() - startNs).toMillis();
            metrics.recordImportBatch(imported, skipped, elapsedMs);
            log.info("Import job {} completed: imported={} skipped={} elapsed={}ms",
                    key.hash(), imported, skipped, elapsedMs);
        } catch (Exception ex) {
            metrics.incrementFailedImports();
            log.error("Import job {} failed: {}", key.hash(), ex.getMessage(), ex);
        } finally {
            localActiveJobs.remove(key);
            distributedLock.unlock(lockName);
        }
    }

    /**
     * Fallback used by Resilience4j when retries are exhausted.
     */
    @SuppressWarnings("unused")
    public void retryFallback(ImportJobKey key,
                              String youtubePlaylistId,
                              String vimeoUserId,
                              Long requestedByUserId,
                              String lockName,
                              Throwable t) {
        log.error("Retry fallback for import job {} due to {}", key.hash(), t.getMessage(), t);
        metrics.incrementFailedImports();
        localActiveJobs.remove(key);
        distributedLock.unlock(lockName);
    }

    /**
     * Imports videos from a specific provider with given params.
     */
    private int importFromProvider(VideoProvider provider,
                                   Map<String, String> params,
                                   Long requestedByUserId) {

        ExternalVideoClient client = providerClients.get(provider);
        if (client == null) {
            log.warn("No client configured for provider={}", provider);
            return 0;
        }

        List<ExternalVideoClient.ExternalVideo> externalVideos = client.fetchVideos(params);
        int imported = 0;

        for (ExternalVideoClient.ExternalVideo ext : externalVideos) {
            // Deduplicate quickly by external id
            String externalId = ext.externalId();
            if (externalId == null || externalId.isBlank()) {
                continue;
            }
            if (duplicateCache.isRecentlyImported(provider, externalId)) {
                continue;
            }
            if (videoService.existsByProviderAndExternalId(provider, externalId)) {
                duplicateCache.markImported(provider, externalId);
                continue;
            }

            try {
                Video video = new Video();
                video.setTitle(ext.title());
                video.setDescription(ext.description() != null ? ext.description() : "");
                video.setProvider(provider);
                video.setCategory(Optional.ofNullable(ext.category()).orElse(VideoCategory.UNSPECIFIED));
                video.setDuration(ext.duration() != null ? ext.duration() : Duration.ZERO);
                video.setUploadDateTime(Optional.ofNullable(ext.uploadDateTime()).orElse(ZonedDateTime.now()));
                video.setCreatedByUserId(requestedByUserId);
                video.setExternalVideoId(externalId);

                videoService.save(video);
                duplicateCache.markImported(provider, externalId);
                imported++;
            } catch (Exception e) {
                log.warn("Failed to persist video provider={} externalId={} reason={}",
                        provider, externalId, e.getMessage());
            }
        }
        metrics.incrementImported(imported);
        return imported;
    }
}
