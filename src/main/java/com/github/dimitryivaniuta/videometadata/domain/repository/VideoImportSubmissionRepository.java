package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.VideoImportSubmission;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface VideoImportSubmissionRepository extends R2dbcRepository<VideoImportSubmission, Long> {

    Mono<VideoImportSubmission> findBySubmissionId(String submissionId);

}