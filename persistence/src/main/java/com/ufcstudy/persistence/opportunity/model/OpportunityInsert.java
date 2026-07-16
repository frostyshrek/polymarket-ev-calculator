package com.ufcstudy.persistence.opportunity.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OpportunityInsert(
        UUID id,
        UUID strategyVersionId,
        UUID marketMappingId,
        UUID sportsbookSnapshotId,
        UUID opposingSportsbookSnapshotId,
        UUID predictionSnapshotId,

        Instant decisionTime,
        BigDecimal referenceProbability,
        String probabilityMethod,
        BigDecimal sportsbookDecimalOdds,
        BigDecimal rawImpliedProbability,
        BigDecimal sportsbookNoVigProbability,
        BigDecimal estimatedExpectedValue,
        BigDecimal expectedProfitPerUnit,
        BigDecimal predictionMarketSpread,

        long sportsbookSnapshotAgeSeconds,
        long predictionSnapshotAgeSeconds,
        long sourceGapSeconds,
        long secondsUntilScheduledStart,

        String qualificationStatus,
        String qualificationReason,
        String rejectionCode,
        String calculationVersion
) {

    public OpportunityInsert {
        Objects.requireNonNull(id);
        Objects.requireNonNull(strategyVersionId);
        Objects.requireNonNull(marketMappingId);
        Objects.requireNonNull(sportsbookSnapshotId);
        Objects.requireNonNull(opposingSportsbookSnapshotId);
        Objects.requireNonNull(predictionSnapshotId);
        Objects.requireNonNull(decisionTime);
        Objects.requireNonNull(referenceProbability);
        Objects.requireNonNull(probabilityMethod);
        Objects.requireNonNull(sportsbookDecimalOdds);
        Objects.requireNonNull(rawImpliedProbability);
        Objects.requireNonNull(sportsbookNoVigProbability);
        Objects.requireNonNull(estimatedExpectedValue);
        Objects.requireNonNull(expectedProfitPerUnit);
        Objects.requireNonNull(predictionMarketSpread);
        Objects.requireNonNull(qualificationStatus);
        Objects.requireNonNull(qualificationReason);
        Objects.requireNonNull(calculationVersion);
    }
}