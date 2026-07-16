package com.ufcstudy.strategy.calculation;

import com.ufcstudy.persistence.opportunity.model.OpportunityCandidateRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class OpportunityTimingCalculator {

    public OpportunityTiming calculate(
            OpportunityCandidateRecord candidate,
            Instant decisionTime
    ) {
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(decisionTime);

        long sportsbookAge = Duration.between(
                candidate.sportsbookObservedAt(),
                decisionTime
        ).getSeconds();

        long predictionAge = Duration.between(
                candidate.predictionObservedAt(),
                decisionTime
        ).getSeconds();

        long sourceGap = absoluteSeconds(
                Duration.between(
                        candidate.sportsbookObservedAt(),
                        candidate.predictionObservedAt()
                )
        );

        long untilStart = Duration.between(
                decisionTime,
                candidate.scheduledStartTime()
        ).getSeconds();

        return new OpportunityTiming(
                sportsbookAge,
                predictionAge,
                sourceGap,
                untilStart
        );
    }

    private static long absoluteSeconds(Duration duration) {
        return Math.abs(duration.getSeconds());
    }
}