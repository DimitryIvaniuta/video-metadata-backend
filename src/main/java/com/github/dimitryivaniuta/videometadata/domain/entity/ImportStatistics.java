package com.github.dimitryivaniuta.videometadata.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportStatistics {
    private int requestedCount;
    private int acceptedCount;
    private int skippedDuplicates;
    private int failedCount;
    private int succeededCount;
}