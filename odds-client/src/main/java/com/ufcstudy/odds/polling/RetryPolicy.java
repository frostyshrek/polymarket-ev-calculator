package com.ufcstudy.odds.polling;

import java.time.Duration;
import java.util.Objects;

public record RetryPolicy(
        int maximumRetries,
        Duration initialDelay,
        Duration maximumDelay
) {

    public RetryPolicy {
        Objects.requireNonNull(initialDelay);
        Objects.requireNonNull(maximumDelay);

        if (maximumRetries < 0) {
            throw new IllegalArgumentException(
                    "Maximum retries cannot be negative"
            );
        }

        if (initialDelay.isNegative() || initialDelay.isZero()) {
            throw new IllegalArgumentException(
                    "Initial delay must be positive"
            );
        }

        if (maximumDelay.compareTo(initialDelay) < 0) {
            throw new IllegalArgumentException(
                    "Maximum delay cannot be below initial delay"
            );
        }
    }

    public Duration delayForAttempt(int retryAttempt) {
        if (retryAttempt < 1) {
            throw new IllegalArgumentException(
                    "Retry attempt begins at one"
            );
        }

        long multiplier = 1L << Math.min(retryAttempt - 1, 20);
        Duration calculated = initialDelay.multipliedBy(multiplier);

        return calculated.compareTo(maximumDelay) > 0
                ? maximumDelay
                : calculated;
    }
}