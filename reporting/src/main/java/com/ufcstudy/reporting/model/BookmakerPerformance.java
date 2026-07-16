package com.ufcstudy.reporting.model;

import java.math.BigDecimal;
import java.util.Objects;

public record BookmakerPerformance(
        String bookmakerCode,
        long settledBets,
        long wins,
        long losses,
        long voids,
        BigDecimal settledStakeUnits,
        BigDecimal netProfitUnits,
        BigDecimal roi,
        BigDecimal averageEstimatedEv
) {

    public BookmakerPerformance {
        Objects.requireNonNull(bookmakerCode);
        Objects.requireNonNull(settledStakeUnits);
        Objects.requireNonNull(netProfitUnits);
        Objects.requireNonNull(roi);
        Objects.requireNonNull(averageEstimatedEv);
    }
}