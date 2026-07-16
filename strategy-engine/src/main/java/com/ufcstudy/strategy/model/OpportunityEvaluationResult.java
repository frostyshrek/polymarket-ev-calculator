package com.ufcstudy.strategy.model;

import com.ufcstudy.domain.strategy.OpportunityRejectionCode;
import com.ufcstudy.domain.strategy.OpportunityStatus;
import com.ufcstudy.domain.strategy.ProbabilityMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OpportunityEvaluationResult(
        UUID strategyVersionId,
        UUID marketMappingId,
        UUID sportsbookSnapshotId,
        UUID opposingSportsbookSnapshotId,
        UUID predictionSnapshotId,

        Instant decisionTime,
        ProbabilityMethod probabilityMethod,

        BigDecimal referenceProbability,
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

        OpportunityStatus status,
        OpportunityRejectionCode primaryRejectionCode,
        List<RuleFailure> failures,
        String calculationVersion
) {

    public OpportunityEvaluationResult {
        Objects.requireNonNull(strategyVersionId);
        Objects.requireNonNull(marketMappingId);
        Objects.requireNonNull(sportsbookSnapshotId);
        Objects.requireNonNull(opposingSportsbookSnapshotId);
        Objects.requireNonNull(predictionSnapshotId);
        Objects.requireNonNull(decisionTime);
        Objects.requireNonNull(probabilityMethod);
        Objects.requireNonNull(referenceProbability);
        Objects.requireNonNull(sportsbookDecimalOdds);
        Objects.requireNonNull(rawImpliedProbability);
        Objects.requireNonNull(sportsbookNoVigProbability);
        Objects.requireNonNull(estimatedExpectedValue);
        Objects.requireNonNull(expectedProfitPerUnit);
        Objects.requireNonNull(predictionMarketSpread);
        Objects.requireNonNull(status);
        Objects.requireNonNull(failures);
        Objects.requireNonNull(calculationVersion);

        failures = List.copyOf(failures);

        if (status == OpportunityStatus.QUALIFIED
                && !failures.isEmpty()) {
            throw new IllegalArgumentException(
                    "A qualified opportunity cannot contain failures"
            );
        }

        if (status == OpportunityStatus.REJECTED
                && primaryRejectionCode == null) {
            throw new IllegalArgumentException(
                    "A rejected opportunity requires a primary rejection code"
            );
        }
    }

    public String qualificationReason() {
        if (failures.isEmpty()) {
            return "All UFC-EV-v1.0 qualification rules passed";
        }

        return failures.stream()
                .map(failure ->
                        failure.code().name()
                                + ": "
                                + failure.message()
                )
                .collect(
                        java.util.stream.Collectors.joining("; ")
                );
    }
}