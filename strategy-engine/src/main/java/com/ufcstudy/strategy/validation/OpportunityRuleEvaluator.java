package com.ufcstudy.strategy.validation;

import com.ufcstudy.domain.strategy.OpportunityRejectionCode;
import com.ufcstudy.persistence.opportunity.model.OpportunityCandidateRecord;
import com.ufcstudy.strategy.calculation.OpportunityTiming;
import com.ufcstudy.strategy.model.RuleFailure;
import com.ufcstudy.strategy.model.StrategyRules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OpportunityRuleEvaluator {

    public List<RuleFailure> evaluate(
            OpportunityCandidateRecord candidate,
            StrategyRules rules,
            OpportunityTiming timing,
            BigDecimal referenceProbability,
            BigDecimal expectedValue
    ) {
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(rules);
        Objects.requireNonNull(timing);
        Objects.requireNonNull(referenceProbability);
        Objects.requireNonNull(expectedValue);

        List<RuleFailure> failures = new ArrayList<>();

        if (!"APPROVED_MANUAL".equals(
                candidate.mappingStatus()
        ) && !"APPROVED_AUTOMATIC".equals(
                candidate.mappingStatus()
        )) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .MAPPING_NOT_APPROVED,
                            "Mapping status is "
                                    + candidate.mappingStatus()
                    )
            );
        }

        if (!"EXACT".equals(
                candidate.settlementCompatibility()
        )) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .SETTLEMENT_NOT_EXACT,
                            "Settlement compatibility is "
                                    + candidate
                                    .settlementCompatibility()
                    )
            );
        }

        if (timing.sportsbookSnapshotAgeSeconds() < 0) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .SPORTSBOOK_SNAPSHOT_IN_FUTURE,
                            "Sportsbook snapshot is after decision time"
                    )
            );
        } else if (
                timing.sportsbookSnapshotAgeSeconds()
                        > rules.maximumSnapshotAge()
                        .getSeconds()
        ) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .SPORTSBOOK_SNAPSHOT_STALE,
                            "Sportsbook snapshot age is "
                                    + timing
                                    .sportsbookSnapshotAgeSeconds()
                                    + " seconds"
                    )
            );
        }

        if (timing.predictionSnapshotAgeSeconds() < 0) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .PREDICTION_SNAPSHOT_IN_FUTURE,
                            "Prediction snapshot is after decision time"
                    )
            );
        } else if (
                timing.predictionSnapshotAgeSeconds()
                        > rules.maximumSnapshotAge()
                        .getSeconds()
        ) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .PREDICTION_SNAPSHOT_STALE,
                            "Prediction snapshot age is "
                                    + timing
                                    .predictionSnapshotAgeSeconds()
                                    + " seconds"
                    )
            );
        }

        if (timing.sourceGapSeconds()
                > rules.maximumSourceGap().getSeconds()) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .SOURCE_TIME_GAP_EXCEEDED,
                            "Source gap is "
                                    + timing.sourceGapSeconds()
                                    + " seconds"
                    )
            );
        }

        if (timing.secondsUntilScheduledStart() <= 0) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .EVENT_ALREADY_STARTED,
                            "Scheduled start is not after decision time"
                    )
            );
        } else if (
                timing.secondsUntilScheduledStart()
                        < rules.minimumTimeBeforeFight()
                        .getSeconds()
        ) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .INSIDE_PREFIGHT_CUTOFF,
                            "Only "
                                    + timing
                                    .secondsUntilScheduledStart()
                                    + " seconds remain before start"
                    )
            );
        }

        if (candidate.predictionSpread().compareTo(
                rules.maximumMarketSpread()
        ) > 0) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .MARKET_SPREAD_TOO_WIDE,
                            "Spread is "
                                    + candidate.predictionSpread()
                    )
            );
        }

        if (referenceProbability.signum() <= 0
                || referenceProbability.compareTo(
                        BigDecimal.ONE
                ) >= 0) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .INVALID_PREDICTION_PROBABILITY,
                            "Reference probability is "
                                    + referenceProbability
                    )
            );
        }

        if (expectedValue.compareTo(
                rules.minimumExpectedValue()
        ) < 0) {
            failures.add(
                    failure(
                            OpportunityRejectionCode
                                    .EV_BELOW_THRESHOLD,
                            "Estimated EV is "
                                    + expectedValue
                                    + ", minimum is "
                                    + rules.minimumExpectedValue()
                    )
            );
        }

        return List.copyOf(failures);
    }

    private static RuleFailure failure(
            OpportunityRejectionCode code,
            String message
    ) {
        return new RuleFailure(code, message);
    }
}