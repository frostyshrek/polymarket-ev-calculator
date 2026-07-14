package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public record MarketQuoteResult(
        Probability bestBid,
        Probability bestAsk,
        Probability midpoint,
        BigDecimal spread
) {

    public MarketQuoteResult {
        Objects.requireNonNull(bestBid);
        Objects.requireNonNull(bestAsk);
        Objects.requireNonNull(midpoint);
        Objects.requireNonNull(spread);
    }
}