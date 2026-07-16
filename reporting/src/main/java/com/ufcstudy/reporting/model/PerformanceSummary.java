package com.ufcstudy.reporting.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PerformanceSummary(
        long totalBets,
        long openBets,
        long settledBets,
        long wins,
        long losses,
        long voids,

        BigDecimal totalStakeUnits,
        BigDecimal settledStakeUnits,
        BigDecimal totalGrossReturnUnits,
        BigDecimal totalNetProfitUnits,

        BigDecimal roi,
        BigDecimal winRate,
        BigDecimal averageDecimalOdds,
        BigDecimal averageReferenceProbability,
        BigDecimal averageEstimatedEv
) {

    public PerformanceSummary {
        Objects.requireNonNull(totalStakeUnits);
        Objects.requireNonNull(settledStakeUnits);
        Objects.requireNonNull(totalGrossReturnUnits);
        Objects.requireNonNull(totalNetProfitUnits);
        Objects.requireNonNull(roi);
        Objects.requireNonNull(winRate);
        Objects.requireNonNull(averageDecimalOdds);
        Objects.requireNonNull(averageReferenceProbability);
        Objects.requireNonNull(averageEstimatedEv);

        if (totalBets < 0
                || openBets < 0
                || settledBets < 0
                || wins < 0
                || losses < 0
                || voids < 0) {
            throw new IllegalArgumentException(
                    "Report counts cannot be negative"
            );
        }
    }
}