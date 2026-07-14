package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.domain.value.Units;

import java.math.BigDecimal;
import java.util.Objects;

public final class KellyCalculator {

    public KellyResult calculate(
            Probability probability,
            DecimalOdds odds,
            Units bankroll,
            BigDecimal kellyMultiplier,
            BigDecimal maximumBankrollFraction
    ) {
        Objects.requireNonNull(probability, "Probability cannot be null");
        Objects.requireNonNull(odds, "Odds cannot be null");
        Objects.requireNonNull(bankroll, "Bankroll cannot be null");
        Objects.requireNonNull(
                kellyMultiplier,
                "Kelly multiplier cannot be null"
        );
        Objects.requireNonNull(
                maximumBankrollFraction,
                "Maximum bankroll fraction cannot be null"
        );

        validateFraction(kellyMultiplier, "Kelly multiplier");
        validateFraction(
                maximumBankrollFraction,
                "Maximum bankroll fraction"
        );

        BigDecimal numerator = probability.value()
                .multiply(odds.value(), MathPolicy.INTERNAL)
                .subtract(BigDecimal.ONE, MathPolicy.INTERNAL);

        BigDecimal denominator = odds.value()
                .subtract(BigDecimal.ONE, MathPolicy.INTERNAL);

        BigDecimal rawFullKelly = numerator.divide(
                denominator,
                MathPolicy.INTERNAL
        );

        BigDecimal fullKelly = rawFullKelly.max(BigDecimal.ZERO);

        BigDecimal fractionalKelly = fullKelly.multiply(
                kellyMultiplier,
                MathPolicy.INTERNAL
        );

        BigDecimal cappedKelly = fractionalKelly.min(
                maximumBankrollFraction
        );

        BigDecimal stake = bankroll.value().multiply(
                cappedKelly,
                MathPolicy.INTERNAL
        );

        return new KellyResult(
                fullKelly,
                fractionalKelly,
                cappedKelly,
                stake
        );
    }

    private static void validateFraction(
            BigDecimal value,
            String fieldName
    ) {
        if (value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be between 0 and 1: " + value
            );
        }
    }
}