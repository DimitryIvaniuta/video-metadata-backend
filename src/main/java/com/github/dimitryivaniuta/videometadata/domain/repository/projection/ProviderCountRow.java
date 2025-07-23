package com.github.dimitryivaniuta.videometadata.domain.repository.projection;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;

/**
 * Row projection for "count per provider" query.
 */
public interface ProviderCountRow {
    VideoProvider getProvider();
    Long          getCnt();
}

