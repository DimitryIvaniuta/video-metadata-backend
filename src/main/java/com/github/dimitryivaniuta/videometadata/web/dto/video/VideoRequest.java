package com.github.dimitryivaniuta.videometadata.web.dto.video;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import lombok.Builder;

/**
 * Simple DTO for bulk external fetch requests.
 *
 * @param provider     external provider
 * @param externalId identifier at that source
 */
@Builder
public record VideoRequest(VideoProvider provider, String externalId) {}