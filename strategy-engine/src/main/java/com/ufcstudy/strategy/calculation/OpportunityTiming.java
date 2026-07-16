package com.ufcstudy.strategy.calculation;

public record OpportunityTiming(
        long sportsbookSnapshotAgeSeconds,
        long predictionSnapshotAgeSeconds,
        long sourceGapSeconds,
        long secondsUntilScheduledStart
) {
}