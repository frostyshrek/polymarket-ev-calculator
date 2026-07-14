package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.domain.value.Units;

import java.math.BigDecimal;
import java.util.Objects;

public final class ExpectedValueCalculator {

    private final ImpliedProbabilityCalculator impliedProbabilityCalculator;

    public ExpectedValueCalculator() {
        this(new ImpliedProbabilityCalculator());
    }

    public ExpectedValueCalculator(
            ImpliedProbabilityCalculator impliedProbabilityCalculator
    ) {
        this.impliedProbabilityCalculator = Objects.requireNonNull(
                impliedProbabilityCalculator,
                "Implied probability calculator cannot be null"
        );
    }

    public ExpectedValueResult calculate(
            Probability referenceProbability,
            DecimalOdds odds,
            Units stake
    ) {
        Objects.requireNonNull(
                referenceProbability,
                "Reference probability cannot be null"
        );
        Objects.requireNonNull(odds, "Odds cannot be null");
        Objects.requireNonNull(stake, "Stake cannot be null");

        BigDecimal expectedReturnRate = referenceProbability.value()
                .multiply(odds.value(), MathPolicy.INTERNAL)
                .subtract(BigDecimal.ONE, MathPolicy.INTERNAL);

        BigDecimal expectedProfitUnits = stake.value()
                .multiply(expectedReturnRate, MathPolicy.INTERNAL);

        Probability breakEvenProbability =
                impliedProbabilityCalculator.calculate(odds);

        return new ExpectedValueResult(
                referenceProbability,
                breakEvenProbability,
                expectedReturnRate,
                expectedProfitUnits
        );
    }
}