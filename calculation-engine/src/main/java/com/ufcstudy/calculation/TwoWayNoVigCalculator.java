package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public final class TwoWayNoVigCalculator {

    private final ImpliedProbabilityCalculator impliedProbabilityCalculator;

    public TwoWayNoVigCalculator() {
        this(new ImpliedProbabilityCalculator());
    }

    public TwoWayNoVigCalculator(
            ImpliedProbabilityCalculator impliedProbabilityCalculator
    ) {
        this.impliedProbabilityCalculator = Objects.requireNonNull(
                impliedProbabilityCalculator
        );
    }

    public TwoWayNoVigResult calculate(
            DecimalOdds oddsA,
            DecimalOdds oddsB
    ) {
        Objects.requireNonNull(oddsA, "Odds A cannot be null");
        Objects.requireNonNull(oddsB, "Odds B cannot be null");

        Probability rawA =
                impliedProbabilityCalculator.calculate(oddsA);
        Probability rawB =
                impliedProbabilityCalculator.calculate(oddsB);

        BigDecimal rawTotal = rawA.value()
                .add(rawB.value(), MathPolicy.INTERNAL);

        Probability noVigA = Probability.of(
                rawA.value().divide(rawTotal, MathPolicy.INTERNAL)
        );

        Probability noVigB = Probability.of(
                rawB.value().divide(rawTotal, MathPolicy.INTERNAL)
        );

        BigDecimal overround = rawTotal.subtract(
                BigDecimal.ONE,
                MathPolicy.INTERNAL
        );

        return new TwoWayNoVigResult(
                rawA,
                rawB,
                noVigA,
                noVigB,
                overround
        );
    }
}