package com.ufcstudy.strategy.model;

import com.ufcstudy.domain.strategy.ProbabilityMethod;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public record StrategyRules(
        UUID strategyVersionId,
        String calculationVersion,
        ProbabilityMethod probabilityMethod,
        BigDecimal minimumExpectedValue,
        BigDecimal maximumMarketSpread,
        Duration maximumSnapshotAge,
        Duration maximumSourceGap,
        Duration minimumTimeBeforeFight
) {

    public StrategyRules {
        Objects.requireNonNull(strategyVersionId);
        Objects.requireNonNull(calculationVersion);
        Objects.requireNonNull(probabilityMethod);
        Objects.requireNonNull(minimumExpectedValue);
        Objects.requireNonNull(maximumMarketSpread);
        Objects.requireNonNull(maximumSnapshotAge);
        Objects.requireNonNull(maximumSourceGap);
        Objects.requireNonNull(minimumTimeBeforeFight);

        if (calculationVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "Calculation version cannot be blank"
            );
        }

        if (minimumExpectedValue.signum() < 0) {
            throw new IllegalArgumentException(
                    "Minimum EV cannot be negative"
            );
        }

        if (maximumMarketSpread.signum() < 0
                || maximumMarketSpread.compareTo(
                        BigDecimal.ONE
                ) > 0) {
            throw new IllegalArgumentException(
                    "Maximum spread must be between zero and one"
            );
        }

        requirePositive(
                maximumSnapshotAge,
                "Maximum snapshot age"
        );

        requirePositive(
                maximumSourceGap,
                "Maximum source gap"
        );

        if (minimumTimeBeforeFight.isNegative()) {
            throw new IllegalArgumentException(
                    "Minimum pre-fight duration cannot be negative"
            );
        }
    }

    private static void requirePositive(
            Duration duration,
            String fieldName
    ) {
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }
}