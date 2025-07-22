package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.VideoImportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoImportSubmissionRepository extends JpaRepository<VideoImportSubmission, Long> {

    Optional<VideoImportSubmission> findBySubmissionId(String submissionId);
}