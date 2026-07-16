package com.ufcstudy.eventmatching.automated;

import java.time.Duration;
import java.util.Objects;

public record AutomatedMatchScore(
        double participantScore,
        double scheduledTimeScore,
        double overallScore,
        Duration scheduledTimeDifference
) {

    public AutomatedMatchScore {
        Objects.requireNonNull(scheduledTimeDifference);

        validateScore(participantScore);
        validateScore(scheduledTimeScore);
        validateScore(overallScore);
    }

    private static void validateScore(double value) {
        if (!Double.isFinite(value)
                || value < 0.0
                || value > 1.0) {
            throw new IllegalArgumentException(
                    "Match scores must be between zero and one"
            );
        }
    }
}