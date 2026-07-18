package com.ufcstudy.console.operation;

import java.time.Instant;

public record QualifiedPaperBetPlacementResult(
        Instant startedAt,
        Instant completedAt,
        int qualifiedOpportunitiesFound,
        int placed,
        int alreadyPlaced,
        int officialEntryAlreadyExists,
        int failed,
        boolean successful,
        String message
) {
}