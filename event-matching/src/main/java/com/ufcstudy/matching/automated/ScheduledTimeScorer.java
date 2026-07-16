package com.ufcstudy.eventmatching.automated;

import java.time.Duration;
import java.time.Instant;

public final class ScheduledTimeScorer {

    private static final Duration PERFECT_WINDOW =
            Duration.ofHours(3);

    private static final Duration MAXIMUM_WINDOW =
            Duration.ofHours(24);

    public double score(
            Instant sportsbookTime,
            Instant predictionTime
    ) {
        if (sportsbookTime == null
                || predictionTime == null) {
            return 0.50;
        }

        Duration difference = difference(
                sportsbookTime,
                predictionTime
        );

        if (difference.compareTo(PERFECT_WINDOW) <= 0) {
            return 1.0;
        }

        if (difference.compareTo(MAXIMUM_WINDOW) > 0) {
            return 0.0;
        }

        double elapsedBeyondPerfect =
                difference.minus(PERFECT_WINDOW)
                        .toSeconds();

        double degradableRange =
                MAXIMUM_WINDOW.minus(PERFECT_WINDOW)
                        .toSeconds();

        return 1.0
                - (elapsedBeyondPerfect / degradableRange);
    }

    public Duration difference(
            Instant left,
            Instant right
    ) {
        if (left == null || right == null) {
            return Duration.ofDays(365);
        }

        return Duration.between(left, right).abs();
    }
}