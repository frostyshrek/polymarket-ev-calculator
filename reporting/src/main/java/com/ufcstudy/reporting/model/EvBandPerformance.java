package com.ufcstudy.reporting.model;

import java.math.BigDecimal;
import java.util.Objects;

public record EvBandPerformance(
        String evBand,
        BigDecimal minimumEvInclusive,
        BigDecimal maximumEvExclusive,
        long settledBets,
        BigDecimal settledStakeUnits,
        BigDecimal netProfitUnits,
        BigDecimal roi
) {

    public EvBandPerformance {
        Objects.requireNonNull(evBand);
        Objects.requireNonNull(minimumEvInclusive);
        Objects.requireNonNull(settledStakeUnits);
        Objects.requireNonNull(netProfitUnits);
        Objects.requireNonNull(roi);
    }
}