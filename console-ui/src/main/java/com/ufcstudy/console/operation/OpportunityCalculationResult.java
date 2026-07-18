package com.ufcstudy.console.operation;

import java.time.Instant;
import java.util.UUID;

public record OpportunityCalculationResult(
        UUID strategyVersionId,
        Instant startedAt,
        Instant completedAt,
        int approvedMappings,
        int mappingsWithSnapshots,
        int qualified,
        int rejected,
        int duplicatesSkipped,
        boolean successful,
        String message
) {
}