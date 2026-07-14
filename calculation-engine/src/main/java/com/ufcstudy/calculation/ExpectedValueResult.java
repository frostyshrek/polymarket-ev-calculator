package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public record ExpectedValueResult(
        Probability referenceProbability,
        Probability breakEvenProbability,
        BigDecimal expectedReturnRate,
        BigDecimal expectedProfitUnits
) {

    public ExpectedValueResult {
        Objects.requireNonNull(
                referenceProbability,
                "Reference probability cannot be null"
        );
        Objects.requireNonNull(
                breakEvenProbability,
                "Break-even probability cannot be null"
        );
        Objects.requireNonNull(
                expectedReturnRate,
                "Expected return rate cannot be null"
        );
        Objects.requireNonNull(
                expectedProfitUnits,
                "Expected profit cannot be null"
        );
    }

    public boolean isPositive() {
        return expectedReturnRate.signum() > 0;
    }

    public boolean meetsThreshold(BigDecimal minimumExpectedReturnRate) {
        Objects.requireNonNull(
                minimumExpectedReturnRate,
                "Minimum expected return rate cannot be null"
        );

        return expectedReturnRate.compareTo(minimumExpectedReturnRate) >= 0;
    }
}