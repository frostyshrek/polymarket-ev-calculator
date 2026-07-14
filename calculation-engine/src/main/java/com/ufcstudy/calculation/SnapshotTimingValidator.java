package com.ufcstudy.calculation;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class SnapshotTimingValidator {

    public boolean isValid(
            Instant decisionTime,
            Instant sportsbookSnapshotTime,
            Instant polymarketSnapshotTime,
            Instant scheduledFightStart,
            Duration maximumSnapshotAge,
            Duration maximumSourceDifference,
            Duration minimumTimeBeforeFight
    ) {
        Objects.requireNonNull(decisionTime);
        Objects.requireNonNull(sportsbookSnapshotTime);
        Objects.requireNonNull(polymarketSnapshotTime);
        Objects.requireNonNull(scheduledFightStart);
        Objects.requireNonNull(maximumSnapshotAge);
        Objects.requireNonNull(maximumSourceDifference);
        Objects.requireNonNull(minimumTimeBeforeFight);

        if (sportsbookSnapshotTime.isAfter(decisionTime)
                || polymarketSnapshotTime.isAfter(decisionTime)) {
            return false;
        }

        Duration sportsbookAge = Duration.between(
                sportsbookSnapshotTime,
                decisionTime
        );

        Duration polymarketAge = Duration.between(
                polymarketSnapshotTime,
                decisionTime
        );

        Duration sourceDifference = absolute(
                Duration.between(
                        sportsbookSnapshotTime,
                        polymarketSnapshotTime
                )
        );

        Duration timeBeforeFight = Duration.between(
                decisionTime,
                scheduledFightStart
        );

        return sportsbookAge.compareTo(maximumSnapshotAge) <= 0
                && polymarketAge.compareTo(maximumSnapshotAge) <= 0
                && sourceDifference.compareTo(maximumSourceDifference) <= 0
                && timeBeforeFight.compareTo(minimumTimeBeforeFight) >= 0;
    }

    private static Duration absolute(Duration duration) {
        return duration.isNegative() ? duration.negated() : duration;
    }
}