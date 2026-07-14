package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public final class ClosingLineValueCalculator {

    public BigDecimal calculateOddsBased(
            DecimalOdds oddsTaken,
            DecimalOdds closingOdds
    ) {
        Objects.requireNonNull(oddsTaken, "Odds taken cannot be null");
        Objects.requireNonNull(closingOdds, "Closing odds cannot be null");

        return oddsTaken.value()
                .divide(closingOdds.value(), MathPolicy.INTERNAL)
                .subtract(BigDecimal.ONE, MathPolicy.INTERNAL);
    }

    public BigDecimal calculateProbabilityBased(
            Probability closingNoVigProbability,
            Probability entryNoVigProbability
    ) {
        Objects.requireNonNull(
                closingNoVigProbability,
                "Closing probability cannot be null"
        );
        Objects.requireNonNull(
                entryNoVigProbability,
                "Entry probability cannot be null"
        );

        return closingNoVigProbability.value()
                .subtract(
                        entryNoVigProbability.value(),
                        MathPolicy.INTERNAL
                );
    }
}