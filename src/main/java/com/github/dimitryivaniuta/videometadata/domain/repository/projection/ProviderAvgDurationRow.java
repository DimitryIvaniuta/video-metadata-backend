package com.github.dimitryivaniuta.videometadata.domain.repository.projection;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;

public interface ProviderAvgDurationRow {
    VideoProvider getProvider();
    Double        getAvg();   // AVG returns double in Postgres
}
