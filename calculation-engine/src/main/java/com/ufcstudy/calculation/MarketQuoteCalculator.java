package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public final class MarketQuoteCalculator {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    public MarketQuoteResult calculate(
            Probability bestBid,
            Probability bestAsk
    ) {
        Objects.requireNonNull(bestBid, "Best bid cannot be null");
        Objects.requireNonNull(bestAsk, "Best ask cannot be null");

        if (bestBid.value().compareTo(bestAsk.value()) > 0) {
            throw new IllegalArgumentException(
                    "Best bid cannot exceed best ask"
            );
        }

        BigDecimal spread = bestAsk.value()
                .subtract(bestBid.value(), MathPolicy.INTERNAL);

        BigDecimal midpointValue = bestBid.value()
                .add(bestAsk.value(), MathPolicy.INTERNAL)
                .divide(TWO, MathPolicy.INTERNAL);

        return new MarketQuoteResult(
                bestBid,
                bestAsk,
                Probability.of(midpointValue),
                spread
        );
    }
}