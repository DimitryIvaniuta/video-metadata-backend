package com.github.dimitryivaniuta.videometadata.persistence.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.Video;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * JPA repository for Video, with support for Specifications.
 */
@Repository
public interface VideoRepository
        extends JpaRepository<Video, Long>,
        JpaSpecificationExecutor<Video> {

    List<Video> findByCreatedByUserId(Long createdByUserId);

    long countByCreatedByUserId(Long createdByUserId);

}
