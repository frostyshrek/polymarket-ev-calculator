package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public final class ImpliedProbabilityCalculator {

    public Probability calculate(DecimalOdds odds) {
        Objects.requireNonNull(odds, "Odds cannot be null");

        BigDecimal probability = BigDecimal.ONE.divide(
                odds.value(),
                MathPolicy.INTERNAL
        );

        return Probability.of(probability);
    }
}